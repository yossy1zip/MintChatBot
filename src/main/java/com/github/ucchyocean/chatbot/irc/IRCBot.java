/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package com.github.ucchyocean.chatbot.irc;

import java.io.IOException;

import org.bukkit.scheduler.BukkitRunnable;
import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
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
     * 指定されたチャンネルに再入室する
     */
    public void joinChannel() {
        bot.sendIRC().joinChannel(config.getChannel());
    }

    /**
     * IRCにメッセージを送信する
     * @param message メッセージ
     */
    public void sendMessage(String message) {
        bot.sendIRC().message(config.getChannel(), message);
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
    public void disconnect() {
        bot.stopBotReconnect();
        bot.sendIRC().quitServer(config.getQuitMessage());
        this.cancel();
    }

    /**
     * IRCに接続しているかどうか確認する。
     * @return
     */
    public boolean isConnected() {
        return bot.isConnected() && !bot.getNick().equals("");
    }

    /**
     * IRCBotが、現在チャンネルOPを持っているかどうかを返す
     * @return チャンネルOPを持っているかどうか
     */
    public boolean hasOP() {
        if ( !isConnected() ) return false;
        Channel channel = bot.getUserChannelDao().getChannel(config.getChannel());
        if ( channel == null ) return false;
        return channel.isOp(bot.getUserBot());
    }

    /**
     * 指定したニックネームのユーザーが、チャンネルにいるかどうかを返す
     * @param nick ニックネーム
     * @return 指定したニックネームのユーザーがチャンネルにいるかどうか
     */
    public boolean existUser(String nick) {
        if ( !isConnected() ) return false;
        Channel channel = bot.getUserChannelDao().getChannel(config.getChannel());
        if ( channel == null ) return false;
        return getChannelUser(channel, nick) != null;
    }

    /**
     * 指定したニックネームのユーザーに、チャンネルのOPを与える
     * @param nick ニックネーム
     */
    public void sendOperator(String nick) {
        if ( !isConnected() ) return;
        Channel channel = bot.getUserChannelDao().getChannel(config.getChannel());
        if ( channel == null ) return;
        User target = getChannelUser(channel, nick);
        if ( target == null ) return;
        channel.send().op(target);
    }

    /**
     * 指定したニックネームのユーザーを、チャンネルからキックする
     * @param nick ニックネーム
     * @param reason キックの理由
     */
    public void kick(String nick, String reason) {
        if ( !isConnected() ) return;
        Channel channel = bot.getUserChannelDao().getChannel(config.getChannel());
        if ( channel == null ) return;
        User target = getChannelUser(channel, nick);
        if ( target == null ) return;
        if ( reason == null || reason.equals("") ) {
            channel.send().kick(target);
        } else {
            channel.send().kick(target, reason);
        }
    }

    /**
     * Botがチャンネルに接続しているかどうかを返す
     * @return チャンネルに接続しているかどうか
     */
    public boolean isJoinedChannel() {
        if ( !isConnected() ) return false;
        Channel channel = bot.getUserChannelDao().getChannel(config.getChannel());
        if ( channel == null ) return false;
        return (getChannelUser(channel, bot.getNick()) != null);
    }

    /**
     * 指定したチャンネルにいる、指定したニックネームのユーザーを取得する
     * @param channel チャンネル
     * @param nick ニックネーム
     * @return ユーザー、取得できない場合はnull
     */
    private static User getChannelUser(Channel channel, String nick) {
        for ( User u : channel.getUsers() ) {
            if ( u.getNick().equals(nick) ) {
                return u;
            }
        }
        return null;
    }

    /**
     * このBotのニックネームを返す
     * @return Botのニックネーム
     */
    protected String getBotNick() {
        return bot.getNick();
    }
}