package com.ltt.findyou.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import util.Constant;
import util.Netc;

import com.google.gson.Gson;
import com.ltt.findyou.app.MyApplication;
import com.ltt.findyou.bean.Find;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.IntentService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.style.BulletSpan;
import android.util.Log;
import android.view.WindowManager;

public class GetLocationService extends IntentService {
	private String name;

	public GetLocationService() {
		super("tt");
		// TODO Auto-generated constructor stub
	}

	private HttpClient client = null;
	private List<Find> findList = new ArrayList<Find>();
	private MyApplication myApplication;
	private Context context;
	private String netType;
	AlertDialog.Builder builder;
    AlertDialog alertDialog;
	@Override
	public void onCreate() {
		super.onCreate();
		myApplication = (MyApplication) getApplication();
		context = getApplicationContext();
		client = new DefaultHttpClient();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		name = intent.getStringExtra("name");
		myApplication.isSearch = 1;
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// Normally we would do some work here, like download a file.
		// For our sample, we just sleep for 5 seconds.
		long endTime = System.currentTimeMillis() + 5 * 1000;
		while (System.currentTimeMillis() < endTime) {
			synchronized (this) {
				String result = null;
				BufferedReader reader = null;
				try {
					HttpClient client = new DefaultHttpClient();
					HttpGet request = new HttpGet();
					request.setURI(new URI(Constant.LOCAL_URL + "/gjdz/page/front/findyou.jsp?name=" + name));
					HttpResponse response = client.execute(request);
					reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
					StringBuffer strBuffer = new StringBuffer("");
					String line = null;
					while ((line = reader.readLine()) != null) {
						strBuffer.append(line);
					}
					result = strBuffer.toString();
					Gson gson = new Gson();
					JSONObject jsonObject = new JSONObject(result);
					JSONArray gsonStr = new JSONArray(jsonObject.getString("result"));
					Find find = null;
					for (int i = 0; i < gsonStr.length(); i++) {
						JSONObject objitem = (JSONObject) gsonStr.get(i);
						find = new Find();
						find = gson.fromJson(objitem.toString(), Find.class);
						findList.add(find);
					}
					Object dd = (Object) findList;
					Intent broadcastIntent = new Intent();
					broadcastIntent.setAction("haha");
					broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
					Bundle bun = new Bundle();
					bun.putSerializable("find", (Serializable) findList);
					broadcastIntent.putExtras(bun);
					sendBroadcast(broadcastIntent);

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					myApplication.isSearch = 0;
					if (myApplication.flag == 0) {
						// 关闭移动网络
						Netc.toggleMobileData(context, false);
					}
					if (reader != null) {
						try {
							reader.close();
							reader = null;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		client.getConnectionManager().shutdown();
		super.onDestroy();
	}

}
