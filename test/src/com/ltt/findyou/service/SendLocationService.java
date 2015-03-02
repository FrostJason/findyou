package com.ltt.findyou.service;

import java.net.URI;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import util.Constant;
import util.GetFlow;
import util.Netc;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory.Options;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.ltt.findyou.MainActivity;
import com.ltt.findyou.app.MyApplication;
import com.ltt.findyou.receiver.BootReceiver;

/**
 * 定时获取坐标并发送到服务器的服务
 * 
 * @author 李彤彤
 * @date 2015年1月23日 上午9:06:15
 */
public class SendLocationService extends Service {

	private Context context;

	private HttpClient client = null;
	public MyLocationListener mMyLocationListener;
	public LocationClient mLocationClient;
	private String wd;
	private String jd;
	private String address;
	private String time;
	private String radius;
	private String name;
	private String netType;
	private MyApplication myApplication;
	private NetworkStateReceiver mNetWrokStateReceiver;
	private WakeLock wakeLock = null;

	// Handler handler = new Handler();
	// Runnable runnable = new Runnable() {
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	// start();
	// handler.postDelayed(this, 60000);
	// }
	// };

	@Override
	public void onCreate() {
		super.onCreate();
		context = this;
		client = new DefaultHttpClient();

		// 生成广播处理
		BootReceiver screenStatReceiver = new BootReceiver();
		// 实例化过滤器并设置要过滤的广播
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_SCREEN_ON);
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		intentFilter.addAction(Intent.ACTION_TIME_TICK); // 注册一个每分钟发送一次的广播
		// intentFilter.addAction(Intent.ACTION_USER_PRESENT);
		// 注册广播
		registerReceiver(screenStatReceiver, intentFilter);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		registerReceiver();// 注册网络状态的监听
		acquireWakeLock();// 开启电源锁，保证锁屏不断网
		boolean isOpen = Netc.isOPen(context);
		if (!isOpen) {
			Netc.openGPS(context);
		}
		myApplication = (MyApplication) getApplication();
		if (myApplication.userName!=null) {
			myApplication.SetBusNum(myApplication.userName);
		}
		name = myApplication.GetBusNum();
		if(name.equals("kong")||name==null){
			name=myApplication.getPhoneModel();
		}
		start();
		// handler.postDelayed(runnable, 60000);// x秒后执行一次runnable.
		flags = START_STICKY;// 手动返回START_STICKY，亲测当service因内存不足被kill，当内存又有的时候，service又被重新创建，比较不错，但是不能保证任何情况下都被重建，比如进程被干掉了....

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		MainActivity.startAlerm(context);
		super.onDestroy();
	}

	public class myAsyncTask extends AsyncTask<String, String, String> {
		@Override
		protected void onPreExecute() {
			netType = Netc.checkNet(context);// 检查是否有网，没有就打开GPRS网络
			// Log.i("appUserName", name);
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet();
				request.setURI(new URI(Constant.OUT_NET_URL + "/gjdz/page/front/findyou.jsp?name=" + name + "&wd=" + wd + "&jd=" + jd + "&radius="
						+ radius + "&address=" + address + "&netType=" + netType));
				Log.i("提交的数据：", name + "--" + wd + "--" + jd + "--" + radius + "--" + address + "--" + netType);
				client.execute(request);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				myApplication.flag = 0;
				if (myApplication.isSearch == 0) {

					Netc.toggleMobileData(context, false);// 关闭移动网络

				}

				Log.i("以下是发送位置信息后的流量信息：", "=============================================================");
				GetFlow getFlow = new GetFlow(myApplication);
				getFlow.sayFlow();// 输出一下花费的流量信息
			}
			return null;
		}

	}

	/**
	 * 实现实位回调监听
	 */
	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			address = "";
			wd = "";
			jd = "";
			address = location.getAddrStr();
			time = location.getTime();
			wd = location.getLatitude() + "";
			jd = location.getLongitude() + "";
			// netType = location.getNetworkLocationType();
			System.out.println(wd + "--------" + jd);
			radius = location.getRadius() + "";
			if (wd != null && !wd.equals("") && jd != null && !jd.equals("")) {
				new myAsyncTask().execute("");
				Log.i("WEIZHI", location.getLatitude() + "," + location.getLongitude());
			}

		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * 注册监听网络状态是否改变的监听
	 * 
	 * @Title: registerReceiver
	 * @return void 返回类型
	 * @author 李彤彤
	 * @date 2015年1月22日 上午9:48:35
	 */
	public void registerReceiver() {
		mNetWrokStateReceiver = new NetworkStateReceiver();
		// 注册网络监听
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mNetWrokStateReceiver, filter);
	}

	/**
	 * 网络状态改变监听
	 * 
	 * @ClassName: NetworkStateReceiver
	 * @author 李彤彤
	 * @date 2015年1月22日 上午9:49:05
	 */
	class NetworkStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.e("TAG", "网络状态改变");
			// boolean success = false;
			// // 获得网络连接服务
			// ConnectivityManager connManager = (ConnectivityManager)
			// getSystemService(CONNECTIVITY_SERVICE);
			// // State state = connManager.getActiveNetworkInfo().getState();
			// State state =
			// connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
			// // 获取WIFI网络连接状态
			// if (State.CONNECTED == state) { // 判断是否正在使用WIFI网络
			// success = true;
			// }
			// state =
			// connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
			// // 获取网络连接状态
			// if (State.CONNECTED != state) { // 判断是否正在使用GPRS网络
			// success = true;
			// }
			// if (!success) {
			// //Toast.makeText(LocationMapActivity.this, "您的网络连接已中断",
			// Toast.LENGTH_LONG).show();
			// }
			int type = Netc.getAPNType(context);
			Log.i("当初网络", type + "");

		}

	}

	/**
	 * 获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
	 * 
	 * @Title: acquireWakeLock
	 * @return void 返回类型
	 * @author 李彤彤
	 * @date 2015年1月22日 上午10:53:20
	 */
	private void acquireWakeLock() {
		if (null == wakeLock) {
			PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "PostLocationService");
			if (null != wakeLock) {
				wakeLock.acquire();
			}
		}
	}

	/**
	 * 释放设备电源锁
	 * 
	 * @author 李彤彤
	 * @date 2015年1月23日 上午9:05:38
	 */
	private void releaseWakeLock() {
		if (null != wakeLock) {
			wakeLock.release();
			wakeLock = null;
		}
	}

	/**
	 * 开始定位
	 * 
	 * @author 李彤彤
	 * @date 2015年2月3日 下午3:09:29
	 */
	public void start() {
		mLocationClient = new LocationClient(context);
		mMyLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mMyLocationListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开GPS
		option.setCoorType("bd09ll"); // bd09ll 返回的定位结果是百度经纬度，默认值gcj02
		option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
		// option.setScanSpan(Constant.MINUTE * 2);// 设置多久获取一次坐标
		option.setIsNeedAddress(true);// 是否需要地址信息
		// 设置产品线名称。强烈建议您使用自定义的产品线名称，方便我们以后为您提供更高效准确的定位服务。
		option.setProdName("通过GPS定位我当前的位置");
		// 设置定位方式的优先级。
		// 当gps可用，而且获取了定位结果时，不再发起网络请求，直接返回给用户坐标。这个选项适合希望得到准确坐标位置的用户。如果gps不可用，再发起网络请求，进行定位。
		// option.setPriority(LocationClientOption.GpsFirst);

		// 需要地址信息，设置为其他任何值（string类型，且不能为null）时，都表示无地址信息。
		try {
			option.setAddrType("all");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		netType = Netc.checkNet(context);// 检查是否有网，没有就打开GPRS网络

		mLocationClient.setLocOption(option);
		mLocationClient.start();
		myApplication.flag = 1;// 表示开始获取坐标信息中，，，，获取完毕将值改为0
	}


	@Override
	public void onStart(Intent intent, int startId) {
		// 再次动态注册广播
//		IntentFilter localIntentFilter = new IntentFilter("android.intent.action.USER_PRESENT");
//		localIntentFilter.setPriority(Integer.MAX_VALUE);// 整形最大值
//		BootReceiver searchReceiver = new BootReceiver();
//		registerReceiver(searchReceiver, localIntentFilter);

		super.onStart(intent, startId);
	}
}
