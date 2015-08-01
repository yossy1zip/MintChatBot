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

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.github.ucchyocean.chatbot.bridge.VaultChatBridge;

/**
 * キーワード置き換え処理クラス
 * @author ucchy
 */
public class KeywordReplacer {

    private static final String RESPONCE_TIME = "HH:mm:ss";
    private static final String RESPONCE_DATE = "yyyy/MM/dd E";

    private SimpleDateFormat time_format;
    private SimpleDateFormat date_format;
    private Pattern patternRandomGroup;

    /**
     * コンストラクタ
     */
    protected KeywordReplacer() {
        time_format = new SimpleDateFormat(RESPONCE_TIME);
        date_format = new SimpleDateFormat(RESPONCE_DATE, Locale.JAPAN);
        patternRandomGroup = Pattern.compile(".*\\(([^\\)]*)\\).*");
    }

    /**
     * キーワード置き換えを実行する
     * @param responce 応答のレスポンス元データ
     * @param player プレイヤー
     * @param key 応答のキー
     * @param chat チャット発言内容
     * @return 置き換え後の内容
     */
    protected String replace(String responce, Player player, String key, String chat, String altName) {

        if ( responce == null ) return null;

        if ( player != null ) {
            responce = replaceKeywords(responce, player);
        } else {
            responce = replaceKeywords(responce, altName);
        }

        if ( key != null && chat != null ) {
            responce = replaceMatchingGroups(responce, key, chat);
        }

        responce = replaceRandomGroup(responce);

        responce = responce.replace("（", "(").replace("）", ")");

        return responce;
    }

    /**
     * キーワード置き換えを実行する。URLタイトル用。
     * @param responce 応答のレスポンス元データ
     * @param player プレイヤー
     * @param title タイトル
     * @return 置き換え後の内容
     */
    protected String replaceForTitle(String responce, Player player, String title, String altName) {

        if ( responce == null ) return null;

        if ( player != null ) {
            responce = replaceKeywords(responce, player);
        } else {
            responce = replaceKeywords(responce, altName);
        }

        if ( title != null ) {
            responce = responce.replace("%title", title);
        } else {
            responce = responce.replace("%title", "");
        }

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
            responce = responce.replace("%player", player);
        } else {
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

        Matcher matcher = patternRandomGroup.matcher(source);

        if ( matcher.matches() ) {

            String org = matcher.group(1);
            String[] items = org.split("\\|");

            int index = (int)(Math.random() * items.length);
            return source.replace("(" + org + ")", items[index]);
        }

        return source;
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
}
