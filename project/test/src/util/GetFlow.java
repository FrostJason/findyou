package util;

import com.ltt.findyou.app.MyApplication;

import android.net.TrafficStats;
import android.util.Log;

public class GetFlow {
	private MyApplication myApplication;

	public GetFlow(MyApplication myApplication) {
		this.myApplication = myApplication;
	}

	public void sayFlow() {
		/** 获取手机通过 2G/3G 接收的字节流量总数 */
		Log.i(" 2G/3G 接收的字节流量总数:", TrafficStats.getMobileRxBytes() + "");

		/** 获取手机通过 2G/3G 接收的数据包总数 */
		Log.i("2G/3G 接收的数据包总数:", TrafficStats.getMobileRxPackets() + "");

		/** 获取手机通过 2G/3G 发出的字节流量总数 */
		Log.i("2G/3G 发出的字节流量总数:", TrafficStats.getMobileTxBytes() + "");

		/** 获取手机通过 2G/3G 发出的数据包总数 */

		Log.i("2G/3G 发出的数据包总数:", TrafficStats.getMobileTxPackets() + "");

		/** 获取手机指定 UID 对应的应程序用通过所有网络方式接收的字节流量总数(包括 wifi) */

		Log.i("指定 UID 对应的应程序用通过所有网络方式接收的字节流量总数:", TrafficStats.getUidRxBytes(myApplication.uid) + "");
		/** 获取手机指定 UID 对应的应用程序通过所有网络方式发送的字节流量总数(包括 wifi) */

		Log.i("指定 UID 对应的应用程序通过所有网络方式发送的字节流量总数:", TrafficStats.getUidTxBytes(myApplication.uid) + "");
	}

}
