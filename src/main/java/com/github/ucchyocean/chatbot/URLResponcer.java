/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2014
 */
package com.github.ucchyocean.chatbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * URLを含んだチャット発言に対するレスポンスを行うクラス
 * @author ucchy
 */
public class URLResponcer extends BukkitRunnable {

    private static final String REGEX_URL = ".*(https?://[\\w/:%#\\$&\\?\\(\\)~\\.=\\+\\-]+).*";
    private static final String REGEX_TITLE = ".*<title>([^<]*)</title>.*";
    private static final String REGEX_CHARSET = ".*charset=\"?([^\"]*)\".*";

    private String source;
    private Player player;
    private String responceSuccessFormat;
    private String responceFailFormat;
    private String responceNotFoundFormat;
    private Pattern urlPattern;
    private Pattern titlePattern;
    private Pattern charsetPattern;

    /**
     * コンストラクタ
     * @param source
     * @param player
     * @param responceSuccessFormat
     * @param responceFailFormat
     * @param responceNotFoundFormat
     */
    public URLResponcer(
            String source,
            Player player,
            String responceSuccessFormat,
            String responceFailFormat,
            String responceNotFoundFormat) {

        this.source = source;
        this.player = player;
        this.responceSuccessFormat = responceSuccessFormat;
        this.responceFailFormat = responceFailFormat;
        this.responceNotFoundFormat = responceNotFoundFormat;

        urlPattern = Pattern.compile(REGEX_URL);
        titlePattern = Pattern.compile(REGEX_TITLE, Pattern.CASE_INSENSITIVE);
        charsetPattern = Pattern.compile(REGEX_CHARSET, Pattern.CASE_INSENSITIVE);
    }

    /**
     * 文字列にURLを含むかどうかを確認する
     * @param source
     * @return
     */
    public static boolean containsURL(String source) {
        return source.matches(REGEX_URL);
    }

    /**
     * レスポンスを行う
     * @return
     */
    public String responce() {

        Matcher matcher = urlPattern.matcher(source);

        if ( !matcher.matches() ) {
            return null;
        }

        String url = matcher.group(1);
        String title = getURLTitle(url);
        String playerName = (player != null) ? player.getName() : "";

        String responce;
        if ( title == null ) {
            responce = new String(responceNotFoundFormat);
        } else if ( title.equals("") ) {
            responce = new String(responceFailFormat);
        } else {
            responce = new String(responceSuccessFormat);
        }

        responce = responce.replace("%player", playerName);
        if ( title != null ) {
            responce = responce.replace("%title", title);
        }
        return responce;
    }

    /**
     * 指定されたURLから、タイトル要素を取得する
     * @param urlStr URL
     * @return タイトル
     */
    private String getURLTitle(String urlStr) {

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(urlStr);

            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");

            if ( connection.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM ) {
                // 301 リダイレクトが指定された、リダイレクト先に再接続する

                urlStr = connection.getHeaderFields().get("Location").get(0);
                connection.disconnect();
                url = new URL(urlStr);
                connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
            }

            if ( connection.getResponseCode() == HttpURLConnection.HTTP_OK ) {
                // 200 正常に接続した

                String charset = getCharsetFromContentType(connection.getContentType());

                InputStreamReader isr = new InputStreamReader(
                        connection.getInputStream(), charset);

                reader = new BufferedReader(isr);

                String line;

                while ( (line = reader.readLine()) != null ) {

                    if ( charset.equals("JISAutoDetect") ) {
                        // エンコードがまだ確定していない場合は、
                        // 本文中にエンコードが指定されているかどうかを確認する

                        Matcher cm = charsetPattern.matcher(line);
                        if ( cm.matches() ) {
                            // charset の指定が見つかったので、接続しなおして読みなおす

                            charset = cm.group(1);
                            reader.close();
                            connection.disconnect();
                            connection = (HttpURLConnection)url.openConnection();

                            reader = new BufferedReader(new InputStreamReader(
                                    connection.getInputStream(), charset));
                            continue;
                        }
                    }

                    Matcher matcher = titlePattern.matcher(line);
                    if ( matcher.matches() ) {
                        // タイトルの取得に成功
                        return reverseSanitize(matcher.group(1));
                    }
                }

                // レスポンスは200だが、タイトルの取得に失敗
                return "";

            }

        } catch (IOException e) {
            //e.printStackTrace();
        } finally {
            if ( reader != null ) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
            if ( connection != null ) {
                connection.disconnect();
            }
        }

        // レスポンスが404など
        return null;
    }

    /**
     * charset属性を取得する
     * @param contentType
     * @return 取得されたcharset、取得できない場合はJISAutoDetectが返される
     */
    private String getCharsetFromContentType(String contentType) {

        if ( contentType == null || !contentType.contains("charset=") ) {
            return "JISAutoDetect";
        }

        String charset = contentType.substring(
                contentType.indexOf("charset=") + "charset=".length()).trim();
        if ( charset.contains(" ") ) {
            charset = charset.substring(0, charset.indexOf(" "));
        }
        if ( charset.contains(";") ) {
            charset = charset.substring(0, charset.indexOf(";"));
        }

        return charset;
    }

    /**
     * シリアライズされた文字を元に戻す
     * @param str
     * @return
     */
    private String reverseSanitize(String str) {

        if ( str == null ) {
            return null;
        }

        str = str.replace("&quot;", "\"");
        str = str.replace("&gt;"  , ">" );
        str = str.replace("&lt;"  , "<" );
        str = str.replace("&amp;" , "&" );
        str = str.replace("&bull;" , "•" );
        return str;
     }

    @Override
    public void run() {

        String responce = responce();
        if ( responce != null ) {
            Bukkit.broadcastMessage(responce);
        }
    }

    // デバッグ用エントリ
    public static void main(String[] args) {

        String[] testees = new String[]{
                "http://my-cra.mydns.jp/rule.html",
                "https://twitter.com",
                "https://www.jpcert.or.jp/at/2014/at140002.html",
                "https://bukkitwiki.jp/Developer/%E3%82%B5%E3%83%B3%E3%83%97%E3%83%AB%E9%9B%86",
        };

        URLResponcer resp = new URLResponcer(null, null, null, null, null);

        for ( String t : testees ) {
            System.out.println( resp.getURLTitle(t) );
        }
    }
}
