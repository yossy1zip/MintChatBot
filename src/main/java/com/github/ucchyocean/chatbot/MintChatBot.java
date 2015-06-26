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
import org.bukkit.plugin.java.JavaPlugin;

import com.github.ucchyocean.chatbot.irc.IRCBot;
import com.github.ucchyocean.chatbot.irc.IRCBotConfig;
import com.github.ucchyocean.chatbot.irc.IRCColor;

/**
 * チャットBOTプラグイン
 * @author ucchy
 */
public class MintChatBot extends JavaPlugin {

    private ChatBotConfig config;
    private ResponceData responceData;
    private TimeSignalData timeSignalData;
    private Messages messages;
    private TimerTask timer;
    private IRCBot ircbot;

    private VaultChatBridge vaultchat;

    private static MintChatBot instance;

    /**
     * プラグインが有効になったときに呼び出されるメソッドです。
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {

        // 設定のリロード
        reloadAllData();

        // VaultChatをロード
        if ( getServer().getPluginManager().isPluginEnabled("Vault") ) {
            vaultchat = VaultChatBridge.load();
        }

        // LunaChatがロードされているなら、専用リスナーを登録する
        if ( getServer().getPluginManager().isPluginEnabled("LunaChat") ) {
            getServer().getPluginManager().registerEvents(
                    new LunaChatListener(this), this);
        }

        // リスナーの登録
        getServer().getPluginManager().registerEvents(new ChatBotListener(), this);

        // タイマーの起動
        timer = new TimerTask(config, timeSignalData);
        timer.runTaskTimerAsynchronously(this, 100, 100);

        // IRCBotの起動
        if ( checkIRCConfig() ) {
            ircbot = new IRCBot(config.getIrcbotConfig());
            ircbot.connect();
        }
    }

    /**
     * プラグインが無効になったときに呼び出されるメソッドです。
     * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
     */
    @Override
    public void onDisable() {

        // IRCBotの停止
        if ( ircbot != null ) {
            ircbot.disconnect();
            ircbot = null;
        }
    }

    /**
     * Botがチャットに発言を行う。
     * @param message 発言内容
     */
    public void say(String message) {

        if ( message == null ) return;

        String base = config.getResponceFormat();
        String msg = base
                .replace("%botName", config.getBotName())
                .replace("%responce", message);
        msg = Utility.replaceColorCode(msg.replace("\\n", "\n"));
        getServer().broadcastMessage(msg);

        if ( ircbot != null ) {
            // IRC連携状態なら、IRCにも発言する
            msg = IRCColor.convRES2IRC(message.replace("\\n", " "));
            ircbot.sendMessage(msg);
        }
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
     * @return チャットBOTプラグインのインスタンス
     */
    public static MintChatBot getInstance() {
        if ( instance == null ) {
            instance = (MintChatBot)Bukkit.getPluginManager().getPlugin("MintChatBot");
        }
        return instance;
    }

    /**
     * @return チャットBOTプラグインのJarファイル
     */
    public static File getJarFile() {
        return getInstance().getFile();
    }

    /**
     * @return チャットBOTプラグインのコンフィグ
     */
    public ChatBotConfig getCBConfig() {
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
    public ResponceData getResponceData() {
        return responceData;
    }

    /**
     * @return IRCBot
     */
    protected IRCBot getIRCBot() {
        return ircbot;
    }

    /**
     * @return メッセージデータ
     */
    public Messages getMessages() {
        return messages;
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

        // メッセージデータのロード
        if ( messages == null ) {
            messages = new Messages(getFile(), getDataFolder());
        } else {
            messages.reloadData();
        }

        // TODO IRCBotは再接続すべきか？
    }

    /**
     * IRC設定が有効な状態かどうかを返す
     * @return IRC設定が有効かどうか
     */
    private boolean checkIRCConfig() {

        IRCBotConfig conf = config.getIrcbotConfig();
        return conf != null && conf.isEnabled()
                && conf.getServerHostname() != null && !conf.getServerHostname().equals("")
                && conf.getChannel() != null && !conf.getChannel().equals("");
    }
}
