/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package com.github.ucchyocean.chatbot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.ucchyocean.chatbot.bridge.VaultChatBridge;

/**
 * キーワード置き換え処理クラス
 * @author ucchy
 */
public class KeywordReplacer {

    private static final String RESPONCE_TIME = "HH:mm:ss";
    private static final String RESPONCE_DATE = "yyyy/MM/dd E";
    private static final String REGEX_PATTERN_COMMAND =
            "(@command|@command_bypass)\\[([^\\]]+)\\]";

    private SimpleDateFormat time_format;
    private SimpleDateFormat date_format;
    private Pattern pattern_command;

    /**
     * コンストラクタ
     */
    protected KeywordReplacer() {
        time_format = new SimpleDateFormat(RESPONCE_TIME);
        date_format = new SimpleDateFormat(RESPONCE_DATE, Locale.JAPAN);
        pattern_command = Pattern.compile(REGEX_PATTERN_COMMAND);
    }

    /**
     * キーワード置き換えを実行する
     * @param responce 応答のレスポンス元データ
     * @param sender プレイヤー
     * @param key 応答のキー
     * @param chat チャット発言内容
     * @return 置き換え後の内容
     */
    protected String replace(String responce, CommandSender sender, String key, String chat, String altName) {

        if ( responce == null ) return null;

        if ( sender == null ) {
            responce = replaceKeywords(responce, altName);
        } else if ( sender instanceof Player ) {
            responce = replaceKeywords(responce, (Player)sender);
        } else {
            responce = replaceKeywords(responce, sender.getName());
        }

        if ( key != null && chat != null ) {
            responce = replaceMatchingGroups(responce, key, chat);
        }

        responce = replaceRandomGroup(responce);

        responce = runCommands(responce, sender);

        responce = responce.replace("（", "(").replace("）", ")");

        return responce;
    }

    /**
     * キーワード置き換えを実行する。URLタイトル用。
     * @param responce 応答のレスポンス元データ
     * @param sender プレイヤー
     * @param title タイトル
     * @return 置き換え後の内容
     */
    protected String replaceForTitle(String responce, CommandSender sender, String title) {

        if ( responce == null ) return null;

        if ( sender == null ) {
            responce = replaceKeywords(responce, "");
        } else if ( sender instanceof Player ) {
            responce = replaceKeywords(responce, (Player)sender);
        } else {
            responce = replaceKeywords(responce, sender.getName());
        }

        if ( title != null ) {
            responce = responce.replace("%title", title);
        } else {
            responce = responce.replace("%title", "");
        }

        responce = replaceRandomGroup(responce);

        responce = runCommands(responce, sender);

        responce = responce.replace("（", "(").replace("）", ")");

        return responce;
    }

    /**
     * キーワード置き換えを実行する。URLタイトル用。
     * @param responce 応答のレスポンス元データ
     * @param altName プレイヤー名
     * @param title タイトル
     * @return 置き換え後の内容
     */
    protected String replaceForTitle(String responce, String altName, String title) {

        if ( responce == null ) return null;

        responce = replaceKeywords(responce, altName);

        if ( title != null ) {
            responce = responce.replace("%title", title);
        } else {
            responce = responce.replace("%title", "");
        }

        responce = replaceRandomGroup(responce);

        responce = responce.replace("（", "(").replace("）", ")");

        return responce;
    }

    /**
     * キーワード置き換えを実行する。他の媒体（IRCやdynmap-webなど）からの実行用。
     * @param responce 応答のレスポンス元データ
     * @param sender 発言者名
     * @param key 応答のキー
     * @param chat チャット発言内容
     * @return 置き換え後の内容
     */
    protected String replaceForOtherSource(String responce, String sender, String key, String chat) {

        if ( responce == null ) return null;

        responce = replaceKeywords(responce, sender);

        if ( key != null && chat != null ) {
            responce = replaceMatchingGroups(responce, key, chat);
        }

        responce = replaceRandomGroup(responce);

        responce = runCommands(responce, null);

        responce = responce.replace("（", "(").replace("）", ")");

        return responce;
    }

    /**
     * 指定された文字列に含まれるキーワードを置き換える
     * @param source 元の文字列
     * @param player プレイヤー、不要ならnullで良い。
     * @return キーワード置き換え済みの文字列
     */
    private String replaceKeywords(String source, Player player) {

        String responce = source;
        VaultChatBridge vaultchat = MintChatBot.getInstance().getVaultChat();

        if ( responce.contains("%playerName") ) {
            String name = "";
            if ( player != null ) {
                name = player.getName();
            }
            responce = responce.replace("%playerName", name);
        }

        if ( responce.contains("%player") ) {
            String name = "";
            if ( player != null ) {
                name = player.getDisplayName() + ChatColor.RESET;
            }
            responce = responce.replace("%player", name);
        }

        if ( responce.contains("%prefix") ) {
            String prefix = "";
            if ( vaultchat != null && player != null ) {
                prefix = vaultchat.getPlayerPrefix(player);
            }
            responce = responce.replace("%prefix", prefix);
        }

        if ( responce.contains("%suffix") ) {
            String suffix = "";
            if ( vaultchat != null && player != null ) {
                suffix = vaultchat.getPlayerSuffix(player);
            }
            responce = responce.replace("%suffix", suffix);
        }

        if ( responce.contains("%time") ) {
            String time = time_format.format(new Date());
            responce = responce.replace("%time", time);
        }

        if ( responce.contains("%date") ) {
            String date = date_format.format(new Date());
            responce = responce.replace("%date", date);
        }

        if ( responce.contains("%random_player") ) {
            responce = responce.replace("%random_player", getRandomPlayerName());
        }

        return responce;
    }

    /**
     * 指定された文字列に含まれるキーワードを置き換える
     * @param source 元の文字列
     * @param player プレイヤー、不要ならnullで良い。
     * @return キーワード置き換え済みの文字列
     */
    private String replaceKeywords(String source, String player) {

        String responce = source;

        if ( player != null ) {
            responce = responce.replace("%playerName", player);
            responce = responce.replace("%player", player);
        } else {
            responce = responce.replace("%playerName", "");
            responce = responce.replace("%player", "");
        }
        responce = responce.replace("%prefix", "");
        responce = responce.replace("%suffix", "");

        if ( responce.contains("%time") ) {
            String time = time_format.format(new Date());
            responce = responce.replace("%time", time);
        }

        if ( responce.contains("%date") ) {
            String date = date_format.format(new Date());
            responce = responce.replace("%date", date);
        }

        if ( responce.contains("%random_player") ) {
            responce = responce.replace("%random_player", getRandomPlayerName());
        }

        return responce;
    }

    /**
     * 正規表現のマッチンググループを置き換える
     * @param source 元の文字列（＝応答内容）
     * @param key 正規表現パターン
     * @param org 正規表現にマッチさせる内容（＝元のチャット発言内容）
     * @return 置き換えられた文字列
     */
    private String replaceMatchingGroups(String source, String key, String org) {

        String responce = source;

        if ( responce.matches(".*%[1-9].*") ) {

            Pattern pattern = Pattern.compile(key);
            Matcher matcher = pattern.matcher(org);
            boolean isMatch = matcher.matches();
            for ( int index = 1; index <= 9; index++ ) {
                String groupkey = "%" + index;
                if ( !responce.contains(groupkey) ) {
                    continue;
                }
                if ( !isMatch || matcher.groupCount() < index ) {
                    responce = responce.replace(groupkey, "");
                } else {
                    responce = responce.replace(groupkey, matcher.group(index));
                }
            }
        }

        return responce;
    }

    /**
     * ランダムグループが設定されている場合に、ランダムに選択して置き換えして返します。
     * @param source 元の文字列
     * @return 置き換えられた文字列
     */
    private String replaceRandomGroup(String source) {

        if ( source == null ) return null;

        String responce = source;

        while (true) {

            int fromIndex = responce.indexOf("(");
            if ( fromIndex < 0 ) {
                break;
            }
            int toIndex = responce.indexOf(")", fromIndex);
            if ( toIndex < 0 ) {
                break;
            }

            String parts = responce.substring(fromIndex, toIndex + 1);
            String[] items = parts.substring(1, parts.length() -1).split("\\|");

            int index = (int)(Math.random() * items.length);
            responce = responce.replace(parts, items[index]);
        }

        return responce;
    }

    /**
     * ログインしているプレイヤーからランダムに選出して、そのプレイヤー名を返す。
     * @return 選出されたプレイヤー名
     */
    private String getRandomPlayerName() {
        ArrayList<Player> players = Utility.getOnlinePlayers();
        if ( players.size() == 0 ) return "";
        int index = (int)(Math.random() * players.size());
        return players.get(index).getName();
    }

    /**
     * コマンド実行キーワードが含まれている時に、コマンドを実行する。
     * @param source
     * @param sender
     * @return
     */
    private String runCommands(String source, final CommandSender sender) {

        Matcher matcher = pattern_command.matcher(source);
        while ( matcher.find() ) {

            final boolean isBypass = matcher.group(1).equals("@command_bypass");
            final String command = matcher.group(2).startsWith("/") ?
                    matcher.group(2).substring(1) : matcher.group(2);
            new BukkitRunnable() {
                public void run() {
                    if ( !isBypass && sender != null ) {
                        Bukkit.dispatchCommand(sender, command);
                    } else if ( sender != null ) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    }
                }
            }.runTaskLater(MintChatBot.getInstance(),
                    MintChatBot.getInstance().getCBConfig().getResponceDelayTicks() + 2);

            source = source.replace(matcher.group(0), "");
            matcher = pattern_command.matcher(source);
        }
        return source;
    }
}
