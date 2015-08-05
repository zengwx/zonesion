package com.zonesion.app;

import java.util.ArrayList;

import android.app.Application;
import android.widget.Toast;

import com.zhiyun360.wsn.droid.WSNRTConnect;
import com.zhiyun360.wsn.droid.WSNRTConnectListener;

//Developer:zengwx
//Date:2015-08-03

public class ZApplication extends Application implements WSNRTConnectListener {
	private WSNRTConnect mWSNRTConnect;// 定义连接

	// 创建和获取连接的方法
	public WSNRTConnect getWSNRTConnect() {
		if (mWSNRTConnect == null) {
			mWSNRTConnect = new WSNRTConnect();// 创建连接
		}
		return mWSNRTConnect;// 返回创建的连接
	}

	// Application创建时会调用该方法
	@Override
	public void onCreate() {
		super.onCreate();
		getWSNRTConnect();// 创建连接
		mWSNRTConnect.setRTConnectListener(this);// 设置监听器
	}

	// 消息到达时会自动调用该方法
	@Override
	public void onMessageArrive(String mac, byte[] data) {// data数据格式:{XX=XXX,XX=XXX,...}
		// 解析数据
		if (data[0] == '{' && data[data.length - 1] == '}') {
			String sData = new String(data, 1, data.length - 2);// sData数据格式:XX=XXX,XX=XXX,...
			String[] pDatas = sData.split(",");// 用","切分字符串
			for (String pData : pDatas) {// pData数据格式:XX=XXX
				String[] tagVal = pData.split("=");// 用"="切分字符串
				if (tagVal.length == 2) {
					for (IOnWSNDataListener li : mIOnSensorDataListeners) {
						// 实现了IOnSensorDataListener传感器数据监听接口的类都会自动调用onSensorData()方法
						li.onMessageArrive(mac, tagVal[0], tagVal[1]);
					}
				}
			}
		}
	}

	// 连接成功时会自动调用该方法
	@Override
	public void onConnect() {
		Toast.makeText(ZApplication.this, "网关连接成功", Toast.LENGTH_SHORT).show();
		for (IOnWSNDataListener li : mIOnSensorDataListeners) {
			// 实现了IOnSensorDataListener传感器数据监听接口的类都会自动调用onConnect()方法
			li.onConnect();
		}
	}

	// 连接断开时会自动调用该方法
	@Override
	public void onConnectLost(Throwable arg0) {
		Toast.makeText(ZApplication.this, "网关连接断开", Toast.LENGTH_SHORT).show();
		for (IOnWSNDataListener li : mIOnSensorDataListeners) {
			// 实现了IOnSensorDataListener传感器数据监听接口的类都会自动调用onConnectLost()方法
			li.onConnectLost();
		}
	}

	// 传感器数据监听器数组
	private ArrayList<IOnWSNDataListener> mIOnSensorDataListeners = new ArrayList<IOnWSNDataListener>();

	// 注册传感器数据监听器
	public void registerOnSensorDataListener(IOnWSNDataListener li) {
		mIOnSensorDataListeners.add(li);
	}

	// 取消注册传感器数据监听器
	public void unregisterOnSensorDataListener(IOnWSNDataListener li) {
		mIOnSensorDataListeners.remove(li);
	}
}