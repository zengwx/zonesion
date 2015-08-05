package com.zonesion.app;

// Developer:zengwx
// Date:2015-08-03

// 传感器数据监听器
public interface IOnWSNDataListener {
	// 消息到达
	void onMessageArrive(String mac, String tag, String val);

	// 连接成功
	public void onConnect();

	// 连接断开
	public void onConnectLost();
}