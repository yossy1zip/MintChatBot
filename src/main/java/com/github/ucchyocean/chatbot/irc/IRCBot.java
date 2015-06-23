/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package com.github.ucchyocean.chatbot.irc;

import java.io.IOException;

import org.bukkit.scheduler.BukkitRunnable;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;

import com.github.ucchyocean.chatbot.MintChatBot;

/**
 * IRCに接続して連携するためのBot
 * @author ucchy
 */
public class IRCBot extends BukkitRunnable {

    private PircBotX bot;
    private IRCBotConfig config;
    private IRCListener listener;

    /**
     * コンストラクタ
     * @param config IRCBotのコンフィグ
     */
    public IRCBot(IRCBotConfig config) {
        this.config = config;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {

        MintChatBot.getInstance().getLogger().info(
                String.format("IRC Connecting [%s:%d, %s]...",
                        config.getServerHostname(), config.getServerPort(), config.getChannel()));

        listener = new IRCListener(config);

        Configuration.Builder<PircBotX> builder = new Configuration.Builder<PircBotX>();
        builder.setName(config.getNickname());
        builder.setLogin("MintChatBot");
        builder.setAutoNickChange(true);
        builder.setServer(config.getServerHostname(), config.getServerPort());
        builder.addAutoJoinChannel(config.getChannel());
        builder.setVersion("MintChatBot");
        builder.setAutoReconnect(true);
        builder.addListener(listener);
        builder.setEncoding(config.getEncoding());

        if ( config.getServerPassword() != null && !config.getServerPassword().equals("") ) {
            builder.setServerPassword(config.getServerPassword());
        }
        if ( config.getNickservPassword() != null && !config.getNickservPassword().equals("") ) {
            builder.setNickservPassword(config.getNickservPassword());
        }

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
        this.runTaskAsynchronously(MintChatBot.getInstance());
    }

    /**
     * LunaChatからIRCにメッセージを送信する。
     * @param name
     * @param message
     */
    public void sendLunaChatMessage(String name, String message) {
        listener.onLunaChat(name, message);
    }

    /**
     * IRCから切断する。
     * @param message
     */
    public void disconnect(String message) {
        bot.stopBotReconnect();
        this.cancel();
    }

    /**
     * IRCに接続しているかどうか確認する。
     * @return
     */
    public boolean isConnected() {
        return bot.isConnected();
    }
}