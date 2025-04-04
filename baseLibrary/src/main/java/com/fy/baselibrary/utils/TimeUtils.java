package com.fy.baselibrary.utils;

import android.text.TextUtils;
import android.util.ArrayMap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 时间转换工具类
 *
 * Created by fangs on 2017/3/22.
 */
public class TimeUtils {

    private TimeUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 将一个时间戳转化成时间字符串，自定义格式
     *
     * @param time
     * @param format
     *            如 yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String Long2DataString(long time, String format) {
        if (time == 0) {
            return "";
        }

        Date date = new Date(time);

        return Data2String(date, format);
    }

    /**
     * 将一个时间字符串转化为 Date
     * @param timeStr
     * @param format
     * @return
     */
    public static Date string2Date(String timeStr, String format) {
        if(TextUtils.isEmpty(timeStr)) return new Date(-1);

        try {
            SimpleDateFormat simpleDateFormat = getSimpleDateFormat(format);
            return simpleDateFormat.parse(timeStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return new Date(-1);//转换出错
    }

    /**
     * 将一个时间字符串转换为时间戳，自定义格式
     * @param timeStr
     * @param format
     * @return
     */
    public static long timeString2long(String timeStr, String format){
        Date date = string2Date(timeStr, format);
        return date.getTime();
    }

    /**
     * 将一个日期对象转换成 时间字符串
     * @param date
     * @param format
     * @return
     */
    public static String Data2String(Date date, String format){
        SimpleDateFormat sdf = getSimpleDateFormat(format);
        return sdf.format(date);
    }

    public static SimpleDateFormat getSimpleDateFormat(String format) {
        Locale locale = Locale.getDefault();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, locale);

        if(locale == Locale.CHINA){
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+08"));
        } else {
            simpleDateFormat.setTimeZone(TimeZone.getDefault());
        }

        return simpleDateFormat;
    }

    /**
     * 根据 calendar 获取本周的开始日期和结束日期 的时间戳
     * @param isMoudel 周起始模式（Calendar.SUNDAY 以周日为首日; Calendar.MONDAY 以周一为首日）
     */
    public static long[] getWeekTimeMillis(Calendar calendar, int isMoudel) {
        Calendar calendarWeek = Calendar.getInstance();
        calendarWeek.setTimeInMillis(calendar.getTimeInMillis());

        calendarWeek.add(Calendar.DATE, 0 * 7);   // 0 表示当前周，-1 表示上周，1 表示下周，以此类推
        calendarWeek.setFirstDayOfWeek(Calendar.SUNDAY); // 以周日为首日
        // 获取本周的开始日期
        calendarWeek.set(Calendar.DAY_OF_WEEK, isMoudel);
        long startOfWeek = calendarWeek.getTimeInMillis();

        // 获取本周的结束日期
        calendarWeek.set(Calendar.DAY_OF_WEEK, isMoudel == Calendar.SUNDAY ? Calendar.SATURDAY : Calendar.SUNDAY);
        long endOfWeek = calendarWeek.getTimeInMillis();

        return new long[]{startOfWeek, endOfWeek};
    }

    /**
     * 判断选择的日期是否是本周
     */
    public static boolean isThisWeek(Calendar date) {
        Calendar calendar = Calendar.getInstance();
        int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);

        calendar.setTimeInMillis(date.getTimeInMillis());

        int paramWeek = calendar.get(Calendar.WEEK_OF_YEAR);
        if (paramWeek == currentWeek) {
            return true;
        }
        return false;
    }


    /**
     * 指定日期 是否在日期范围內
     * @return
     */
    public static boolean isCalendarInRange(Calendar targetCalendar, Calendar minCalendar, Calendar maxCalendar){
        targetCalendar.set(Calendar.HOUR_OF_DAY, 0);
        targetCalendar.set(Calendar.MINUTE, 0);
        targetCalendar.set(Calendar.SECOND, 0);
        targetCalendar.set(Calendar.MILLISECOND, 0);

        minCalendar.set(Calendar.HOUR_OF_DAY, 0);
        minCalendar.set(Calendar.MINUTE, 0);
        minCalendar.set(Calendar.SECOND, 0);
        minCalendar.set(Calendar.MILLISECOND, 0);

        maxCalendar.set(Calendar.HOUR_OF_DAY, 0);
        maxCalendar.set(Calendar.MINUTE, 0);
        maxCalendar.set(Calendar.SECOND, 0);
        maxCalendar.set(Calendar.MILLISECOND, 0);

        return targetCalendar.getTimeInMillis() >= minCalendar.getTimeInMillis()
                && targetCalendar.getTimeInMillis() <= maxCalendar.getTimeInMillis();
    }

    public static boolean isCalendarInRange(
            int targetYear, int targetYearMonth, int targetYearDay,
            int minYear, int minYearMonth, int minYearDay,
            int maxYear, int maxYearMonth, int maxYearDay
    ) {
        Calendar target = Calendar.getInstance();
        target.set(targetYear, targetYearMonth - 1, targetYearDay);

        Calendar min = Calendar.getInstance();
        min.set(minYear, minYearMonth - 1, minYearDay);

        Calendar max = Calendar.getInstance();
        max.set(maxYear, maxYearMonth - 1, maxYearDay);

        return isCalendarInRange(target, min, max);
    }

    /**
     * 获取 指定时间戳，指定 时间间隔， 的时间戳【如：获取当前时间点 后 50年3个月2天0时3分的 时间戳】
     * @param time
     * @param fieldMap key：日历字段。value：增加的时间【calendar.add(Calendar.YEAR, 50)】
     */
    public static long getTimeInterval(long time, ArrayMap<Integer, Integer> fieldMap){
        Calendar calendar = Calendar.getInstance();// 获取当前日期
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+08"));
        calendar.setTimeInMillis(time);

        for (ArrayMap.Entry<Integer, Integer> entry : fieldMap.entrySet()){
            calendar.add(entry.getKey(), entry.getValue());
        }

        return calendar.getTimeInMillis();
    }

    /**
     * 获得给定时间戳表示的日期 零时零分零秒零毫秒的时间戳
     * @return
     */
    public static long initDateByDay(long time){
        Calendar calendar = Calendar.getInstance();

        Locale locale = Locale.getDefault();
        if(locale == Locale.CHINA){
            calendar.setTimeZone(TimeZone.getTimeZone("GMT+08"));
        } else {
            calendar.setTimeZone(TimeZone.getDefault());
        }
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    /**
     * 计算 年龄
     * @param birthday
     * @param format
     * @return
     */
    public static int calculationAge(String birthday, String format){
        long   birthdayLong = timeString2long(birthday, format);
        if (birthdayLong == -1){
            return 0;
        }

        String currentYear  = Long2DataString(System.currentTimeMillis(), "yyyy");
        String birthdayYear = Long2DataString(birthdayLong, "yyyy");

        return Integer.parseInt(currentYear) - Integer.parseInt(birthdayYear);
    }

    /**
     * 获取指定时间戳 所在的一周的时间集合
     * @param time
     * @param isMoudel isMoudel == 0 以指定的时间戳为第一天；
     *                 isMoudel == 1 以指定的时间戳为最后一天；
     *                 isMoudel == 2 以指定的时间戳为一个礼拜其中一天；
     * @return
     */
    public static List<Date> dateToWeek(long time, int isMoudel) {
        //指定毫秒数所在的日期 零点零分零秒的毫秒数
        long zero = initDateByDay(time);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(zero);

        int d = calendar.get(Calendar.DAY_OF_WEEK);
        long startTime = isMoudel == 0 ? calendar.getTimeInMillis() :
                isMoudel == 1 ? calendar.getTimeInMillis() - 7 * 24 * 3600000 : calendar.getTimeInMillis() - d * 24 * 3600000;

        Date fdate;
        List<Date> list = new ArrayList<>();
        for (int a = 0; a < 7; a++) {
            fdate = new Date();
            fdate.setTime(startTime + a * 24 * 3600000);

            list.add(fdate);
        }
        return list;
    }

    /**
     * 判断两个时间戳是否同一天
     * @param time1
     * @param time2
     * @return
     */
    public static boolean isSameDay(long time1, long time2){
        String date1 = TimeUtils.Long2DataString(time1, "yyyy-MM-dd");
        String date2 = TimeUtils.Long2DataString(time2, "yyyy-MM-dd");

        return date1.equals(date2);
    }

    /**
     * 计算当前时间 和 指定时间戳，的时间差
     * @return
     */
    public static String getTimeDifference(long time) {
        long timeDifference = (System.currentTimeMillis() - time);

        if (timeDifference <= 60000) {//小于一分钟
            return "刚刚";
        } else if (timeDifference < 60 * 60000) {//小于一小时
            return (timeDifference / 60000) + "分钟前";
        } else if (timeDifference < 24L * 3600000L) {//小于一天
            return (timeDifference / 3600000L) + "小时前";
        }

//        else if (timeDifference < 48L * 3600000L) {//大于一天小于两天
//            return "昨天";
//        } else if (timeDifference < 720L * 3600000L){//小于30天
//            return (timeDifference / (24L * 3600000L)) + "天前";
//        }

        else {
            return Long2DataString(time, "yyyy-MM-dd");
        }
    }

    //毫秒转秒
    public static String long2String(long time){

        //毫秒转秒
        int sec = (int) time / 1000 ;
        int min = sec / 60 ;	//分钟
        sec = sec % 60 ;		//秒
        if(min < 10){	//分钟补0
            if(sec < 10){	//秒补0
                return "0"+min+":0"+sec;
            }else{
                return "0"+min+":"+sec;
            }
        }else{
            if(sec < 10){	//秒补0
                return min+":0"+sec;
            }else{
                return min+":"+sec;
            }
        }

    }

    /**
     * 根据给定时间 time 单位 秒，计算 分钟数 秒 数
     * @param time
     * @return "3'12''"
     */
    public static String getTime(int time) {
        StringBuilder sb = new StringBuilder();
        if (time > 60) {
            sb.append(time / 60)
                    .append("'");

            if (time % 60 != 0) {
                sb.append(time % 60)
                        .append("''");
            }
        } else {
            if (time % 60 != 0) {
                sb.append(time % 60)
                        .append("''");
            }
        }

        return sb.toString();
    }

    /**
     * 传入一个时间(毫秒单位),
     *
     * @param millis
     * @return 获取当前时间与传入时间,   相差的天数
     */
    public int getIntervalDada(long millis) {
        long curMillis = System.currentTimeMillis();
        long betweenTime = curMillis - millis;
        int days = (int) (betweenTime / 1000 / 60 / 60 / 24);
        return days;
    }


    /**
     * 将误差转变为d日h时m分s秒格式
     *
     * @param lTime
     * @return
     */
    public static String setLtime(long lTime) {
        long mTime = Math.abs(lTime);
        String str;
        long d = mTime / (1000 * 60 * 60 * 24);
        long h = mTime % (1000 * 60 * 60 * 24) / (1000 * 60 * 60);
        long m = mTime % (1000 * 60 * 60 * 24) % (1000 * 60 * 60) / (1000 * 60);
        long s = mTime % (1000 * 60 * 60 * 24) % (1000 * 60 * 60) % (1000 * 60) / 1000;
        if (d != 0) {
            if (lTime > 0) {
                str = String.format("  快%d日%d时%02d分%02d秒", d, h, m, s);
            } else {
                str = String.format("  慢%d日%d时%02d分%02d秒", d, h, m, s);
            }
        } else if (h != 0) {
            if (lTime > 0) {
                str = String.format("  快%d时%02d分%02d秒", h, m, s);
            } else {
                str = String.format("  慢%d时%02d分%02d秒", h, m, s);
            }
        } else if (h == 0 && m != 0) {
            if (lTime > 0) {
                str = String.format("  快%02d分%02d秒", m, s);
            } else {
                str = String.format("  慢%02d分%02d秒", m, s);
            }
        } else if (h == 0 && m == 0 && s != 0) {
            if (lTime > 0) {
                str = String.format("  快%02d秒", s);
            } else {
                str = String.format("  慢%02d秒", s);
            }
        } else {
            str = "  (0\")";
        }
        return str;
    }
}
