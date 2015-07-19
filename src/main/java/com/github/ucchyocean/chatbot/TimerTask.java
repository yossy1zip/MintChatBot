/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2014
 */
package com.github.ucchyocean.chatbot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.scheduler.BukkitRunnable;

/**
 * タイマータスク
 * @author ucchy
 */
public class TimerTask extends BukkitRunnable {

    private static final String SIGNAL_FORMAT = "HHmm";
    private static final String ALERM_FORMAT = "MMddHHmm";
    private static final String REPEAT_REGEX = "R([0-9]{1,4})";

    // 最後に時報を行った時刻の文字列
    private String lastSignal;

    private SimpleDateFormat time_format;
    private SimpleDateFormat date_format;
    private Pattern repeat_pattern;
    private ChatBotConfig config;
    private TimeSignalData signalData;

    private Pattern patternRandomGroup;

    /**
     * コンストラクタ
     * @param config コンフィグ
     * @param signalData 時報データ
     */
    public TimerTask(ChatBotConfig config, TimeSignalData signalData) {

        patternRandomGroup = Pattern.compile(".*\\(([^\\)]*)\\).*");

        this.config = config;
        this.signalData = signalData;

        time_format = new SimpleDateFormat(SIGNAL_FORMAT);
        date_format = new SimpleDateFormat(ALERM_FORMAT);
        repeat_pattern = Pattern.compile(REPEAT_REGEX);

        // 繰り返し通知を起動する。
        if ( config.isRepeatSignals() ) {

            for ( String key : signalData.getAllKeys() ) {

                Matcher matcher = repeat_pattern.matcher(key);

                if ( matcher.matches() ) {

                    int minutes = Integer.parseInt(matcher.group(1));
                    if ( minutes == 0 ) continue;
                    int ticks = minutes * 60 * 20;
                    final String responce = signalData.getResponceIfMatch(key);

                    new BukkitRunnable() {
                        public void run() {
                            MintChatBot.getInstance().say(replaceRandomGroup(responce));
                        }
                    }.runTaskTimerAsynchronously(MintChatBot.getInstance(), ticks, ticks);
                }
            }
        }
    }

    /**
     * タスクが動作する時に呼び出されるメソッド
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        Date date = new Date();
        String time = time_format.format(date);

        if ( time.equals(lastSignal) ) {
            return;
        }

        if ( config.isTimeSignals() ) {

            // 時報の処理
            String responce = signalData.getResponceIfMatch(time);
            MintChatBot.getInstance().say(replaceRandomGroup(responce));
        }

        if ( config.isAlermSignals() ) {

            // アラームの処理
            String datetime = date_format.format(date);
            String responce = signalData.getResponceIfMatch(datetime);
            MintChatBot.getInstance().say(replaceRandomGroup(responce));
        }

        lastSignal = time;
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
}
