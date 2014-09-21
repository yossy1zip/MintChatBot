/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2014
 */
package com.github.ucchyocean.chatbot;

import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;

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
        getURLTitleSuccess = config.getString("getURLTitleSuccess",
                "%playerさんの貼ったURLは、\"%title\"というタイトルです。");
        getURLTitleFail = config.getString("getURLTitleFail",
                "%playerさんの貼ったURLは、タイトルが取得できませんでした…");
        getURLTitleNotFound = config.getString("getURLTitleNotFound",
                "%playerさんの貼ったURLは、接続できませんでした…");
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
}
