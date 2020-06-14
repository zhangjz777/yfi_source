package com.zzz.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @desc date util
 */
public final class DateUtil {
    public static final String YYYYMMDD = "yyyyMMdd";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String YYMMDD = "yyMMdd";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YY_MM_DD = "yy-MM-dd";
    public static final String YYYY_MM_DD_T_HH_MM_SS_ZZ = "yyyy-MM-dd'T'HH:mm:ssZZ";

    public static long parseLongTime(Date date) {
        if (AppUtil.isNull(date)) {
            return 0;
        }
        long t = 0;
        try {
            String time = formatTime(date, YYYYMMDD);
            t = AppUtil.parseLong(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    public static long parseLongTimeH(Date date) {
        if (AppUtil.isNull(date)) {
            return 0;
        }
        long t = 0;
        try {
            String time = formatTime(date, "yyyyMMddHH");
            t = AppUtil.parseLong(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    public static Date parseDate(String dateStr) {
        return parseDate(dateStr, YYYY_MM_DD);
    }

    public static Date parseDate(String dateStr, String format) {
        if (AppUtil.isNull(dateStr)) {
            return null;
        }
        SimpleDateFormat df = new SimpleDateFormat(format);
        Date d = null;
        try {
            d = df.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d;
    }

    public static String formatTime(Date date, String format) {
        if (AppUtil.isNull(date)) {
            return null;
        }
        if (AppUtil.isNull(format)) {
            format = YYYY_MM_DD;
        }
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(date);
    }

    /**
     * 获取当前时间总秒数
     */
    public static int getTodayPlusSeconds() {
        Calendar calendar = Calendar.getInstance();
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        return 60 * 60 * 24 - (hours * 60 * 60 - minutes * 60 - seconds);
    }

    /**
     * 获取N分钟前
     */
    public static Date getDateBeforeMin(Date d, int min) {
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.MINUTE, now.get(Calendar.MINUTE) - min);
        return now.getTime();
    }

    /**
     * 获取N天前
     */
    public static Date getDateBefore(Date d, int day) {
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - day);
        return now.getTime();
    }

    /**
     * 获取N月前
     */
    public static Date getMonthBefore(Date d, int month) {
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.MONTH, now.get(Calendar.MONTH) - month);
        return now.getTime();
    }

    public static String to_yyyy_MM_DD(String str) {
        if (AppUtil.isNull(str)) {
            return "";
        }

        if (str.length() != 8) {
            return "";
        }

        return str.substring(0, 4) + "-" + str.substring(4, 6) + "-" + str.substring(6, 8);
    }

    public static Integer timearea(Date date) {
    	Calendar ca =Calendar.getInstance();
    	ca.setTime(date);
    	ca.clear(Calendar.MILLISECOND);
    	ca.clear(Calendar.SECOND);
    	ca.clear(Calendar.MINUTE);
    	long time = date.getTime();
    	for(int i=1;i<=6;i++) {
    		ca.set(Calendar.HOUR_OF_DAY, i*4);
    		if(time<ca.getTimeInMillis()) {
    			return i*4;
    		}
    	}
    	return null;
    }

    /**
     * @methodsname 返回第二天凌晨的秒数
     * @author 段康庄
     * @date 2018年7月26日下午2:22:16
     * @param
     * @return
     */
    public static Integer getSecondsNextEarlyMorning() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        // 改成这样就好了
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Long seconds = (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000;
        return seconds.intValue();
    }

    public static int getPlusDay(Date begin, Date end){
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(begin);
        c2.setTime(end);
        return c2.get(Calendar.DAY_OF_YEAR) - c1.get(Calendar.DAY_OF_YEAR);
    }
    public static void main(String[] args) {
        String s = "sds";
        System.out.println(s.compareTo("sds"));
    }
}
