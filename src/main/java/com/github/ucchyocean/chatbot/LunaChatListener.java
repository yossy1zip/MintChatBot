/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2014
 */
package com.github.ucchyocean.chatbot;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.ucchyocean.chatbot.irc.IRCColor;
import com.github.ucchyocean.lc.LunaChat;
import com.github.ucchyocean.lc.LunaChatAPI;
import com.github.ucchyocean.lc.channel.Channel;
import com.github.ucchyocean.lc.event.LunaChatChannelChatEvent;

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
    public void onChannelChat(LunaChatChannelChatEvent event) {

        final Channel channel = api.getChannel(event.getChannelName());

        if ( !channel.isBroadcastChannel() ) {
            return;
        }

        String message = event.getNgMaskedMessage();
        Player player = event.getPlayer().getPlayer();

        // IRCBotがいるなら、IRCにも流す
        if ( parent.getIRCBot() != null ) {
            parent.getIRCBot().sendLunaChatMessage(player.getDisplayName(), message);
        }

        if ( parent.getCBConfig().isResponceChat() ) {

            // レスポンスデータに一致があるなら、レスポンスを返す
            VaultChatBridge vaultchat = parent.getVaultChat();
            ResponceData responceData = parent.getResponceData();
            String responce = responceData.getResponceIfMatch(message, player, vaultchat);

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

            VaultChatBridge vaultchat = parent.getVaultChat();
            final URLResponcer resp = new URLResponcer(message, player, vaultchat);

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
