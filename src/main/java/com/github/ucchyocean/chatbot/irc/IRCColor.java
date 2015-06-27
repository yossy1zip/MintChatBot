/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package com.github.ucchyocean.chatbot.irc;

import org.bukkit.ChatColor;

/**
 * IRCとMinecraftの色や制御コードの相互変換
 * @author ucchy
 */
public enum IRCColor {

    // Colors
    WHITE       ("\u000300", "f"),
    BLACK       ("\u000301", "0"),
    DARK_BLUE   ("\u000302", "1"),
    GREEN       ("\u000303", "2"),
    RED         ("\u000304", "c"),
    BROWN       ("\u000305", "4"),
    PURPLE      ("\u000306", "5"),
    ORANGE      ("\u000307", "6"),
    YELLOW      ("\u000308", "e"),
    LIGHT_GREEN ("\u000309", "a"),
    TEAL        ("\u000310", "3"),
    LIGHT_CYAN  ("\u000311", "b"),
    LIGHT_BLUE  ("\u000312", "9"),
    PINK        ("\u000313", "d"),
    GRAY        ("\u000314", "7"),
    LIGHT_GRAY  ("\u000315", "8"),

    // Controls
    BOLD        ("\u0002",   "l"),
    RANDOM      ("",         "k"),
    MAGIC       ("",         "k"),
    STRIKE      ("",         "m"),
    UNDERLINE   ("\u001f",   "n"),
    ITALIC      ("\u0016",   "o"),
    REVERSE     ("\u0016",   "o"),
    NORMAL      ("\u000f",   "r"),
    RESET       ("\u000f",   "r"),

    // Extra
    C_RESET     ("\u0003",   "r");

    private static final String SEC = "\u00A7";

    private final String irc;
    private final String mc;

    /**
     * コンストラクタ
     * @param irc
     * @param mc
     */
    private IRCColor(String irc, String mc) {
        this.irc = irc;
        this.mc = mc;
    }

    /**
     * 指定されたMinecraft文字列を、IRC文字列に変換する。
     * @param source Minecraft文字列
     * @return IRC文字列
     */
    public static String convMC2IRC(String source) {
        if ( source == null ) return null;
        String message = source;
        for ( IRCColor c : values() ) {
            if ( c.mc.equals("") ) continue;
            message = message.replace(SEC + c.mc, c.irc);
        }

        // IRCの標準文字色 白 は、Minecraftだと見にくいので、黒 に変更する。
        message = message.replace(WHITE.irc, BLACK.irc);

        return message;
    }

    /**
     * 指定されたIRC文字列を、Minecraft文字列に変換する。
     * @param source IRC文字列
     * @return Minecraft文字列
     */
    public static String convIRC2MC(String source) {
        if ( source == null ) return null;
        String message = source;
        for ( IRCColor c : values() ) {
            if ( c.irc.equals("") ) continue;
            message = message.replace(c.irc, SEC + c.mc);
        }

        // IRCの標準文字色 黒 は、Minecraftだと見にくいので、白 に変更する。
        message = message.replace(SEC + BLACK.mc, SEC + WHITE.mc);

        return message;
    }

    /**
     * 指定されたリソース文字列を、IRC文字列に変換する。
     * @param source リソース文字列
     * @return IRC文字列
     */
    public static String convRES2IRC(String source) {
        return convMC2IRC(convRES2MC(source));
    }

    /**
     * 指定されたリソース文字列を、Minecraft文字列に変換する。
     * @param source リソース文字列
     * @return Minecraft文字列
     */
    public static String convRES2MC(String source) {
        if ( source == null ) return null;
        return ChatColor.translateAlternateColorCodes('&', source);
    }
}
