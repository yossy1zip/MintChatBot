/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package com.github.ucchyocean.chatbot.irc;

import java.io.IOException;
import java.nio.charset.Charset;

import org.bukkit.scheduler.BukkitRunnable;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;

import com.github.ucchyocean.chatbot.ChatBot;

/**
 * IRCに接続して連携するためのBot
 * @author ucchy
 */
public class IRCBot extends BukkitRunnable {

    private PircBotX bot;

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {

        Charset jis = Charset.forName("ISO-2022-JP");

        Configuration.Builder<PircBotX> builder = new Configuration.Builder<PircBotX>();
        builder.setName("MintChan");
        builder.setLogin("BukkitChatBot");
        builder.setAutoNickChange(true);
        builder.setServer("irc.friend-chat.jp", 6665);
        builder.addAutoJoinChannel("#TestTest");
        builder.setVersion("BukkitChatBot");
        builder.setAutoReconnect(true);
        builder.addListener(new IRCListener());
        builder.setEncoding(jis);

        bot = new PircBotX(builder.buildConfiguration());
        try {
            bot.startBot();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IrcException e) {
            e.printStackTrace();
        }
    }

    /**
     * 与えられた設定でIRCサーバーに接続する
     */
    public void connect() {
        this.runTaskAsynchronously(ChatBot.getInstance());
    }

    /**
     * IRCにメッセージを送信する。
     * @param message
     */
    public void sendMessage(String message) {
        bot.sendIRC().message("#TestTest", message);
    }

    /**
     * IRCから切断する。
     * @param message
     */
    public void disconnect(String message) {
        bot.stopBotReconnect();
    }

    /**
     * IRCに接続しているかどうか確認する。
     * @return
     */
    public boolean isConnected() {
        return bot.isConnected();
    }
}