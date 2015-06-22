/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package com.github.ucchyocean.chatbot.irc;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.KickEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.QuitEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

/**
 * IRCBotのリスナー部分
 * @author ucchy
 */
public class IRCListener extends ListenerAdapter<PircBotX> {

    /**
     * @see org.pircbotx.hooks.ListenerAdapter#onConnect(org.pircbotx.hooks.events.ConnectEvent)
     */
    @Override
    public void onConnect(ConnectEvent<PircBotX> event) throws Exception {
        // TODO 自動生成されたメソッド・スタブ
        super.onConnect(event);
    }

    /**
     * @see org.pircbotx.hooks.ListenerAdapter#onGenericMessage(org.pircbotx.hooks.types.GenericMessageEvent)
     */
    @Override
    public void onGenericMessage(GenericMessageEvent<PircBotX> event) throws Exception {
        // TODO 自動生成されたメソッド・スタブ
        super.onGenericMessage(event);
    }

    /**
     * @see org.pircbotx.hooks.ListenerAdapter#onJoin(org.pircbotx.hooks.events.JoinEvent)
     */
    @Override
    public void onJoin(JoinEvent<PircBotX> event) throws Exception {
        // TODO 自動生成されたメソッド・スタブ
        super.onJoin(event);
    }

    /**
     * @see org.pircbotx.hooks.ListenerAdapter#onKick(org.pircbotx.hooks.events.KickEvent)
     */
    @Override
    public void onKick(KickEvent<PircBotX> event) throws Exception {
        // TODO 自動生成されたメソッド・スタブ
        super.onKick(event);
    }

    /**
     * @see org.pircbotx.hooks.ListenerAdapter#onPart(org.pircbotx.hooks.events.PartEvent)
     */
    @Override
    public void onPart(PartEvent<PircBotX> event) throws Exception {
        // TODO 自動生成されたメソッド・スタブ
        super.onPart(event);
    }

    /**
     * @see org.pircbotx.hooks.ListenerAdapter#onQuit(org.pircbotx.hooks.events.QuitEvent)
     */
    @Override
    public void onQuit(QuitEvent<PircBotX> event) throws Exception {
        // TODO 自動生成されたメソッド・スタブ
        super.onQuit(event);
    }


}
