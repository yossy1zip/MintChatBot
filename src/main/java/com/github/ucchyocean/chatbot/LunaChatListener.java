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
        VaultChatBridge vaultchat = parent.getVaultChat();
        ResponceData responceData = parent.getResponceData();

        // レスポンスデータに一致があるなら、レスポンスを返す
        String responce = responceData.getResponceIfMatch(message, player, vaultchat);

        if ( responce != null ) {

            final String botName = config.getBotName();
            final String res = Utility.replaceColorCode(responce.replace("\\n", "\n"));

            // 10tick遅らせて送信する
            new BukkitRunnable() {
                public void run() {
                    channel.chatFromOtherSource(botName, "", res);
                }
            }.runTaskLaterAsynchronously(parent, 10);

            return;
        }

        // URLマッチをする場合は、タスクを作成して応答させる。
        if ( config.isGetURLTitle() && URLResponcer.containsURL(message) ) {

            final URLResponcer resp = new URLResponcer(message, player, config, vaultchat);

            // 非同期で処理する
            new BukkitRunnable() {

                @Override
                public void run() {

                    String responce = resp.getResponce();
                    if ( responce != null ) {
                        channel.chatFromOtherSource(config.getBotName(), "", responce);
                    }
                }

            }.runTaskLaterAsynchronously(parent, 10);

            return;
        }
    }
}
