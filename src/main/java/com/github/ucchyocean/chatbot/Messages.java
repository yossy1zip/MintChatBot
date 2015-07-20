/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2015
 */
package com.github.ucchyocean.chatbot;

import java.io.File;
import java.util.LinkedHashMap;

/**
 * メッセージ設定
 * @author ucchy
 */
public class Messages {

    private static final String FILE_NAME = "messages.txt";

    private LinkedHashMap<String, String> data;
    private File jarFile;
    private File file;

    /**
     * コンストラクタ
     * @param jarFile プラグインのJarファイル
     * @param dataFolder プラグインのデータフォルダ
     */
    public Messages(File jarFile, File dataFolder) {

        this.jarFile = jarFile;
        file = new File(dataFolder, FILE_NAME);
        reloadData();
    }

    /**
     * 設定ファイルをリロードする
     */
    public void reloadData() {
        data = Utility.loadConfigFile(jarFile, file);
    }

    /**
     * 指定された時刻文字列に対する応答設定がある場合、応答内容を返します。
     * 応答設定が無いなら、nullが返されます。
     * @param source 時刻文字列
     * @return 応答内容
     */
    public String getResponceIfMatch(String source) {
        return data.get(source);
    }
}
