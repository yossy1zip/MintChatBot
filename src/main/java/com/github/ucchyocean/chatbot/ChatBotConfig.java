/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2014
 */
package com.github.ucchyocean.chatbot;

import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;

import com.github.ucchyocean.chatbot.irc.IRCBotConfig;

/**
 * ChatBotのコンフィグ設定
 * @author ucchy
 */
public class ChatBotConfig {

    private String botName;
    private String responceFormat;
    private boolean getURLTitle;
    private String getURLTitleSuccess;
    private String getURLTitleFail;
    private String getURLTitleNotFound;
    private String joinResponce;
    private String firstJoinResponce;
    private boolean ircEnabled;
    private IRCBotConfig ircbotConfig;

    private File jarFile;
    private File dataFolder;

    /**
     * コンストラクタ
     * @param jarFile
     * @param dataFolder
     */
    public ChatBotConfig(File jarFile, File dataFolder) {

        this.jarFile = jarFile;
        this.dataFolder = dataFolder;

        reloadConfig();
    }

    /**
     * config.yml を再読み込みする
     */
    public void reloadConfig() {

        if ( !dataFolder.exists() ) {
            dataFolder.mkdirs();
        }

        File configFile = new File(dataFolder, "config.yml");
        if ( !configFile.exists() ) {
            Utility.copyFileFromJar(jarFile, configFile, "config_ja.yml");
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        botName = config.getString("botName", "ミントちゃん");
        responceFormat = config.getString("responceFormat",
                "<&e%botName&f> %responce");
        getURLTitle = config.getBoolean("getURLTitle", true);
        getURLTitleSuccess = config.getString("getURLTitleSuccess");
        getURLTitleFail = config.getString("getURLTitleFail");
        getURLTitleNotFound = config.getString("getURLTitleNotFound");
        joinResponce = config.getString("joinResponce");
        firstJoinResponce = config.getString("firstJoinResponce");
        ircEnabled = config.getBoolean("irc.enabled", false);
        ircbotConfig = IRCBotConfig.getConfigFromSection(config.getConfigurationSection("irc"));
    }

    /**
     * BOTの表示名
     * @return
     */
    public String getBotName() {
        return botName;
    }

    /**
     * オートレスポンスのフォーマット
     * @return
     */
    public String getResponceFormat() {
        return responceFormat;
    }

    /**
     * URLレスポンスを使用するかどうか
     * @return
     */
    public boolean isGetURLTitle() {
        return getURLTitle;
    }

    /**
     * URLレスポンスが成功した時の、返信フォーマット
     * @return
     */
    public String getGetURLTitleSuccess() {
        return getURLTitleSuccess;
    }

    /**
     * URLレスポンスが失敗した時の、返信フォーマット
     * @return
     */
    public String getGetURLTitleFail() {
        return getURLTitleFail;
    }

    /**
     * URLレスポンスが失敗した時の、返信フォーマット
     * @return
     */
    public String getGetURLTitleNotFound() {
        return getURLTitleNotFound;
    }

    /**
     * サーバー参加レスポンス
     * @return
     */
    public String getJoinResponce() {
        return joinResponce;
    }

    /**
     * サーバー初参加レスポンス
     * @return
     */
    public String getFirstJoinResponce() {
        return firstJoinResponce;
    }

    /**
     * IRC連携を有効にするかどうか
     * @return
     */
    public boolean isIrcEnabled() {
        return ircEnabled;
    }

    /**
     * IRC連携設定
     * @return
     */
    public IRCBotConfig getIrcBotConfig() {
        return ircbotConfig;
    }
}
