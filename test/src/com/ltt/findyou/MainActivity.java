package com.ltt.findyou;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import util.Constant;
import util.GetFlow;
import util.Netc;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.google.gson.Gson;
import com.ltt.findyou.adapter.DetailAdapter;
import com.ltt.findyou.app.MyApplication;
import com.ltt.findyou.bean.Find;
import com.ltt.findyou.service.SendLocationService;

@SuppressLint("NewApi")
public class MainActivity extends Activity {

	MapView mMapView = null;
	BaiduMap mBaiduMap = null;
	public LocationClient mLocationClient;
	private List<Find> findList = new ArrayList<Find>();
	private EditText name;
	private Button search, detail;
	private String searchName;
	AlertDialog.Builder builder;
	AlertDialog alertDialog;
	private DetailAdapter detailAdapter;
	private Context context;
	private MyApplication myApplication;
	private ProgressDialog dialog;
	private Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏
		context = this;
		myApplication = (MyApplication) getApplication();
		setContentView(R.layout.main);
		// 获取地图控件引用
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		initView();
		if (myApplication.userName!=null) {
			myApplication.SetBusNum(myApplication.userName);
		}
		startAlerm(context);
	}

	@SuppressLint("ShowToast")
	private void initView() {
		name = (EditText) findViewById(R.id.name);
		search = (Button) findViewById(R.id.search);
		search.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager m = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);// 切换软键盘的打开与官兵
				m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				searchName = name.getText().toString();
				if (searchName.equals("")) {
					Toast.makeText(context, "请先输入要查询的姓名！", 2000).show();
					return;
				}
				if (Netc.getAPNType(context) == -1) {
					creatDialog();
				} else {
					dialog = ProgressDialog.show(context, "提示", "正在查询,请稍后...", true, false);
					new myAsyncTask().execute("");
				}

			}
		});
		detail = (Button) findViewById(R.id.detail);
		detail.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (findList.size() == 0) {
					Toast.makeText(context, "搜索结果为空，或还没有进行搜索！", 2000).show();
					return;
				}

				builder = new AlertDialog.Builder(MainActivity.this);
				detailAdapter = new DetailAdapter(MainActivity.this, findList);

				builder.setAdapter(detailAdapter, null);
				builder.setTitle("地点详情--今天共" + findList.size() + "条记录");
				alertDialog = builder.create();
				alertDialog.show();
			}
		});

	}

	@Override
	protected void onDestroy() {
		// // 退出时销毁定位
		// mLocationClient.stop();
		// // 关闭定位图层
		// mBaiduMap.setMyLocationEnabled(false);
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		// mMapView.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		// mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// // 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		// mMapView.onPause();
	}

	/**
	 * 将获取的最后一个位置定位到地图上
	 * 
	 * @Title: drawInMap
	 * @return void 返回类型
	 * @author 李彤彤
	 * @date 2015年1月21日 下午1:18:07
	 */
	private void drawInMap(double lat, double longit, float radius) {
		MyLocationData locData = new MyLocationData.Builder().accuracy(radius)
		// 此处设置开发者获取到的方向信息，顺时针0-360
				.direction(100).latitude(lat).longitude(longit).build();
		mBaiduMap.setMyLocationData(locData);
		LatLng ll = new LatLng(lat, longit);
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
		mBaiduMap.animateMapStatus(u);

	}

	/**
	 * 将制定图标画到地图上
	 * 
	 * @Title: drawPictureInMap
	 * @return void 返回类型
	 * @author 李彤彤
	 * @date 2015年1月21日 下午5:00:04
	 */
	private void drawPictureInMap(LatLng lat, int pictureId) {
		// 添加图片
		BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(pictureId);
		// 构建MarkerOption，用于在地图上添加Marker
		OverlayOptions option = new MarkerOptions().position(lat).icon(bitmap);
		// 在地图上添加Marker，并显示
		mBaiduMap.addOverlay(option);
	}

	/**
	 * 创建弹出框，提示是否开启GPRS
	 * 
	 * @Title: creatDialog
	 * @return void 返回类型
	 * @author 李彤彤
	 * @date 2015年1月21日 下午1:07:59
	 */
	private void creatDialog() {
		builder = new AlertDialog.Builder(context);
		builder.setTitle("没有网络！");
		builder.setMessage("您确定打开GPRS流量吗，建议在有套餐时使用？");
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog = ProgressDialog.show(context, "提示", "正在处理,请稍后...", true, false);
				Netc.checkNet(context);// 检查是否有网，没有就打开GPRS网络
				new myAsyncTask().execute("");
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		alertDialog = builder.create();
		alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		alertDialog.show();
	}

	/**
	 * 从服务器获取坐标点的线程
	 * 
	 * @ClassName: myAsyncTask
	 * @author 李彤彤
	 * @date 2015年1月21日 下午1:17:25
	 */
	public class myAsyncTask extends AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {
			Netc.checkNet(context);// 检查是否有网，没有就打开GPRS网络
			myApplication.isSearch = 1;
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			long endTime = System.currentTimeMillis() + 5 * 1000;
			String result = null;
			while (System.currentTimeMillis() < endTime) {
				synchronized (this) {
					BufferedReader reader = null;
					try {
						HttpClient client = new DefaultHttpClient();
						HttpGet request = new HttpGet();
						request.setURI(new URI(Constant.OUT_NET_URL + "/gjdz/page/front/findyou.jsp?name=" + searchName));
						HttpResponse response = client.execute(request);
						reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
						StringBuffer strBuffer = new StringBuffer("");
						String line = null;
						while ((line = reader.readLine()) != null) {
							strBuffer.append(line);
						}
						result = strBuffer.toString();

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

			Gson gson = new Gson();
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(result);
				JSONArray gsonStr = new JSONArray(jsonObject.getString("result"));
				Find find = null;
				findList.clear();
				System.out.println(gsonStr.length());
				for (int i = 0; i < gsonStr.length(); i++) {
					JSONObject objitem = (JSONObject) gsonStr.get(i);
					find = new Find();
					find = gson.fromJson(objitem.toString(), Find.class);
					findList.add(find);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			dialog.dismiss();
			GetFlow getFlow = new GetFlow(myApplication);
			getFlow.sayFlow();// 输出一下花费的流量信息
			for (int i = 0; i < findList.size(); i++) {
				LatLng lat = new LatLng(Double.valueOf(findList.get(i).getWd()), Double.valueOf(findList.get(i).getJd()));
				float radiusf = Float.parseFloat(findList.get(i).getRadius());
				// 添加点
				if (i != (findList.size() - 1)) {
					OverlayOptions ooDot = new DotOptions().center(lat).radius(6).color(0xFF0000FF);
					mBaiduMap.addOverlay(ooDot);
				} else {
					drawInMap(Double.valueOf(findList.get(i).getWd()), Double.valueOf(findList.get(i).getJd()), radiusf);
				}
			}
			super.onPostExecute(result);
		}

	}

	/**
	 * 判断sendService是否已经启动
	 * 
	 * @author 李彤彤
	 * @date 2015年2月3日 下午3:35:11
	 */
	public static boolean isServiceRunning(Context mContext, String className) {

		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(30);

		if (!(serviceList.size() > 0)) {
			return false;
		}

		for (int i = 0; i < serviceList.size(); i++) {
			if (serviceList.get(i).service.getClassName().equals(className) == true) {
				isRunning = true;
				break;
			}
		}
		return isRunning;
	}

	public static void startAlerm(Context context) {
		// 获取AlarmManager系统服务
		AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		if (MainActivity.isServiceRunning(context, SendLocationService.class.getName())) {
			Log.i("isStart_类名", SendLocationService.class.getName());
		} else {
			Log.i("NOStart_类名", SendLocationService.class.getName());
			// 包装需要执行Service的Intent
			Intent intents = new Intent(context, SendLocationService.class);
			PendingIntent Intents = PendingIntent.getService(context, 0, intents, PendingIntent.FLAG_UPDATE_CURRENT);
			// 触发服务的起始时间
			long triggerAtTime = SystemClock.elapsedRealtime();
			// 使用AlarmManger的setRepeating方法设置定期执行的时间间隔（seconds秒）和需要执行的Service
			manager.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtTime, 30 * 60000, Intents);
		}
	}
}
