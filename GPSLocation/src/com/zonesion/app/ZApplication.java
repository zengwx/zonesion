package com.zonesion.app;

import java.util.ArrayList;

import android.app.Application;
import android.widget.Toast;

import com.zhiyun360.wsn.droid.WSNRTConnect;
import com.zhiyun360.wsn.droid.WSNRTConnectListener;

// Developer:zengwx
// Date:2015-08-03

public class ZApplication extends Application implements WSNRTConnectListener {
	private WSNRTConnect mWSNRTConnect;// ��������

	// �����ͻ�ȡ���ӵķ���
	public WSNRTConnect getWSNRTConnect() {
		if (mWSNRTConnect == null) {
			mWSNRTConnect = new WSNRTConnect();// ��������
		}
		return mWSNRTConnect;// ���ش���������
	}

	// Application����ʱ����ø÷���
	@Override
	public void onCreate() {
		super.onCreate();
		getWSNRTConnect();// ��������
		mWSNRTConnect.setRTConnectListener(this);// ���ü�����
	}

	// ��Ϣ����ʱ���Զ����ø÷���
	@Override
	public void onMessageArrive(String mac, byte[] data) {// data���ݸ�ʽ:{XX=XXX,XX=XXX,...}
		// ��������
		if (data[0] == '{' && data[data.length - 1] == '}') {
			String sData = new String(data, 1, data.length - 2);// sData���ݸ�ʽ:XX=XXX,XX=XXX,...
			String[] pDatas = sData.split(",");// ��","�з��ַ���
			for (String pData : pDatas) {// pData���ݸ�ʽ:XX=XXX
				String[] tagVal = pData.split("=");// ��"="�з��ַ���
				if (tagVal.length == 2) {
					for (IOnWSNDataListener li : mIOnSensorDataListeners) {
						// ʵ����IOnSensorDataListener���������ݼ����ӿڵ��඼���Զ�����onSensorData()����
						li.onMessageArrive(mac, tagVal[0], tagVal[1]);
					}
				}
			}
		}
	}

	// ���ӳɹ�ʱ���Զ����ø÷���
	@Override
	public void onConnect() {
		Toast.makeText(ZApplication.this, "�������ӳɹ�", Toast.LENGTH_SHORT).show();
		for (IOnWSNDataListener li : mIOnSensorDataListeners) {
			// ʵ����IOnSensorDataListener���������ݼ����ӿڵ��඼���Զ�����onConnect()����
			li.onConnect();
		}
	}

	// ���ӶϿ�ʱ���Զ����ø÷���
	@Override
	public void onConnectLost(Throwable arg0) {
		Toast.makeText(ZApplication.this, "�������ӶϿ�", Toast.LENGTH_SHORT).show();
		for (IOnWSNDataListener li : mIOnSensorDataListeners) {
			// ʵ����IOnSensorDataListener���������ݼ����ӿڵ��඼���Զ�����onConnectLost()����
			li.onConnectLost();
		}
	}

	// ���������ݼ���������
	private ArrayList<IOnWSNDataListener> mIOnSensorDataListeners = new ArrayList<IOnWSNDataListener>();

	// ע�ᴫ�������ݼ�����
	public void registerOnSensorDataListener(IOnWSNDataListener li) {
		mIOnSensorDataListeners.add(li);
	}

	// ȡ��ע�ᴫ�������ݼ�����
	public void unregisterOnSensorDataListener(IOnWSNDataListener li) {
		mIOnSensorDataListeners.remove(li);
	}
}