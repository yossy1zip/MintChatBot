/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2014
 */
package com.github.ucchyocean.chatbot;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * チャットBOTプラグイン
 * @author ucchy
 */
public class ChatBot extends JavaPlugin implements Listener {

    private static ChatBot instance;
    private ChatBotConfig config;
    private ResponceData responceData;
    private TimeSignalData timeSignalData;
    private TimerTask timer;

    private VaultChatBridge vaultchat;

    /**
     * プラグインが有効になったときに呼び出されるメソッドです。
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {

        instance = this;

        // 設定のリロード
        reloadAllData();

        // VaultChatをロード
        if ( getServer().getPluginManager().isPluginEnabled("Vault") ) {
            vaultchat = VaultChatBridge.load(
                    getServer().getPluginManager().getPlugin("Vault"));
        }

        // LunaChatがロードされているなら、専用リスナーを登録する
        if ( getServer().getPluginManager().isPluginEnabled("LunaChat") ) {
            getServer().getPluginManager().registerEvents(
                    new LunaChatListener(this), this);
        }

        // リスナーの登録
        getServer().getPluginManager().registerEvents(this, this);

        // タイマーの起動
        timer = new TimerTask(config, timeSignalData);
        timer.runTaskTimerAsynchronously(this, 100, 100);
    }

    /**
     * プラグインのコマンドが実行されたときに呼び出されるメソッドです。
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(
            CommandSender sender, Command command, String label, String[] args) {

        if ( args.length >= 1 && args[0].equals("reload") ) {
            reloadAllData();
            sender.sendMessage("設定ファイルをリロードしました。");
            return true;
        }

        return false;
    }

    /**
     * チャット発言がされたときに呼び出されるメソッドです。
     * @param event
     */
    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onChat(AsyncPlayerChatEvent event) {

        String message = event.getMessage();
        Player player = event.getPlayer();

        // レスポンスデータに一致があるなら、レスポンスを返す
        String responce = responceData.getResponceIfMatch(message, player, vaultchat);

        if ( responce != null ) {

            String temp = config.getResponceFormat();
            temp = temp.replace("%botName", config.getBotName());
            temp = temp.replace("%responce", responce);
            temp = temp.replace("\\n", "\n");
            final String res = Utility.replaceColorCode(temp);

            // 3tick遅らせて送信する
            new BukkitRunnable() {
                public void run() {
                    Bukkit.broadcastMessage(res);
                }
            }.runTaskLater(this, 3);

            return;
        }

        // URLマッチをする場合は、タスクを作成して応答させる。
        if ( config.isGetURLTitle() && URLResponcer.containsURL(message) ) {

            URLResponcer resp = new URLResponcer(message, player, config, vaultchat);
            resp.runTaskAsynchronously(this);

            return;
        }
    }

    /**
     * プレイヤーがサーバーに参加したときに呼び出されるメソッドです。
     * @param event
     */
    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onServerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        String responce;

        // レスポンスを取得
        if ( !player.hasPlayedBefore() ) {
            responce = config.getFirstJoinResponce();
        } else {
            responce = config.getJoinResponce();
        }

        if ( responce == null || responce.equals("") ) {
            return;
        }

        String temp = config.getResponceFormat();
        String base = temp.replace("%botName", config.getBotName());
        responce = base.replace("%responce", responce);
        responce = responce.replace("%player", player.getName());
        if ( vaultchat != null ) {
            responce = responce.replace("%prefix", vaultchat.getPlayerPrefix(player));
            responce = responce.replace("%suffix", vaultchat.getPlayerSuffix(player));
        } else {
            responce = responce.replace("%prefix", "");
            responce = responce.replace("%suffix", "");
        }
        responce = responce.replace("\\n", "\n");
        final String res = Utility.replaceColorCode(responce);

        // 3tick遅らせて送信する
        new BukkitRunnable() {
            public void run() {
                Bukkit.broadcastMessage(res);
            }
        }.runTaskLater(this, 3);
    }

    /**
     * @return チャットBOTプラグインのインスタンス
     */
    protected static ChatBot getInstance() {
        return instance;
    }

    /**
     * @return チャットBOTプラグインのJarファイル
     */
    protected static File getJarFile() {
        return instance.getFile();
    }

    /**
     * @return チャットBOTプラグインのコンフィグ
     */
    protected ChatBotConfig getCBConfig() {
        return config;
    }

    /**
     * @return VaultChatブリッジ
     */
    protected VaultChatBridge getVaultChat() {
        return vaultchat;
    }

    /**
     * @return レスポンスデータ
     */
    protected ResponceData getResponceData() {
        return responceData;
    }

    /**
     * 全ての設定データをリロードします。
     */
    public void reloadAllData() {

        // コンフィグのロード
        if ( config == null ) {
            config = new ChatBotConfig(getFile(), getDataFolder());
        } else {
            config.reloadConfig();
        }

        // レスポンスデータのロード
        if ( responceData == null ) {
            responceData = new ResponceData(getFile(), getDataFolder());
        } else {
            responceData.reloadData();
        }

        // 時報データのロード
        if ( timeSignalData == null ) {
            timeSignalData = new TimeSignalData(getFile(), getDataFolder());
        } else {
            timeSignalData.reloadData();
        }
    }
}
