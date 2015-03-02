package com.ltt.findyou.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.ltt.findyou.MainActivity;
import com.ltt.findyou.service.SendLocationService;

public class staticReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		System.out.println("进入静态广播，判断服务是否开启----");
		Log.i("广播名称", intent.getAction());
		MainActivity.startAlerm(context);
	}

}
