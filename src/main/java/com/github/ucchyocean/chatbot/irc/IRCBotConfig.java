/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package com.github.ucchyocean.chatbot.irc;

import java.nio.charset.Charset;

import org.bukkit.configuration.ConfigurationSection;

/**
 * IRCBotのコンフィグファイル
 * @author ucchy
 */
public class IRCBotConfig {

    private static final Charset DEFAULT_ENCODE = Charset.forName("ISO-2022-JP");

    /** IRC連携を利用するかどうか。 */
    private boolean enabled;

    /** IRCのチャットに対する自動応答をするかどうか。 */
    private boolean responceChat;

    /** IRCのチャンネル参加に対する自動応答をするかどうか。 */
    private boolean responceJoinServer;

    /** IRCにURLを含んだチャット発言がされたときに、URL先のタイトルを取得するかどうか。 */
    private boolean getURLTitle;

//    /** IRCに対して時報を使用するかどうか。 */
//    private boolean timeSignals;
//
//    /** IRCに対してアラームを使用するかどうか。 */
//    private boolean alermSignals;

    /** 接続先のIRCサーバー */
    private String serverHostname;

    /** 接続先のIRCサーバーのパスワード */
    private String serverPassword;

    /** 接続先のIRCサーバーのポート番号 */
    private int serverPort;

    /** 接続先のIRCチャンネル */
    private String channel;

    /** BOTのニックネーム */
    private String nickname;

    /** BOTのニックネームのパスワード */
    private String nickservPassword;

    /** 接続先のIRCサーバーの文字コード */
    private String encoding;

    /** IRCサーバーから切断するときのメッセージ */
    private String quitMessage;

    /**
     * コンストラクタ
     */
    private IRCBotConfig() {
    }

    public boolean isEnabled() {
        return enabled;
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

//    public boolean isTimeSignals() {
//        return timeSignals;
//    }
//
//    public boolean isAlermSignals() {
//        return alermSignals;
//    }

    public String getServerHostname() {
        return serverHostname;
    }

    public String getServerPassword() {
        return serverPassword;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getChannel() {
        return channel;
    }

    public String getNickname() {
        return nickname;
    }

    public String getNickservPassword() {
        return nickservPassword;
    }

    public Charset getEncoding() {
        if ( encoding != null && Charset.isSupported(encoding) ) {
            return Charset.forName(encoding);
        } else {
            return DEFAULT_ENCODE;
        }
    }

    public String getQuitMessage() {
        return quitMessage;
    }

    public static IRCBotConfig getConfigFromSection(ConfigurationSection section) {

        IRCBotConfig config = new IRCBotConfig();

        if ( section == null ) {
            return config;
        }

        config.enabled = section.getBoolean("enabled", false);
        config.responceChat = section.getBoolean("responceChat", true);
        config.responceJoinServer = section.getBoolean("responceJoinServer", true);
        config.getURLTitle = section.getBoolean("getURLTitle", true);
//        config.timeSignals = section.getBoolean("timeSignals", true);
//        config.alermSignals = section.getBoolean("alermSignals", true);

        config.serverHostname = section.getString("serverHostname");
        config.serverPassword = section.getString("serverPassword");
        config.serverPort = section.getInt("serverPort", 6667);
        config.channel = section.getString("channel");
        config.nickname = section.getString("nickname", "MintChan");
        config.nickservPassword = section.getString("nickservPassword");
        config.encoding = section.getString("encoding", "ISO-2022-JP");
        config.quitMessage = section.getString("quitMessage", "さよなら！またね！");

        return config;
    }
}
