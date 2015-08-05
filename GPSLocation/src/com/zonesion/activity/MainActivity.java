package com.zonesion.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;
import com.zhiyun360.wsn.droid.WSNRTConnect;
import com.zonesion.app.IOnWSNDataListener;
import com.zonesion.app.ZApplication;
import com.zonesion.gpslocation.R;

// Developer:zengwx
// Date:2015-08-03

public class MainActivity extends Activity implements IOnWSNDataListener {
	private MapView mMapView;// �ٶȵ�ͼ��ʾ�ؼ�
	private BaiduMap mBaiduMap;// �ٶȵ�ͼ����
	private Marker mMarkerGateWay;// ������
	private InfoWindow mInfoWindow;// ��Ϣ����
	private BitmapDescriptor bdMarker01, bdMarker02;// λͼ����
	private TextView tvLongitude, tvLatitude, tvGateWay;// �ı���ʾ�ؼ�

	// Ӧ�ú�����
	private ZApplication mApplication;
	private WSNRTConnect mWSNRTConnect;

	private String ID = "1214";// �û��˺�
	private String KEY = "1214";// �û���Կ

	private static final String GPS_ADDRESS = "GPS:0";// GPS��ַ
	private double dLongitude, dLatitude;// ����,γ��

	// ����Activityʱ���Զ����ø÷���
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);

		mApplication = (ZApplication) getApplication();// ��ȡӦ��
		mWSNRTConnect = mApplication.getWSNRTConnect();// ��ȡ����
		mWSNRTConnect.setIdKey(ID, KEY);// �����û�ID����Կ
		mWSNRTConnect.setServerAddr("zhiyun360.com:28081");// �������Ʒ����ַ
		mApplication.registerOnSensorDataListener(this);// ע�����
		mWSNRTConnect.connect();// ��������

		// ��ʼ��λͼ��Դ
		bdMarker01 = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_marker01);
		bdMarker02 = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_marker02);
		// ��ʼ����ͼ��ʾ�ؼ�,��ȡ�ٶȵ�ͼ����
		mMapView = (MapView) findViewById(R.id.mvRight);
		mBaiduMap = mMapView.getMap();

		// ��ʼ����γ���ı���ʾ�ؼ�
		tvLongitude = (TextView) findViewById(R.id.tvLongitude);
		tvLatitude = (TextView) findViewById(R.id.tvLatitude);
		// tvGateWay��ʾ������Ϣ
		tvGateWay = new TextView(getApplicationContext());
		tvGateWay.setBackgroundColor(Color.WHITE);
		tvGateWay.setTextColor(Color.BLACK);
		// Ϊ�ٶȵ�ͼ���ø�������������
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			// ���������¼�
			@Override
			public boolean onMarkerClick(Marker marker) {
				if (marker == mMarkerGateWay) {
					tvGateWay.setText("ID:" + ID);
					LatLng latLng = marker.getPosition();
					mInfoWindow = new InfoWindow(tvGateWay, latLng, -50);// -50Ϊƫ��λ��,�ڸ������Ϸ�50�����ش���ʾ
					mBaiduMap.showInfoWindow(mInfoWindow);
				}
				return true;
			}
		});

	}

	// ����Activityʱ���Զ����ø÷���
	@Override
	protected void onDestroy() {
		mApplication.unregisterOnSensorDataListener(this);// �Ƴ�����
		mWSNRTConnect.disconnect();// �Ͽ�����
		super.onDestroy();
	}

	// ���Ƶ�ͼ
	private void drawMapView(double latitude, double longitude) {
		LatLng latlng = new LatLng(latitude, longitude);
		latlng = GPSCoord2BaiduCoord(latlng);// Ӳ���ɼ���GPS����ת���ɰٶ�����

		MapStatus mMapStatus = new MapStatus.Builder().target(latlng).zoom(17)
				.build();
		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory
				.newMapStatus(mMapStatus);
		mBaiduMap.setMapStatus(mMapStatusUpdate);

		mBaiduMap.clear();// ����µĸ�����֮ǰ���֮ǰ���еĸ�����
		// ��Ӱٶȵ�ͼ������
		ArrayList<BitmapDescriptor> giflist = new ArrayList<BitmapDescriptor>();
		giflist.add(bdMarker01);
		giflist.add(bdMarker02);
		OverlayOptions ooGateWay = new MarkerOptions().position(latlng)
				.icons(giflist).zIndex(0).period(20);
		mMarkerGateWay = (Marker) (mBaiduMap.addOverlay(ooGateWay));
	}

	// ��λ���ذ�ť����¼�
	public void locGateWay(View view) {
		Toast.makeText(this, "{A0=?,A1=?}", Toast.LENGTH_SHORT).show();
		mWSNRTConnect.sendMessage(GPS_ADDRESS, "{A0=?,A1=?}".getBytes());
	}

	// ����ڵ㷢�͹�������Ϣ
	@Override
	public void onMessageArrive(String mac, String tag, String val) {
		if (GPS_ADDRESS.equalsIgnoreCase(mac)) {
			if (tag.equals("A0")) {
				tvLongitude.setText(val);
				dLongitude = Double.parseDouble(val);
			} else if (tag.equals("A1")) {
				tvLatitude.setText(val);
				dLatitude = Double.parseDouble(val);
			}
			drawMapView(dLatitude, dLongitude);// ���û��Ƶ�ͼ����
		}
	}

	// ���ӳɹ�
	@Override
	public void onConnect() {
		mWSNRTConnect.sendMessage(GPS_ADDRESS, "{A0=?,A1=?}".getBytes());
		Toast.makeText(this, "{A0=?,A1=?}", Toast.LENGTH_SHORT)
				.show();
	}

	// ���ӶϿ�
	@Override
	public void onConnectLost() {

	}

	// ��GPS�豸�ɼ���ԭʼGPS����ת���ɰٶ�����
	public LatLng GPSCoord2BaiduCoord(LatLng srcLatlng) {// srcLatlngΪ��ת����GPS����
		CoordinateConverter converter = new CoordinateConverter();
		converter.from(CoordType.GPS);// CoordType.COMMON,������ͼ����ת������
		converter.coord(srcLatlng);
		LatLng dstLatLng = converter.convert();// dstLatLng����ת���õİٶ�����
		return dstLatLng;
	}
}
