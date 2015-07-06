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
    private boolean responceChat;
    private boolean responceJoinServer;
    private boolean getURLTitle;
    private boolean timeSignals;
    private boolean alermSignals;
    private boolean repeatSignals;
    private int responceDelayTicks;

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
        responceFormat = config.getString("responceFormat", "<&e%botName&f> %responce");
        responceChat = config.getBoolean("responceChat", true);
        responceJoinServer = config.getBoolean("responceJoinServer", true);
        getURLTitle = config.getBoolean("getURLTitle", true);
        timeSignals = config.getBoolean("timeSignals", true);
        alermSignals = config.getBoolean("alermSignals", true);
        repeatSignals = config.getBoolean("repeatSignals", true);
        responceDelayTicks = config.getInt("responceDelayTicks", 15);

        ircbotConfig = IRCBotConfig.getConfigFromSection(config.getConfigurationSection("irc"));
    }

    public String getBotName() {
        return botName;
    }

    public String getResponceFormat() {
        return responceFormat;
    }

    public boolean isResponceChat() {
        return responceChat;
    }

    public boolean isResponceJoinServer() {
        return responceJoinServer;
    }

    public boolean isGetURLTitle() {
        return getURLTitle;
    }

    public boolean isTimeSignals() {
        return timeSignals;
    }

    public boolean isAlermSignals() {
        return alermSignals;
    }

    public boolean isRepeatSignals() {
        return repeatSignals;
    }

    public int getResponceDelayTicks() {
        return responceDelayTicks;
    }

    public IRCBotConfig getIrcbotConfig() {
        return ircbotConfig;
    }
}
