/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.chatbot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * @author ucchy
 * ユーティリティクラス
 */
public class Utility {

    /**
     * 設定ファイルを読み込む
     * @param jarFile jarファイル
     * @param file 読み込むファイル
     * @return 読み込み結果
     */
    public static LinkedHashMap<String, String> loadConfigFile(File jarFile, File file) {

        // 親フォルダの存在を確認し、無いなら作る
        File parent = file.getParentFile();
        if ( !parent.exists() ) {
            parent.mkdirs();
        }

        // 設定ファイルの存在を確認し、無いなら同名ファイルをjarから取り出す。
        // Jarファイル設定が無いならここで終了する。
        if ( !file.exists() ) {
            if ( jarFile != null ) {
                Utility.copyFileFromJar(jarFile, file, file.getName());
            } else {
                return new LinkedHashMap<String, String>();
            }
        }

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
        LinkedHashMap<String, String> datas = new LinkedHashMap<String, String>();

        for ( String c : contents ) {

            int index = c.indexOf(":"); // 必ずコロンは存在するので -1にはならない
            String key = c.substring(0, index).trim();
            String value = c.substring(index + 1).trim();
            datas.put(key, value);
        }

        return datas;
    }

    /**
     * 設定ファイルに1行書き込む
     * @param file 書き込むファイル
     * @param data データ
     */
    public static void saveConfigFile(File file, LinkedHashMap<String, String> data) {

        // 親フォルダの存在を確認し、無いなら作る
        File parent = file.getParentFile();
        if ( !parent.exists() ) {
            parent.mkdirs();
        }

        // 書き込み
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            for ( String key : data.keySet() ) {
                writer.write(key + " : " + data.get(key));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( writer != null ) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
        }
    }

    /**
     * jarファイルの中に格納されているテキストファイルを、jarファイルの外にコピーするメソッド<br/>
     * WindowsだとS-JISで、MacintoshやLinuxだとUTF-8で保存されます。
     * @param jarFile jarファイル
     * @param targetFile コピー先
     * @param sourceFilePath コピー元
     */
    public static void copyFileFromJar(
            File jarFile, File targetFile, String sourceFilePath) {

        JarFile jar = null;
        InputStream is = null;
        FileOutputStream fos = null;
        BufferedReader reader = null;
        BufferedWriter writer = null;

        File parent = targetFile.getParentFile();
        if ( !parent.exists() ) {
            parent.mkdirs();
        }

        try {
            jar = new JarFile(jarFile);
            ZipEntry zipEntry = jar.getEntry(sourceFilePath);
            is = jar.getInputStream(zipEntry);

            fos = new FileOutputStream(targetFile);

            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            // CB190以降は、書き出すファイルエンコードにUTF-8を強制する。
            if ( isCB19orLater() ) {
                writer = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
            } else {
                writer = new BufferedWriter(new OutputStreamWriter(fos));
            }

            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.newLine();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( jar != null ) {
                try {
                    jar.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
            if ( writer != null ) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
            if ( reader != null ) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
            if ( fos != null ) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
            if ( is != null ) {
                try {
                    is.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
        }
    }

    /**
     * Jarファイル内から指定したファイルを直接読み込み、内容を返すメソッド
     * @param file 読み込み対象のJarファイル
     * @param name Jarファイルの中から読み出すファイルの絶対パス
     * @return ファイルの内容
     */
    public static ArrayList<String> getContentsFromJar(File file, String name) {

        ArrayList<String> contents = new ArrayList<String>();
        JarFile jarFile = null;
        InputStream inputStream = null;
        try {
            jarFile = new JarFile(file);
            ZipEntry zipEntry = jarFile.getEntry(name);
            inputStream = jarFile.getInputStream(zipEntry);
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;
            while ( (line = reader.readLine()) != null ) {
                contents.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if ( jarFile != null ) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
            if ( inputStream != null ) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // do nothing.
                }
            }
        }

        return contents;
    }

    /**
     * 文字列内のカラーコードを置き換えする
     * @param source 置き換え元の文字列
     * @return 置き換え後の文字列
     */
    public static String replaceColorCode(String source) {

        return ChatColor.translateAlternateColorCodes('&', source);
    }

    /**
     * 文字列が整数値に変換可能かどうかを判定する
     * @param source 変換対象の文字列
     * @return 整数に変換可能かどうか
     */
    public static boolean checkIntParse(String source) {

        return source.matches("^-?[0-9]{1,9}$");
    }

    /**
     * カラーコードをChatColorに変換する
     * @param colorCode カラーコード
     * @return ChatColorオブジェクト
     */
    public static ChatColor getChatColorFromColorCode(String colorCode) {

        if ( !colorCode.matches("&[0-9a-fk-or]") ) {
            return ChatColor.WHITE;
        }

        char code = colorCode.charAt(1);
        return ChatColor.getByChar(code);
    }

    /**
     * 現在動作中のCraftBukkitが、v1.9 以上かどうかを確認する
     * @return v1.9以上ならtrue、そうでないならfalse
     */
    public static boolean isCB19orLater() {
        return isUpperVersion(Bukkit.getBukkitVersion(), "1.9");
    }

    /**
     * 指定されたバージョンが、基準より新しいバージョンかどうかを確認する
     * @param version 確認するバージョン
     * @param border 基準のバージョン
     * @return 基準より確認対象の方が新しいバージョンかどうか<br/>
     * ただし、無効なバージョン番号（数値でないなど）が指定された場合はfalseに、
     * 2つのバージョンが完全一致した場合はtrueになる。
     */
    public static boolean isUpperVersion(String version, String border) {

        int hyphen = version.indexOf("-");
        if ( hyphen > 0 ) {
            version = version.substring(0, hyphen);
        }

        String[] versionArray = version.split("\\.");
        int[] versionNumbers = new int[versionArray.length];
        for ( int i=0; i<versionArray.length; i++ ) {
            if ( !versionArray[i].matches("[0-9]+") )
                return false;
            versionNumbers[i] = Integer.parseInt(versionArray[i]);
        }

        String[] borderArray = border.split("\\.");
        int[] borderNumbers = new int[borderArray.length];
        for ( int i=0; i<borderArray.length; i++ ) {
            if ( !borderArray[i].matches("[0-9]+") )
                return false;
            borderNumbers[i] = Integer.parseInt(borderArray[i]);
        }

        int index = 0;
        while ( (versionNumbers.length > index) && (borderNumbers.length > index) ) {
            if ( versionNumbers[index] > borderNumbers[index] ) {
                return true;
            } else if ( versionNumbers[index] < borderNumbers[index] ) {
                return false;
            }
            index++;
        }
        if ( borderNumbers.length == index ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 現在接続中のプレイヤーを全て取得する
     * @return 接続中の全てのプレイヤー
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<Player> getOnlinePlayers() {
        // CB179以前と、CB1710以降で戻り値が異なるため、
        // リフレクションを使って互換性を（無理やり）保つ。
        try {
            if (Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0]).getReturnType() == Collection.class) {
                Collection<?> temp =
                        ((Collection<?>) Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0])
                                .invoke(null, new Object[0]));
                return new ArrayList<Player>((Collection<? extends Player>) temp);
            } else {
                Player[] temp =
                        ((Player[]) Bukkit.class.getMethod("getOnlinePlayers", new Class<?>[0])
                                .invoke(null, new Object[0]));
                ArrayList<Player> players = new ArrayList<Player>();
                for (Player t : temp) {
                    players.add(t);
                }
                return players;
            }
        } catch (NoSuchMethodException ex) {} // never happen
        catch (InvocationTargetException ex) {} // never happen
        catch (IllegalAccessException ex) {} // never happen
        return new ArrayList<Player>();
    }
}
