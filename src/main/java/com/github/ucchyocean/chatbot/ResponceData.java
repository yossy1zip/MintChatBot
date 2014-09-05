/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2014
 */
package com.github.ucchyocean.chatbot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * レスポンス用のデータ管理オブジェクト
 * @author ucchy
 */
public class ResponceData {

    private static final String FILE_NAME = "responces.txt";

    private static final String RESPONCE_TIME = "HH:mm:ss";
    private static final String RESPONCE_DATE = "yyyy/MM/dd(E)";

    private HashMap<String, String> data;
    private SimpleDateFormat time_format;
    private SimpleDateFormat date_format;
    private Pattern patternRandomGroup;

    private File jarFile;
    private File dataFolder;

    /**
     * コンストラクタ
     * @param jarFile プラグインのJarファイル
     * @param dataFolder プラグインのデータフォルダ
     */
    public ResponceData(File jarFile, File dataFolder) {

        this.jarFile = jarFile;
        this.dataFolder = dataFolder;

        time_format = new SimpleDateFormat(RESPONCE_TIME);
        date_format = new SimpleDateFormat(RESPONCE_DATE, Locale.JAPAN);
        patternRandomGroup = Pattern.compile(".*\\(([^\\)]*)\\).*");

        reloadData();
    }

    public void reloadData() {

        if ( !dataFolder.exists() ) {
            dataFolder.mkdirs();
        }

        File file = new File(dataFolder, FILE_NAME);
        if ( !file.exists() ) {
            // 新しいファイルをコピーする
            Utility.copyFileFromJar(jarFile, file, FILE_NAME);
        }

        data = load(file);
    }

    /**
     * responce.txt を読み込む
     * @param file 読み込むファイル
     * @return 読み込み結果
     */
    private static HashMap<String, String> load(File file) {

        // ファイルの内容を読み出す
        ArrayList<String> contents = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ( (line = reader.readLine()) != null ) {

                line = line.trim();

                // 頭にシャープが付いている行は、コメントとして読み飛ばす
                // コロンが含まれない行は、データ無しとして読み飛ばす
                if ( !line.startsWith("#") && line.contains(":") ) {
                    contents.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( reader != null ) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
        }

        // 内容の解析
        HashMap<String, String> datas = new HashMap<String, String>();

        for ( String c : contents ) {

            int index = c.indexOf(":"); // 必ずコロンは存在するので -1にはならない
            String key = c.substring(0, index).trim();
            String value = c.substring(index + 1).trim();
            datas.put(key, value);
        }

        return datas;
    }

    /**
     * 指定されたチャット発言に対する応答設定がある場合、応答内容を返します。
     * 応答設定が無いなら、nullが返されます。
     * @param source チャット発言内容
     * @param player チャット発言者
     * @return 応答内容
     */
    public String getResponceIfMatch(String source, Player player) {

        for ( String key : data.keySet() ) {

            if ( source.matches(key) ) {

                String responce = data.get(key);
                responce = replaceKeywords(responce, player);
                responce = replaceMatchingGroups(responce, key, source);
                responce = replaceRandomGroup(responce);
                return responce;
            }
        }

        return null;
    }

    /**
     * 指定された文字列に含まれるキーワードを置き換える
     * @param source 元の文字列
     * @param player プレイヤー、不要ならnullで良い。
     * @return キーワード置き換え済みの文字列
     */
    private String replaceKeywords(String source, Player player) {

        String responce = source;

        if ( responce.contains("%player") ) {
            String name = "";
            if ( player != null ) {
                name = player.getDisplayName() + ChatColor.RESET;
            }
            responce = responce.replace("%player", name);
        }

        if ( responce.contains("%time") ) {
            String time = time_format.format(new Date());
            responce = responce.replace("%time", time);
        }

        if ( responce.contains("%date") ) {
            String date = date_format.format(new Date());
            responce = responce.replace("%date", date);
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

        Matcher matcher = patternRandomGroup.matcher(source);

        if ( matcher.matches() ) {

            String org = matcher.group(1);
            String[] items = org.split("\\|");

            int index = (int)(Math.random() * items.length);
            return source.replace("(" + org + ")", items[index]);
        }

        return source;
    }

    // デバッグ用のエントリポイント
    public static void main(String[] args) {

        File folder = new File("src\\main\\resources");
        ResponceData test = new ResponceData(null, folder);

        for ( String key : test.data.keySet() ) {
            System.out.println(String.format("key={%s}, data={%s}", key, test.data.get(key)));
        }

        String[] testees = new String[]{"hi.", "Hi!", "おはよう", "いまなんじ？", "今日何日",
                "ごはんください", "お金をください！", "マスターいつもの", "占い", "⑨", "(´ω｀)", "(*´ω｀*)"};

        for ( String testee : testees ) {
            System.out.println(String.format("testee={%s}, responce={%s}",
                    testee, test.getResponceIfMatch(testee, null)));
        }
    }

}
