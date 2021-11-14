/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package com.github.ucchyocean.chatbot;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import de.myzelyam.api.vanish.VanishAPI;

import com.github.ucchyocean.chatbot.bridge.VaultChatBridge;

/**
 * ChatBotのリスナークラス
 * @author ucchy
 */
public class ChatBotListener implements Listener {

    /**
     * チャット発言がされたときに呼び出されるメソッドです。
     * @param event
     */
    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onChat(AsyncPlayerChatEvent event) {

        String message = event.getMessage();
        Player player = event.getPlayer();
        MintChatBot parent = MintChatBot.getInstance();
        ChatBotConfig config = parent.getCBConfig();
        ResponceData responceData = parent.getResponceData();

        if ( config.isResponceChat() ) {
            // レスポンスデータに一致があるなら、レスポンスを返す
            final String responce = responceData.getResponceIfMatch(message, player);

            if ( responce != null ) {

                // 数tick遅らせて送信する
                new BukkitRunnable() {
                    public void run() {
                        MintChatBot.getInstance().say(responce);
                    }
                }.runTaskLater(parent, config.getResponceDelayTicks());

                return;
            }
        }

        // URLマッチをする場合は、タスクを作成して応答させる。
        if ( config.isGetURLTitle() && URLResponcer.containsURL(message) ) {

            URLResponcer resp = new URLResponcer(message, player, null);
            resp.runTaskLaterAsynchronously(parent, config.getResponceDelayTicks());

            return;
        }
    }

    /**
     * プレイヤーがサーバーに参加したときに呼び出されるメソッドです。
     * @param event
     */
    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onServerJoin(PlayerJoinEvent event) {

        final MintChatBot parent = MintChatBot.getInstance();
        final ChatBotConfig config = parent.getCBConfig();

        // 無効なら何もしない
        if ( !config.isResponceJoinServer() ) {
            return;
        }

        final Player player = event.getPlayer();

        if ( VanishAPI.isInvisible(player) ) {
            return;
        }

        final Messages messages = parent.getMessages();
        final VaultChatBridge vaultchat = parent.getVaultChat();

        // 以降は非同期で処理する
        new BukkitRunnable() {
            public void run() {

                String responce;

                // レスポンスを取得
                if ( !player.hasPlayedBefore() ) {
                    responce = messages.getResponceIfMatch("firstJoinResponce");
                } else {
                    responce = messages.getResponceIfMatch("joinResponce");
                }

                if ( responce == null || responce.equals("") ) {
                    return;
                }

                responce = responce.replace("%player", player.getName());
                if ( vaultchat != null ) {
                    responce = responce.replace("%prefix", vaultchat.getPlayerPrefix(player));
                    responce = responce.replace("%suffix", vaultchat.getPlayerSuffix(player));
                } else {
                    responce = responce.replace("%prefix", "");
                    responce = responce.replace("%suffix", "");
                }

                // 数tick遅らせて送信する
                final String msg = responce;
                new BukkitRunnable() {
                    public void run() {
                        MintChatBot.getInstance().say(msg);
                    }
                }.runTaskLater(parent, config.getResponceDelayTicks());
            }
        }.runTaskLaterAsynchronously(MintChatBot.getInstance(), config.getResponceDelayTicks());
    }

}
