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

    /**
     * プラグインが有効になったときに呼び出されるメソッドです。
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {

        instance = this;

        // 設定のリロード
        reloadAllData();

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
        String responce = responceData.getResponceIfMatch(message, player);

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

            String temp = config.getResponceFormat();
            String base = temp.replace("%botName", config.getBotName());


            String sf = Utility.replaceColorCode(base.replace(
                    "%responce", config.getGetURLTitleSuccess()));
            String ff = Utility.replaceColorCode(base.replace(
                    "%responce", config.getGetURLTitleFail()));
            String nf = Utility.replaceColorCode(base.replace(
                    "%responce", config.getGetURLTitleNotFound()));

            URLResponcer resp = new URLResponcer(message, player, sf, ff, nf);
            resp.runTaskAsynchronously(this);

            return;
        }
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
    public ChatBotConfig getCBConfig() {
        return config;
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
