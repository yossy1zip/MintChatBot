/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package com.github.ucchyocean.chatbot.irc;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.DisconnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.KickEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.QuitEvent;

import com.github.ucchyocean.chatbot.MintChatBot;
import com.github.ucchyocean.chatbot.URLResponcer;

/**
 * IRCBotのリスナー部分
 * @author ucchy
 */
public class IRCListener extends ListenerAdapter<PircBotX> implements Listener {

    private MintChatBot plugin;
    private IRCBotConfig config;
    private PircBotX bot;

    public IRCListener(IRCBotConfig config) {
        this.config = config;
        this.plugin = MintChatBot.getInstance();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // ========== IRC --> Minecraft ==========

    /**
     * @see org.pircbotx.hooks.ListenerAdapter#onConnect(org.pircbotx.hooks.events.ConnectEvent)
     */
    @Override
    public void onConnect(ConnectEvent<PircBotX> event) throws Exception {
        bot = event.getBot();
        String format = plugin.getMessages().getResponceIfMatch("irc_connect");
        if ( format == null ) return;
        String message = format
                .replace("%server", config.getServerHostname())
                .replace("%channel", config.getChannel());

        String botname = MintChatBot.getInstance().getCBConfig().getBotName();
        String resp = IRCColor.convRES2MC(
                MintChatBot.getInstance().getCBConfig().getResponceFormat()
                    .replace("%botName", botname)
                    .replace("%responce", message));
        Bukkit.broadcastMessage(resp);
    }

    /**
     * @see org.pircbotx.hooks.ListenerAdapter#onDisconnect(org.pircbotx.hooks.events.DisconnectEvent)
     */
    @Override
    public void onDisconnect(DisconnectEvent<PircBotX> event) throws Exception {
        String format = plugin.getMessages().getResponceIfMatch("irc_disconnect");
        if ( format == null ) return;
        String message = format
                .replace("%server", config.getServerHostname())
                .replace("%channel", config.getChannel());

        String botname = MintChatBot.getInstance().getCBConfig().getBotName();
        String resp = IRCColor.convRES2MC(
                MintChatBot.getInstance().getCBConfig().getResponceFormat()
                    .replace("%botName", botname)
                    .replace("%responce", message));
        Bukkit.broadcastMessage(resp);
    }

    /**
     * @see org.pircbotx.hooks.ListenerAdapter#onJoin(org.pircbotx.hooks.events.JoinEvent)
     */
    @Override
    public void onJoin(JoinEvent<PircBotX> event) throws Exception {
        if ( event.getUser().getNick().equals(bot.getNick()) ) return;
        String format = plugin.getMessages().getResponceIfMatch("irc_join");
        if ( format != null ) {
            String message = IRCColor.convRES2MC(format.replace("%name", event.getUser().getNick()));
            Bukkit.broadcastMessage(message);
        }

        // 必要に応じて、サーバー参加応答を返す
        if ( config.isResponceJoinServer() ) {
            format = plugin.getMessages().getResponceIfMatch("joinResponce");
            if ( format != null ) {
                String message = IRCColor.convRES2IRC(format.replace("%player", event.getUser().getNick()));
                plugin.say(message);
            }
        }
    }

    /**
     * @see org.pircbotx.hooks.ListenerAdapter#onKick(org.pircbotx.hooks.events.KickEvent)
     */
    @Override
    public void onKick(KickEvent<PircBotX> event) throws Exception {
//        if ( event.getUser().getNick().equals(bot.getNick()) ) return;
        String format = plugin.getMessages().getResponceIfMatch("irc_kick");
        if ( format == null ) return;
        String message = IRCColor.convRES2MC(format
                .replace("%name", event.getRecipient().getNick())
                .replace("%reason", event.getReason())
                .replace("%kicker", event.getUser().getNick()));
        Bukkit.broadcastMessage(message);
    }

    /**
     * @see org.pircbotx.hooks.ListenerAdapter#onPart(org.pircbotx.hooks.events.PartEvent)
     */
    @Override
    public void onPart(PartEvent<PircBotX> event) throws Exception {
        if ( event.getUser().getNick().equals(bot.getNick()) ) return;
        String format = plugin.getMessages().getResponceIfMatch("irc_part");
        if ( format == null ) return;
        String message = IRCColor.convRES2MC(
                format.replace("%name", event.getUser().getNick()).replace("%reason", event.getReason()));
        Bukkit.broadcastMessage(message);
    }

    /**
     * @see org.pircbotx.hooks.ListenerAdapter#onQuit(org.pircbotx.hooks.events.QuitEvent)
     */
    @Override
    public void onQuit(QuitEvent<PircBotX> event) throws Exception {
        if ( event.getUser().getNick().equals(bot.getNick()) ) return;
        String format = plugin.getMessages().getResponceIfMatch("irc_quit");
        if ( format == null ) return;
        String message = IRCColor.convRES2MC(
                format.replace("%name", event.getUser().getNick()).replace("%reason", event.getReason()));
        Bukkit.broadcastMessage(message);
    }

    /**
     * @see org.pircbotx.hooks.ListenerAdapter#onMessage(org.pircbotx.hooks.events.MessageEvent)
     */
    @Override
    public void onMessage(MessageEvent<PircBotX> event) throws Exception {
        if ( event.getUser().getNick().equals(bot.getNick()) ) return;
        String format = plugin.getMessages().getResponceIfMatch("irc_chat");
        if ( format != null ) {
            String message = IRCColor.convRES2MC(
                    format.replace("%name", event.getUser().getNick()).replace("%message",
                            IRCColor.convIRC2MC(event.getMessage())));
            Bukkit.broadcastMessage(message);
        }

        // 必要に応じて、自動応答を返す
        if ( config.isResponceChat() ) {
            String responce = plugin.getResponceData().getResponceIfMatch(
                    event.getMessage(), event.getUser().getNick());
            if ( responce != null ) {
                plugin.say(responce);
            }
        }

        // 必要に応じて、URL応答を返す
        if ( config.isGetURLTitle() && URLResponcer.containsURL(event.getMessage()) ) {
            URLResponcer resp = new URLResponcer(event.getMessage(), event.getUser().getNick());
            String responce = resp.getResponce();
            if ( responce != null ) {
                plugin.say(responce);
            }
        }
    }

    // ========== Minecraft --> IRC ==========

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if ( bot == null ) return;
        String format = plugin.getMessages().getResponceIfMatch("minecraft_chat");
        if ( format == null ) return;
        String message = IRCColor.convRES2IRC(
                format.replace("%name", event.getPlayer().getName()).replace("%message", event.getMessage()));
        bot.sendIRC().message(config.getChannel(), message);
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if ( bot == null ) return;
        String format = plugin.getMessages().getResponceIfMatch("minecraft_join");
        if ( format == null ) return;
        String message = IRCColor.convRES2IRC(format.replace("%name", event.getPlayer().getName()));
        bot.sendIRC().message(config.getChannel(), message);
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if ( bot == null ) return;
        String format = plugin.getMessages().getResponceIfMatch("minecraft_quit");
        if ( format == null ) return;
        String message = IRCColor.convRES2IRC(format.replace("%name", event.getPlayer().getName()));
        bot.sendIRC().message(config.getChannel(), message);
    }

    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onPlayerKick(PlayerKickEvent event) {
        if ( bot == null ) return;
        String format = plugin.getMessages().getResponceIfMatch("minecraft_kick");
        if ( format == null ) return;
        String message = IRCColor.convRES2IRC(format
                .replace("%name", event.getPlayer().getName())
                .replace("%reason", event.getReason()));
        bot.sendIRC().message(config.getChannel(), message);
    }

    // ========== LunaChat --> IRC ==========

    public void onLunaChat(String name, String message) {
        if ( bot == null ) return;
        String format = plugin.getMessages().getResponceIfMatch("minecraft_chat");
        if ( format == null ) return;
        String msg = IRCColor.convRES2IRC(
                format.replace("%name", name).replace("%message", message));
        bot.sendIRC().message(config.getChannel(), msg);
    }
}
