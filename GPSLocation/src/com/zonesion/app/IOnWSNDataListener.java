package com.zonesion.app;

// Developer:zengwx
// Date:2015-08-03

// ���������ݼ�����
public interface IOnWSNDataListener {
	// ��Ϣ����
	void onMessageArrive(String mac, String tag, String val);

	// ���ӳɹ�
	public void onConnect();

	// ���ӶϿ�
	public void onConnectLost();
}