package com.nx.nx6313.mp4radio;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

/**
 * 临时会话类，短时间存储
 * 
 * @author Li Shaoqing
 *
 */
public class Session {
	
	/**
	 * 屏幕的密度（0.75 / 1.0 / 1.5）
	 */
	public static final String DENSITY = "screen_density";
	
	/**
	 * 屏幕的密度DPI（120 / 160 / 240）
	 */
	public static final String DENSITY_DPI = "screen_density_dpi";
	
	/**
	 * 屏幕的宽
	 */
	public static final String WIDTH = "screen_width";
	
	/**
	 * 屏幕的高
	 */
	public static final String HEIGHT = "screen_height";
	
	/**
	 * 设备ID
	 */
	public static final String DEVICE_ID = "DeviceId";
	
	/**
	 * 手机机型
	 */
	public static final String MODEL = "MODEL";
	
	/**
	 * 系统版本名称
	 */
	public static final String SYSTEM_VERSION_NAME = "SYSTEM_VERSION_NAME";
	
	/**
	 * 系统版本号
	 */
	public static final String SYSTEM_VERSION_CODE = "SYSTEM_VERSION_CODE";
	
	/**
	 * 本应用版本名称
	 */
	public static final String VERSION_NAME = "VERSION_NAME";
	
	/**
	 * 本应用版本号
	 */
	public static final String VERSION_CODE = "VERSION_CODE";

	private static Map<String, Object> map;	//短时间的数据集合
	
	private static Session session;

	private Session() {
		map = new HashMap<String, Object>();
	}

	public static Session getSession() {
		if(session == null){
			session = new Session();
		}
		return session;
	}

	public void put(String key, Object value) {
		map.put(key, value);
	}

	public boolean contains(String key) {
		return map.containsKey(key);
	}

	public Object get(String key) {
		return map.get(key);
	}

	public void remove(String key) {
		map.remove(key);
	}

	public void removeAll() {
		map.clear();
	}
	
	public void save(Bundle outState) {
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()){
			Entry en = (Entry) it.next();
			String key = (String) en.getKey();
			if(map.get(key) instanceof Serializable 
					&& !(map.get(key) instanceof List)
					&& !(map.get(key) instanceof Map))
				outState.putSerializable(key, (Serializable)map.get(key));
		}
	}
	
	public void restore(Bundle inState) {
		if(inState != null){
			Set<String> keys = inState.keySet();
			for (String key: keys) {
				if(!map.containsKey(key) || map.get(key) == null)
					map.put(key, inState.get(key));
			}
		}
	}

	/**
	 * 初始化一些界面数据
	 * @param activity
	 */
	@SuppressLint("NewApi")
	public void initialize(Activity activity){
		DisplayMetrics metric = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;  // 屏幕宽度（像素）
        int height = metric.heightPixels;  // 屏幕高度（像素）
        float density = metric.density;  // 屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = metric.densityDpi;  // 屏幕密度DPI（120 / 160 / 240）
        map.put(DENSITY, density);
        map.put(WIDTH, width);
        map.put(HEIGHT, height);
        map.put(DENSITY_DPI, densityDpi);
//        TelephonyManager tm = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
//        if(StringHandler.isEmpty("NX1234")){
//        	WifiManager wifi = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        	WifiInfo info = wifi.getConnectionInfo();
//        	if(StringHandler.isEmpty(info.getMacAddress())){
//        		String str = "" + Build.BOARD + Build.MODEL + Build.VERSION.RELEASE
//        				+ Build.BRAND + Build.TIME + Build.MANUFACTURER + Build.ID
//        				+ Build.DEVICE + Build.SERIAL;
//        		map.put(DEVICE_ID, MD5.md5(str));
//        	} else {
//        		map.put(DEVICE_ID, MD5.md5(info.getMacAddress())); // 使用mac地址代替DEVICE_ID
//        	}
//        } else {
        	map.put(DEVICE_ID, MD5.md5("NX1234")); // DEVICE_ID
//        }
        map.put(MODEL, android.os.Build.MODEL);	// 手机机型
        map.put(SYSTEM_VERSION_NAME, android.os.Build.VERSION.RELEASE);	// 操作系统版本名称，如：4.4
        map.put(SYSTEM_VERSION_CODE, android.os.Build.VERSION.SDK_INT);	// 操作系统版本号，如：21
		try {
			PackageManager pm = activity.getPackageManager();
			PackageInfo packInfo = pm.getPackageInfo(activity.getPackageName(),0);
	        map.put(VERSION_NAME, packInfo.versionName);	// 本应用版本名称
	        map.put(VERSION_CODE, packInfo.versionCode);	// 本应用版本号
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
}