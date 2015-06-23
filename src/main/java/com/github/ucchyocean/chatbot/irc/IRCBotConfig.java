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

    /**
     * コンストラクタ
     * @param serverHostname 接続先のIRCサーバー
     * @param serverPassword 接続先のIRCサーバーのパスワード
     * @param serverPort 接続先のIRCサーバーのポート番号
     * @param channel 接続先のIRCチャンネル
     * @param nickname BOTのニックネーム
     * @param nickservPassword BOTのニックネームのパスワード
     * @param encoding 接続先のIRCサーバーの文字コード
     */
    public IRCBotConfig(
            String serverHostname, String serverPassword, int serverPort, String channel,
            String nickname, String nickservPassword, String encoding) {

        this.serverHostname = serverHostname;
        this.serverPassword = serverPassword;
        this.serverPort = serverPort;
        this.channel = channel;
        this.nickname = nickname;
        this.nickservPassword = nickservPassword;
        this.encoding = encoding;
    }

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

    public static IRCBotConfig getConfigFromSection(ConfigurationSection section) {
        String serverHostname = section.getString("serverHostname");
        String serverPassword = section.getString("serverPassword");
        int serverPort = section.getInt("serverPort", 6667);
        String channel = section.getString("channel");
        String nickname = section.getString("nickname");
        String nickservPassword = section.getString("nickservPassword");
        String encoding = section.getString("encoding");

        if ( serverHostname == null || serverHostname.equals("")
                || channel == null || channel.equals("") ) {
            return null;
        }

        return new IRCBotConfig(
                serverHostname, serverPassword, serverPort,
                channel, nickname, nickservPassword, encoding);
    }
}
