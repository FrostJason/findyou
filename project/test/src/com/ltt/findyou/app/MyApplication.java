package com.ltt.findyou.app;

import com.baidu.mapapi.SDKInitializer;
import com.ltt.findyou.receiver.BootReceiver;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class MyApplication extends Application {

	private static MyApplication instance;
	public int flag = 0;// 判断发送和接受是否都完成，完成关闭GPRS
	public int isSearch = 0;// 判断是否进行了搜索，即是否开启了查询线程
	public String userName;
	public int uid;

	public static MyApplication getInstance() {
		if (null == instance) {
			instance = new MyApplication();
		}
		return instance;

	}

	@Override
	public void onCreate() {
		super.onCreate();
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		// 注意：在SDK各功能组件使用之前都需要调用该方法，因此我们建议该方法放在Application的初始化方法中
		SDKInitializer.initialize(getApplicationContext());
		getUid();
		System.out.println(getPhoneModel());
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());

	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	private void getUid() {
		try {
			PackageManager pm = getPackageManager();
			ApplicationInfo ai = pm.getApplicationInfo("com.ltt.findyou", PackageManager.GET_ACTIVITIES);
			Log.d("!!", "!!uid=" + ai.uid);
			uid = ai.uid;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取手机型号，版本号
	 * 
	 * @author 李彤彤
	 * @date 2015年2月5日 下午4:42:55
	 */
	public String getPhoneModel() {
		String model = android.os.Build.MODEL + "," + android.os.Build.VERSION.SDK + "," + android.os.Build.VERSION.RELEASE;
		return model;
	}

	public void SetBusNum(String busnumber) {
		SharedPreferences mySharedPreferences = getSharedPreferences("name", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		editor.putString("name", busnumber);
		editor.commit();
	}

	public String GetBusNum() {
		SharedPreferences sharedPreferences = getSharedPreferences("name", Context.MODE_PRIVATE);
		String busnmu = sharedPreferences.getString("name", "");
		if (busnmu.equals("") || busnmu == null) {
			return "kong";
		} else {
			return busnmu;
		}
	}
}
