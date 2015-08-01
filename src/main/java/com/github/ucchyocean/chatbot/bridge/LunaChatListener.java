/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2014
 */
package com.github.ucchyocean.chatbot.bridge;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.ucchyocean.chatbot.ChatBotConfig;
import com.github.ucchyocean.chatbot.MintChatBot;
import com.github.ucchyocean.chatbot.ResponceData;
import com.github.ucchyocean.chatbot.URLResponcer;
import com.github.ucchyocean.chatbot.Utility;
import com.github.ucchyocean.chatbot.irc.IRCColor;
import com.github.ucchyocean.lc.LunaChat;
import com.github.ucchyocean.lc.LunaChatAPI;
import com.github.ucchyocean.lc.channel.Channel;
import com.github.ucchyocean.lc.event.LunaChatChannelMessageEvent;

/**
 * LunaChat連携用リスナー
 * @author ucchy
 */
public class LunaChatListener implements Listener {

    private MintChatBot parent;
    private ChatBotConfig config;
    private LunaChatAPI api;

    /**
     * コンストラクタ
     */
    public LunaChatListener(MintChatBot parent) {
        this.parent = parent;
        this.config = parent.getCBConfig();
        api = LunaChat.getInstance().getLunaChatAPI();
    }

    /**
     * チャンネルチャットの発言イベント
     * @param event
     */
    @EventHandler
    public void onChannelChat(LunaChatChannelMessageEvent event) {

        final Channel channel = api.getChannel(event.getChannelName());

        if ( !channel.isBroadcastChannel() || channel.isWorldRange() || channel.getChatRange() > 0 ) {
            return;
        }

        String message = event.getOriginalMessage();
        Player player = (event.getPlayer() != null) ? player = event.getPlayer().getPlayer() : null;
        String displayName = event.getDisplayName();

        // Bot自身の発言なら無視する
        if ( displayName.startsWith(config.getBotName() + "@") ) {
            return;
        }

        // dynmapのweb発言なら、Dynmap連携の方で流れるから、無視する
        if ( displayName.endsWith("@web") ) {
            return;
        }

        // IRCBotがいるなら、IRCにも流す
        if ( parent.getIRCBot() != null ) {
            parent.getIRCBot().sendLunaChatMessage(displayName, message);
        }

        if ( parent.getCBConfig().isResponceChat() ) {

            // レスポンスデータに一致があるなら、レスポンスを返す
            ResponceData responceData = parent.getResponceData();
            String responce = responceData.getResponceIfMatch(message, player);

            if ( responce != null ) {

                final String botName = config.getBotName();
                final String res = Utility.replaceColorCode(responce.replace("\\n", "\n"));

                // 数tick遅らせて送信する
                new BukkitRunnable() {
                    public void run() {
                        channel.chatFromOtherSource(botName, "", res);

                        if ( parent.getIRCBot() != null ) {
                            // IRC連携状態なら、IRCにも発言する
                            String msg = IRCColor.convRES2IRC(res.replace("\\n", " "));
                            parent.getIRCBot().sendMessage(msg);
                        }
                    }
                }.runTaskLaterAsynchronously(parent, parent.getCBConfig().getResponceDelayTicks());

                return;
            }
        }

        // URLマッチをする場合は、タスクを作成して応答させる。
        if ( config.isGetURLTitle() && URLResponcer.containsURL(message) ) {

            final URLResponcer resp = new URLResponcer(message, null, player);

            // 非同期で処理する
            new BukkitRunnable() {

                @Override
                public void run() {

                    String responce = resp.getResponce();
                    if ( responce != null ) {
                        channel.chatFromOtherSource(config.getBotName(), "", responce);

                        if ( parent.getIRCBot() != null ) {
                            // IRC連携状態なら、IRCにも発言する
                            String msg = IRCColor.convRES2IRC(responce.replace("\\n", " "));
                            parent.getIRCBot().sendMessage(msg);
                        }
                    }
                }

            }.runTaskLaterAsynchronously(parent, parent.getCBConfig().getResponceDelayTicks());

            return;
        }
    }
}
