package com.nx.nx6313.mp4radio;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateHandler {
	
	private static long calibrationTime = 0;
	
	public static void setTime(long time){
		calibrationTime = time - System.currentTimeMillis();
	}
	
	public static long getTime(){
		return System.currentTimeMillis() + calibrationTime;
	}

	/**
	 * 获得时间编号（17位）加 n 位随机数
	 * @param n 随机数的数量
	 * @return
	 */
	public static String getcode(int n) {
		String code = new String();
		Date nowTime = new Date();
		SimpleDateFormat matter = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		code = matter.format(nowTime);
		for(int i = 0; i < n; i++){
			int flag = (int) (Math.random() * 10);
			code = code + flag;
		}
		return code;
	}
	
	/**
	* 从身份证获取出生年月
	* @param cardNumber 已经校验合法的身份证号
	* @return Strig YYYY-MM 出生年月
	*/
	public static String getBirthMonthFromCard(String cardNumber){
	String card = cardNumber.trim();
	    String year;
	    String month;
	    if (card.length()==18){
	    	//处理18位身份证
	        year=card.substring(6,10);
	        month=card.substring(10,12);
	    }else{
	    	//处理非18位身份证
	    	year=card.substring(6,8);
	        month=card.substring(8,10);
	        year="19"+year;        
	    }
	    return year+"-"+month;
	}
	
	/**
	* 从身份证获取出生年月日
	* @param cardNumber 已经校验合法的身份证号
	* @return Strig YYYY-MM-DD 出生年月日
	*/
	public static String getBirthDateFromCard(String cardNumber){
	String card = cardNumber.trim();
	    String year;
	    String month;
	    String day;
	    if (card.length()==18){
	    	//处理18位身份证
	        year = card.substring(6,10);
	        month = card.substring(10,12);
	        day = card.substring(12,14);
	    }else{
	    	//处理非18位身份证
	    	year = card.substring(6,8);
	        month = card.substring(8,10);
	        year = "19" + year;
	        day = card.substring(10,12);
	    }
	    return year + "-" + month + "-" + day;
	}

	/**
	 * 格式化时间字符串(默认为yyyy-MM-dd HH:mm:ss)
	 * @param date
	 * @param format
	 * @return
	 */
	public static String formatDate(Date date, String format) {
		if(date == null)
			date = new Date();
		if(format == null)
			format = "yyyy-MM-dd HH:mm:ss";
		String code = new String();
		SimpleDateFormat matter = new SimpleDateFormat(format);
		code = matter.format(date);
		return code;
	}
	
	/**
	 * 格式化时间字符串，获得大概时间
	 * 
	 * @return String
	 */
	public static String getDateString(Date date) {
		
		if(date == null)
			date = new Date();
		
		String code = new String();
		Date today = new Date();
		long d = date.getTime() / 86400000;
		long t = today.getTime() / 86400000;
		
		if(d == t){
			if(today.getTime() - date.getTime() < 180000) {
				code = "刚刚";
			} else if(today.getTime() - date.getTime() < 3600000) {
				code = (today.getTime() - date.getTime()) / 60000 +"分钟前";
			} else if(today.getTime() - date.getTime() < 10800000) {
				code = (today.getTime() - date.getTime()) / 3600000 +"小时前";
			} else {
				SimpleDateFormat matter = new SimpleDateFormat("HH:mm");
				code = matter.format(date);
			}
		} else if(d == t - 1){
			long time = date.getTime() % 86400000;
			if(time < 32400000){
				code = "昨天早上";
			} else if(time < 43200000){
				code = "昨天上午";
			} else if(time < 64800000){
				code = "昨天下午";
			} else {
				code = "昨天晚上";
			}
		} else {
			SimpleDateFormat matter = new SimpleDateFormat("MM月dd日");
			code = matter.format(date);
		}
		
		return code;
	}
	
	/**
	 * 由字符串，获得一个时间
	 * @param time 字符串
	 * @return 时间
	 */
	public static Date getDate(String time){
		
		if(null == time)
			return new Date(0);
		
		Calendar ca = Calendar.getInstance();
		
		String str[] = time.split(" |-|:|\\.");
		
		int num[] = new int[6];
		for(int i = 0; i < 6; i++){
			if(i < str.length){
				num[i] = Integer.parseInt(str[i]);
			} else {
				num[i] = 0;
			}
		}
		
		ca.set(num[0], num[1]-1, num[2], num[3], num[4], num[5]);
		
		return new Date(ca.getTimeInMillis());
	}

	/**
	 * 获得时间字符串，计时使用
	 * @param time 毫秒数
	 * @return 计时
	 */
	public static String getTime(long time) {
		String format_time = "";

		time = time / 1000;
		long temp = time % 60;
		if (temp > 9)
			format_time += ":" + temp;
		else
			format_time += ":0" + temp;

		time = time / 60;
		temp = time % 60;
		if (temp > 9)
			format_time = ":" + temp + format_time;
		else
			format_time = ":0" + temp + format_time;

		temp = time / 60;
		if (temp > 9)
			format_time = temp + format_time;
		else
			format_time = "0" + temp + format_time;

		return format_time;
	}
	
	/**
	 * 计算日期对应的星座
	 * @param date 日期
	 * @return 星座
	 */
	public static String getConstellation(Date date) {
		
		if(null == date)
			return "未知";
		
		int month = date.getMonth()+1;
		int day = date.getDate();
		
	    String s="魔羯水瓶双鱼牡羊金牛双子巨蟹狮子处女天秤天蝎射手魔羯";
	    
	    int[] arr={20,19,21,21,21,22,23,23,23,23,22,22};
	    
	    int num = month*2-(day<arr[month-1]?2:0);
	    
	    return s.substring(num,num+2)+"座";
	}
	
	/**
	 * 获得年龄
	 * @param birthday
	 * @return
	 */
	public static int getAge(Date birthday) {
		return (new Date().getYear() - birthday.getYear());
	}
	
	/**
	 * 获得属相
	 * @param birthday
	 * @return
	 */
	public static String getZodiac(Date birthday) {
		
		String name[] = new String[12];
	    name[0]="鼠";name[1]="牛";name[2]="虎";
	    name[3]="兔";name[4]="龙";name[5]="蛇";
	    name[6]="马";name[7]="羊";name[8]="猴";
	    name[9]="鸡";name[10]="狗";name[11]="猪";
		
		return name[birthday.getYear()%12];
	}

}
