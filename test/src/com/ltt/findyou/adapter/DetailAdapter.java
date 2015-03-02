package com.ltt.findyou.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ltt.findyou.R;
import com.ltt.findyou.bean.Find;

public class DetailAdapter extends BaseAdapter{
	private Context mContext;
	// xml转View对象
	private LayoutInflater mInflater;
	private List<Find> dataList;
	private Map<Integer, String> map=new HashMap<Integer,String>();
	
	public DetailAdapter(Context context,List<Find> dataList){
		this.mContext=context;
		this.dataList=dataList;
		this.mInflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return dataList.size();
	}

	@Override
	public Object getItem(int position) {
		return dataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			// 使用自定义的list_items作为Layout
			convertView = mInflater.inflate(R.layout.detail_item, parent, false);
			// 减少findView的次数
			holder = new ViewHolder();
			holder.names = (TextView) convertView.findViewById(R.id.name);
			holder.jd = (TextView) convertView.findViewById(R.id.jd);
			holder.wd = (TextView) convertView.findViewById(R.id.wd);
			holder.time = (TextView) convertView.findViewById(R.id.time);
			holder.address = (TextView) convertView.findViewById(R.id.address);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		Find find=dataList.get(position);
		String dd = find.getName();
		holder.names.setText("姓名:"+dd);
		holder.jd.setText("经度:"+find.getJd());
		holder.wd.setText("纬度:"+find.getWd());
		holder.time.setText("时间:"+find.getTime());
		holder.address.setText("地址:"+find.getAddress());
		return convertView;
	}
	
	static class ViewHolder{
		public TextView names,jd,wd,time,address;
	}

}
