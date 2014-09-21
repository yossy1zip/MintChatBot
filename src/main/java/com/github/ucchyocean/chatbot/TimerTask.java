/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2014
 */
package com.github.ucchyocean.chatbot;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * タイマータスク
 * @author ucchy
 */
public class TimerTask extends BukkitRunnable {

    private static final String SIGNAL_REGEX = "HHmm";
    private static final String ALERM_REGEX = "MMddHHmm";

    // 最後に時報を行った時刻の文字列
    private String lastSignal;

    private SimpleDateFormat time_format;
    private SimpleDateFormat date_format;
    private ChatBotConfig config;
    private TimeSignalData signalData;

    /**
     * コンストラクタ
     * @param config コンフィグ
     * @param signalData 時報データ
     */
    public TimerTask(ChatBotConfig config, TimeSignalData signalData) {

        this.config = config;
        this.signalData = signalData;

        time_format = new SimpleDateFormat(SIGNAL_REGEX);
        date_format = new SimpleDateFormat(ALERM_REGEX);
    }

    /**
     * タスクが動作する時に呼び出されるメソッド
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        Date date = new Date();
        String time = time_format.format(date);
        if ( !time.equals(lastSignal) ) {

            // 時報の処理
            String responce = signalData.getResponceIfMatch(time);
            if ( responce != null ) {
                String temp = config.getResponceFormat();
                temp = temp.replace("%botName", config.getBotName());
                temp = temp.replace("%responce", responce);
                temp = temp.replace("\\n", "\n");
                Bukkit.broadcastMessage(Utility.replaceColorCode(temp));
            }

            // アラームの処理
            String datetime = date_format.format(date);
            responce = signalData.getResponceIfMatch(datetime);
            if ( responce != null ) {
                String temp = config.getResponceFormat();
                temp = temp.replace("%botName", config.getBotName());
                temp = temp.replace("%responce", responce);
                temp = temp.replace("\\n", "\n");
                Bukkit.broadcastMessage(Utility.replaceColorCode(temp));
            }

            lastSignal = time;
        }
    }
}
