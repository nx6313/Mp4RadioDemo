package com.nx.nx6313.mp4radio;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.widget.TextView;

/**
 * 工具类，处理字符串
 * 
 * @author lishaoqing
 * 
 */
public class StringHandler {

	/**
	 * 测试是否是Email地址
	 *
	 * @return 测试结果
	 */
	public static boolean testEmail(String email) {

		String regex = "^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(\\.([a-zA-Z0-9_-])+)+$";

		return email.matches(regex);
	}

	/**
	 * 测试是否为合法的用户名
	 *
	 * @return 测试结果
	 */
	public static boolean testUsername(String username) {

		String regex = "^[a-zA-Z][a-zA-Z0-9_]{3,11}$";

		return username.matches(regex);
	}

	/**
	 * 测试是否是Email地址
	 *
	 * @return 测试结果
	 */
	public static boolean testQQ(String qq) {

		String regex = "^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(\\.([a-zA-Z0-9_-])+)+$";

		String regex2 = "^[1-9][0-9]{4,11}$";

		return qq.matches(regex) || qq.matches(regex2);
	}
	
	/**
	 * 测试是否是身份证号码
	 * 
	 * @param idcard 身份证号码
	 * @return 测试结果
	 */
	public static boolean testIDCard(String idcard) {

		String regex1 = "^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$";
		String regex2 = "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{4}$";

		return idcard.matches(regex1) | idcard.matches(regex2);
	}
	
	/**
	 * 测试是否是手机号
	 * @param phone 手机号
	 * @return 测试结果
	 */
	public static boolean testPhone(String phone) {

		String regex = "^(1)[0-9]{10}$";
		// String regex = "^(13[0-9]|15[5-9]|153|152|145|147|18[0-9])[0-9]{8}$";

		return phone.matches(regex);
	}
	
	/**
	 * 获得价格小数点后2位的编码格式
	 * 
	 * @param price
	 *            价格
	 * @return 格式化后的价格
	 */
	public static String getPrice(float price) {
		String str = String.format("%.2f", price);
		if(str.endsWith(".00") || str.endsWith(".0")){
			str = str.substring(0, str.indexOf("."));
		} else if(str.contains(".") && str.endsWith("0")) {
			str = str.substring(0, str.length()-1);
		}
		return str;
	}
	
	/**
	 * 判断一个字符串是否为空
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		return null == str || str.equals("");
	}
	
	/**
	 * 格式化文件大小
	 * @param volume
	 *  			文件大小
	 * @return 格式化的字符
	 */
	public static String getVolume(long volume) {

		float num = 1.0F;

		String str = null;

		if (volume < 1024) {
			str = volume + "B";
		} else if (volume < 1048576) {
			num = num * volume / 1024;
			str = String.format("%.1f", num) + "K";
		} else if (volume < 1073741824) {
			num = num * volume / 1048576;
			str = String.format("%.1f", num) + "M";
		} else if (volume < 1099511627776L) {
			num = num * volume / 1073741824;
			str = String.format("%.1f", num) + "G";
		}

		return str;
	}
	
	/**
	 * 给TextView后面加一张图片
	 * @param tv
	 */
	public static void loadSmileySpans(TextView tv, int drawable_id) {
		
		String text = tv.getText().toString() + "_";
		
		SpannableString ss = new SpannableString(text);
		
		Drawable drawable = tv.getResources().getDrawable(drawable_id); 
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
		ss.setSpan(span, text.length() - 1, text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE); 
		
		tv.setText(ss);
	}
	
	/**
	 * 给TextView设置文本，替换为指定格式
	 * @param tv
	 * @param title
	 * @param content
	 */
	public static void loadWikiContent(TextView tv, String title, String content) {
		
		if(StringHandler.isEmpty(title))
			title = "……";
		if(StringHandler.isEmpty(content)){
			content = "……";
		}
		
		SpannableString ss = new SpannableString(title + "\n" + content 
				+ "\n" + "　　　　　　　　　　　阅读详细");
		
		// 内容的字体颜色
		ss.setSpan(new ForegroundColorSpan(0xff999999), title.length() + 1, 
				title.length() + content.length() + 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		// 内容的字体大小
		ss.setSpan(new RelativeSizeSpan(0.8F), title.length() + 1, 
				title.length() + content.length() + 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		
		// 阅读详细的颜色和大小
		ss.setSpan(new ForegroundColorSpan(0xff159af3), title.length() + content.length() + 12, 
				title.length() + content.length() + 17, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		ss.setSpan(new RelativeSizeSpan(0.85F), title.length() + content.length() + 12, 
				title.length() + content.length() + 17, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		
		tv.setText(ss);
	}
	
	private static final String regEx_img = "<img[\\s\\S]*?\\/>"; // 定义img的正则表达式
	private static final String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式
    private static final String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式
    private static final String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
    
    /**
     * 格式化HTML标签
     * @param htmlStr
     * @return
     */
    public static String delHTMLTag(String htmlStr) {
    	
    	Pattern p_img = Pattern.compile(regEx_img, Pattern.CASE_INSENSITIVE);
        Matcher m_img = p_img.matcher(htmlStr);
        htmlStr = m_img.replaceAll("[图片]"); // 过滤img标签
    	
        Pattern p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll(""); // 过滤script标签

        Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll(""); // 过滤style标签

        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll(""); // 过滤html标签

        htmlStr = htmlStr.replaceAll("\r\n", " ").replaceAll("\r", " ").replaceAll("\n", " ").trim();
        
        while(htmlStr.indexOf("  ") != -1){
        	htmlStr = htmlStr.replaceAll("  ", " ");
        }
        
        return htmlStr;// 返回文本字符串
    }
}