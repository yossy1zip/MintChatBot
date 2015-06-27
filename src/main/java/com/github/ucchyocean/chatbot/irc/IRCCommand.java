/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package com.github.ucchyocean.chatbot.irc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import com.github.ucchyocean.chatbot.MintChatBot;

/**
 * IRCBotコマンド
 * @author ucchy
 */
public class IRCCommand implements TabExecutor {

    private static final String PERMISSION_PREFIX = "chatbot.irc.";
    private static final String[] COMMANDS = new String[]{
        "connect", "disconnect", "reconnect", "message", "op", "kick"
    };

    /**
     * コマンドが実行された時に呼び出されるメソッド
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if ( args.length == 0 ) {
            sender.sendMessage("コマンドの指定が正しくありません。");
            return false;
        }

        boolean isValidCommand = false;
        for ( String c : COMMANDS ) {
            if ( c.equalsIgnoreCase(args[0]) ) {
                isValidCommand = true;
            }
        }
        if ( !isValidCommand ) {
            sender.sendMessage("コマンドの指定が正しくありません。");
            return false;
        }

        String node = PERMISSION_PREFIX + args[0].toLowerCase();
        if ( !sender.hasPermission(node) ) {
            sender.sendMessage("パーミッション " + node + " が無いため実行できません。");
            return false;
        }

        IRCBot bot = MintChatBot.getInstance().getIRCBot();

        if ( args[0].equalsIgnoreCase("connect") ) {

            if ( bot != null ) {
                if ( bot.isConnected() ) {
                    if ( bot.isJoinedChannel() ) {
                        sender.sendMessage("既にIRCチャンネルに入室しています。");
                        return true;
                    }

                    bot.joinChannel();
                    sender.sendMessage("IRCチャンネルに入室しました。");
                    return true;
                }

                sender.sendMessage("既に接続処理中です。");
                return true;
            }

            bot = MintChatBot.getInstance().connectIRCBot();
            if ( bot != null ) {
                sender.sendMessage("IRCに接続しています...");
                return true;
            } else {
                sender.sendMessage("IRC接続設定がオンになっていません。");
                return true;
            }

        } else if ( args[0].equalsIgnoreCase("disconnect") ) {

            if ( bot == null || !bot.isConnected() ) {
                sender.sendMessage("既にIRCから切断しています。");
                return true;
            }

            MintChatBot.getInstance().disconnectIRCBot();
            sender.sendMessage("IRCから切断しました。");
            return true;

        } else if ( args[0].equalsIgnoreCase("reconnect") ) {

            if ( bot == null || !bot.isConnected() ) {
                sender.sendMessage("IRCに接続していません。");
                return true;
            }

            MintChatBot.getInstance().disconnectIRCBot();
            bot = MintChatBot.getInstance().connectIRCBot();
            if ( bot != null ) {
                sender.sendMessage("IRCに接続しています...");
                return true;
            } else {
                sender.sendMessage("IRC接続設定がオフになりました。");
                return true;
            }

        } else if ( args[0].equalsIgnoreCase("message") ) {

            if ( bot == null ) {
                sender.sendMessage("IRC連携が行われていません。");
                return true;
            }

            if ( args.length <= 1 ) {
                sender.sendMessage("メッセージを指定してください。");
                return true;
            }

            if ( !bot.isConnected() ) {
                sender.sendMessage("IRCに接続していません。");
                return true;
            }

            String message = args[1];
            bot.sendMessage(message);

            String format = MintChatBot.getInstance().getMessages().getResponceIfMatch("irc_chat");
            if ( format != null ) {
                String msg = IRCColor.convRES2MC(
                        format.replace("%name", bot.getBotNick()).replace("%message", message));
                Bukkit.broadcastMessage(msg);
            }

            return true;

        } else if ( args[0].equalsIgnoreCase("op") ) {

            if ( bot == null ) {
                sender.sendMessage("IRC連携が行われていません。");
                return true;
            }

            if ( !bot.isConnected() ) {
                sender.sendMessage("IRCに接続していません。");
                return true;
            }

            if ( !bot.hasOP() ) {
                sender.sendMessage("チャンネル管理者権限を持っていません。");
                return true;
            }

            String nick = "";
            if ( args.length >= 2 ) nick = args[1];
            if ( !bot.existUser(nick) ) {
                sender.sendMessage("指定されたユーザーが見つかりません。");
                return true;
            }

            bot.sendOperator(nick);
            sender.sendMessage(nick + "にチャンネル管理者権限を渡しました。");
            return true;

        } else if ( args[0].equalsIgnoreCase("kick") ) {

            if ( bot == null ) {
                sender.sendMessage("IRC連携が行われていません。");
                return true;
            }

            if ( !bot.isConnected() ) {
                sender.sendMessage("IRCに接続していません。");
                return true;
            }

            if ( !bot.hasOP() ) {
                sender.sendMessage("チャンネル管理者権限を持っていません。");
                return true;
            }

            String nick = "";
            if ( args.length >= 2 ) nick = args[1];
            if ( !bot.existUser(nick) ) {
                sender.sendMessage("指定されたユーザーが見つかりません。");
                return true;
            }

            String reason = null;
            if ( args.length >= 3 ) reason = args[2];

            bot.kick(nick, reason);
            sender.sendMessage(nick + "をチャンネルからキックしました。");
            return true;

        }

        sender.sendMessage("コマンドの指定が正しくありません。");
        return false;
    }

    /**
     * TABキーで補完が行われた時に呼び出されるメソッド
     * @see org.bukkit.command.TabCompleter#onTabComplete(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if ( args.length == 1 ) {
            // コマンド名で補完する
            String arg = args[0].toLowerCase();
            ArrayList<String> coms = new ArrayList<String>();
            for ( String c : COMMANDS ) {
                if ( c.startsWith(arg) && sender.hasPermission(PERMISSION_PREFIX + c) ) {
                    coms.add(c);
                }
            }
            return coms;
        }

        return null;
    }

}
