/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.chatbot.bridge;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.dynmap.DynmapAPI;
import org.dynmap.DynmapWebChatEvent;

import com.github.ucchyocean.chatbot.ChatBotConfig;
import com.github.ucchyocean.chatbot.MintChatBot;
import com.github.ucchyocean.chatbot.ResponceData;
import com.github.ucchyocean.chatbot.URLResponcer;
import com.github.ucchyocean.chatbot.Utility;

/**
 * dynmap連携クラス
 * @author ucchy
 */
public class DynmapBridge implements Listener {

    /** dynmap-apiクラス */
    private DynmapAPI dynmap;

    private MintChatBot parent;

    /** コンストラクタは使用不可 */
    private DynmapBridge(MintChatBot parent, DynmapAPI dynmap) {
        this.parent = parent;
        this.dynmap = dynmap;
    }

    /**
     * dynmap-apiをロードする
     * @param parent MintChatBotのインスタンス
     * @return ロードしたブリッジ
     */
    public static DynmapBridge load(MintChatBot parent) {

        PluginManager pm = Bukkit.getPluginManager();
        Plugin dynmap = pm.getPlugin("dynmap");
        if ( dynmap != null ) {
            DynmapBridge bridge = new DynmapBridge(parent, (DynmapAPI)dynmap);
            pm.registerEvents(bridge, parent);
            return bridge;
        }

        return null;
    }

    /**
     * dynmapにプレイヤーのチャットを流す
     * @param player プレイヤー
     * @param message 発言内容
     */
    public void chat(Player player, String message) {

        dynmap.postPlayerMessageToWeb(player, message);
    }

    /**
     * dynmapにブロードキャストメッセージを流す
     * @param message メッセージ
     */
    public void broadcast(String message) {

        dynmap.sendBroadcastToWeb(null, message);
    }

    /**
     * DynmapのWebUIからチャット発言されたときのイベント
     * @param event
     */
    @EventHandler
    public void onDynmapWebChat(DynmapWebChatEvent event) {

        String message = event.getMessage();
        String displayName = event.getName() + "@" + event.getSource();
        ChatBotConfig config = parent.getCBConfig();

        // IRCBotがいるなら、IRCにも流す
        if ( parent.getIRCBot() != null ) {
            parent.getIRCBot().sendLunaChatMessage(displayName, message);
        }

        if ( parent.getCBConfig().isResponceChat() ) {

            // レスポンスデータに一致があるなら、レスポンスを返す
            ResponceData responceData = parent.getResponceData();
            String responce = responceData.getResponceIfMatch(message, displayName);

            if ( responce != null ) {

                final String res = Utility.replaceColorCode(responce.replace("\\n", "\n"));

                // 数tick遅らせて送信する
                new BukkitRunnable() {
                    public void run() {
                        parent.say(res);
                    }
                }.runTaskLaterAsynchronously(parent, parent.getCBConfig().getResponceDelayTicks());

                return;
            }
        }

        // URLマッチをする場合は、タスクを作成して応答させる。
        if ( config.isGetURLTitle() && URLResponcer.containsURL(message) ) {

            final URLResponcer resp = new URLResponcer(message, displayName);

            // 非同期で処理する
            new BukkitRunnable() {

                @Override
                public void run() {

                    String responce = resp.getResponce();
                    if ( responce != null ) {
                        parent.say(responce);
                    }
                }

            }.runTaskLaterAsynchronously(parent, parent.getCBConfig().getResponceDelayTicks());

            return;
        }

    }
}
