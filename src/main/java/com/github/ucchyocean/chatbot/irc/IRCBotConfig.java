/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package com.github.ucchyocean.chatbot.irc;

import java.nio.charset.Charset;

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
     * @param nickname BOTのニックネーム
     * @param nickservPassword BOTのニックネームのパスワード
     * @param encoding 接続先のIRCサーバーの文字コード
     */
    public IRCBotConfig(
            String serverHostname, String serverPassword, int serverPort, String nickname,
            String nickservPassword, String encoding) {
        
        this.serverHostname = serverHostname;
        this.serverPassword = serverPassword;
        this.serverPort = serverPort;
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

    public String getNickname() {
        return nickname;
    }

    public String getNickservPassword() {
        return nickservPassword;
    }

    public Charset getEncoding() {
        if ( Charset.isSupported(encoding) ) {
            return Charset.forName(encoding);
        } else {
            return DEFAULT_ENCODE;
        }
    }
}
