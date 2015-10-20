/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2014
 */
package com.github.ucchyocean.chatbot;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * レスポンス用のデータ管理オブジェクト
 * @author ucchy
 */
public class ResponceData {

    private static final String FILE_NAME = "responces.txt";
    private static final String FILE_NAME_USERDATA = "userdata.txt";

    private static final String COMMAND_COOLDOWN = "@cooldown";
    private static final String COMMAND_LEARN = "@learn";
    private static final String COMMAND_FORGET = "@forget";
    private static final String COMMAND_PRIVATE = "@private";

    private LinkedHashMap<String, String> data;
    private LinkedHashMap<String, String> userdata;
    private String prevResponceKey;
    private long prevResponceTime;
    private int responceCooldownSeconds;

    private File jarFile;
    private File file;
    private KeywordReplacer replacer;

    /**
     * コンストラクタ
     * @param jarFile プラグインのJarファイル
     * @param dataFolder プラグインのデータフォルダ
     */
    public ResponceData(File jarFile, File dataFolder, int responceCooldownSeconds) {

        this.jarFile = jarFile;
        this.responceCooldownSeconds = responceCooldownSeconds;
        this.replacer = new KeywordReplacer();

        file = new File(dataFolder, FILE_NAME);
        reloadData();
    }

    /**
     * 設定ファイルをリロードする
     */
    public void reloadData() {
        data = Utility.loadConfigFile(jarFile, file);
        userdata = Utility.loadConfigFile(null,
                new File(file.getParentFile(), FILE_NAME_USERDATA));
    }

    /**
     * 指定されたチャット発言に対する応答設定がある場合、応答内容を返します。
     * 応答設定が無いなら、nullが返されます。
     * @param source チャット発言内容
     * @param sender チャット発言者
     * @return 応答内容
     */
    public String getResponceIfMatch(String source, CommandSender sender) {

        String res = getRes(data, source, sender, null);
        if ( res != null ) {
            if ( res.equals(COMMAND_COOLDOWN) ) return null;
            if ( res.startsWith(COMMAND_LEARN) ) return learn(res);
            if ( res.startsWith(COMMAND_FORGET) ) return forget(res);
            if ( res.startsWith(COMMAND_PRIVATE) ) return tell(res, sender);
            return res;
        }

        res = getRes(userdata, source, sender, null);
        if ( res == null ) return null;
        if ( res.equals(COMMAND_COOLDOWN) ) return null;
        if ( res.startsWith(COMMAND_LEARN) ) return learn(res);
        if ( res.startsWith(COMMAND_FORGET) ) return forget(res);
        if ( res.startsWith(COMMAND_PRIVATE) ) return tell(res, sender);
        return res;
    }

    /**
     * 指定されたチャット発言に対する応答設定がある場合、応答内容を返します。
     * 応答設定が無いなら、nullが返されます。
     * @param source チャット発言内容
     * @param altName チャット発言者名
     * @return 応答内容
     */
    public String getResponceIfMatch(String source, String altName) {

        String res = getRes(data, source, null, altName);
        if ( res != null ) {
            if ( res.equals(COMMAND_COOLDOWN) ) return null;
            if ( res.startsWith(COMMAND_LEARN) ) return learn(res);
            if ( res.startsWith(COMMAND_FORGET) ) return forget(res);
            if ( res.startsWith(COMMAND_PRIVATE) ) return null;
            return res;
        }

        res = getRes(userdata, source, null, altName);
        if ( res == null ) return null;
        if ( res.equals(COMMAND_COOLDOWN) ) return null;
        if ( res.startsWith(COMMAND_LEARN) ) return learn(res);
        if ( res.startsWith(COMMAND_FORGET) ) return forget(res);
        if ( res.startsWith(COMMAND_PRIVATE) ) return null;
        return res;
    }

    /**
     * 指定されたチャット発言に対する応答設定がある場合、応答内容を返します。
     * 応答設定が無いなら、nullが返されます。
     */
    private String getRes(LinkedHashMap<String, String> data, String source, CommandSender sender, String altName) {

        for ( String key : data.keySet() ) {

            String responce = data.get(key);

            boolean isNotRepeat = false;
            boolean isPrivateResponce = false;
            if ( key.startsWith("@") ) {
                isNotRepeat = true;
                key = key.substring(1);
            }
            if ( key.startsWith(">") ) {
                isPrivateResponce = true;
                key = key.substring(1);
            }

            if ( source.matches(key) ) {

                long cooldown = responceCooldownSeconds * 1000;
                if ( isNotRepeat && prevResponceKey != null &&
                        prevResponceKey.equals(key) &&
                        (System.currentTimeMillis() - prevResponceTime) < cooldown ) {
                    return COMMAND_COOLDOWN;
                }

                responce = replacer.replace(responce, sender, key, source, altName);

                if ( isNotRepeat ) {
                    prevResponceKey = key;
                    prevResponceTime = System.currentTimeMillis();
                }

                if ( isPrivateResponce ) {
                    responce = COMMAND_PRIVATE + responce;
                }

                return responce;
            }
        }

        return null;
    }

    private String learn(String source) {

        Pattern pat = Pattern.compile("@learn (.+)=(.+)");
        Matcher matcher = pat.matcher(source);
        if ( !matcher.matches() ) return null;
        String key = matcher.group(1);
        String value = matcher.group(2);
        setUserData(key, value);

        String format = MintChatBot.getInstance().getMessages()
                .getResponceIfMatch("study_learn");
        if ( format == null ) return null;
        return format.replace("%key", key).replace("%value", value);
    }

    private String forget(String source) {

        Pattern pat = Pattern.compile("@forget (.+)");
        Matcher matcher = pat.matcher(source);
        if ( !matcher.matches() ) return null;
        String key = matcher.group(1);
        boolean result = removeUserData(key);

        if ( !result ) return null;
        String format = MintChatBot.getInstance().getMessages()
                .getResponceIfMatch("study_forget");
        if ( format == null ) return null;
        return format.replace("%key", key);
    }

    private String tell(String source, final CommandSender recipient) {

        final String message = source.substring(COMMAND_PRIVATE.length());
        MintChatBot parent = MintChatBot.getInstance();

        // 数tick遅らせて送信する
        new BukkitRunnable() {
            public void run() {
                MintChatBot.getInstance().tell(message, recipient);
            }
        }.runTaskLater(parent, parent.getCBConfig().getResponceDelayTicks());

        return null; // 常にnullを返す
    }

    /**
     * ユーザーデータを追加設定or上書き設定する
     * @param key キー
     * @param value 値
     */
    private void setUserData(String key, String value) {
        userdata.put(key, value);
        Utility.saveConfigFile(new File(file.getParentFile(), FILE_NAME_USERDATA), userdata);
    }

    /**
     * ユーザーデータから設定を削除する
     * @param key キー
     */
    private boolean removeUserData(String key) {
        String res = userdata.remove(key);
        if ( res != null ) {
            Utility.saveConfigFile(new File(file.getParentFile(), FILE_NAME_USERDATA), userdata);
            return true;
        }
        return false;
    }
}
