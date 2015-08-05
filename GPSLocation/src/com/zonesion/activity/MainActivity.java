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
	private MapView mMapView;// 百度地图显示控件
	private BaiduMap mBaiduMap;// 百度地图对象
	private Marker mMarkerGateWay;// 覆盖物
	private InfoWindow mInfoWindow;// 信息窗口
	private BitmapDescriptor bdMarker01, bdMarker02;// 位图描述
	private TextView tvLongitude, tvLatitude, tvGateWay;// 文本显示控件

	// 应用和连接
	private ZApplication mApplication;
	private WSNRTConnect mWSNRTConnect;

	private String ID = "1214";// 用户账号
	private String KEY = "1214";// 用户密钥

	private static final String GPS_ADDRESS = "GPS:0";// GPS地址
	private double dLongitude, dLatitude;// 经度,纬度

	// 创建Activity时会自动调用该方法
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);

		mApplication = (ZApplication) getApplication();// 获取应用
		mWSNRTConnect = mApplication.getWSNRTConnect();// 获取连接
		mWSNRTConnect.setIdKey(ID, KEY);// 设置用户ID和秘钥
		mWSNRTConnect.setServerAddr("zhiyun360.com:28081");// 设置智云服务地址
		mApplication.registerOnSensorDataListener(this);// 注册监听
		mWSNRTConnect.connect();// 建立连接

		// 初始化位图资源
		bdMarker01 = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_marker01);
		bdMarker02 = BitmapDescriptorFactory
				.fromResource(R.drawable.icon_marker02);
		// 初始化地图显示控件,获取百度地图对象
		mMapView = (MapView) findViewById(R.id.mvRight);
		mBaiduMap = mMapView.getMap();

		// 初始化经纬度文本显示控件
		tvLongitude = (TextView) findViewById(R.id.tvLongitude);
		tvLatitude = (TextView) findViewById(R.id.tvLatitude);
		// tvGateWay显示网关信息
		tvGateWay = new TextView(getApplicationContext());
		tvGateWay.setBackgroundColor(Color.WHITE);
		tvGateWay.setTextColor(Color.BLACK);
		// 为百度地图设置覆盖物点击监听器
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			// 覆盖物点击事件
			@Override
			public boolean onMarkerClick(Marker marker) {
				if (marker == mMarkerGateWay) {
					tvGateWay.setText("ID:" + ID);
					LatLng latLng = marker.getPosition();
					mInfoWindow = new InfoWindow(tvGateWay, latLng, -50);// -50为偏移位置,在覆盖物上方50个像素处显示
					mBaiduMap.showInfoWindow(mInfoWindow);
				}
				return true;
			}
		});

	}

	// 销毁Activity时会自动调用该方法
	@Override
	protected void onDestroy() {
		mApplication.unregisterOnSensorDataListener(this);// 移除监听
		mWSNRTConnect.disconnect();// 断开连接
		super.onDestroy();
	}

	// 绘制地图
	private void drawMapView(double latitude, double longitude) {
		LatLng latlng = new LatLng(latitude, longitude);
		latlng = GPSCoord2BaiduCoord(latlng);// 硬件采集的GPS坐标转换成百度坐标

		MapStatus mMapStatus = new MapStatus.Builder().target(latlng).zoom(17)
				.build();
		MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory
				.newMapStatus(mMapStatus);
		mBaiduMap.setMapStatus(mMapStatusUpdate);

		mBaiduMap.clear();// 添加新的覆盖物之前清除之前所有的覆盖物
		// 添加百度地图覆盖物
		ArrayList<BitmapDescriptor> giflist = new ArrayList<BitmapDescriptor>();
		giflist.add(bdMarker01);
		giflist.add(bdMarker02);
		OverlayOptions ooGateWay = new MarkerOptions().position(latlng)
				.icons(giflist).zIndex(0).period(20);
		mMarkerGateWay = (Marker) (mBaiduMap.addOverlay(ooGateWay));
	}

	// 定位网关按钮点击事件
	public void locGateWay(View view) {
		Toast.makeText(this, "{A0=?,A1=?}", Toast.LENGTH_SHORT).show();
		mWSNRTConnect.sendMessage(GPS_ADDRESS, "{A0=?,A1=?}".getBytes());
	}

	// 处理节点发送过来的消息
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
			drawMapView(dLatitude, dLongitude);// 调用绘制地图方法
		}
	}

	// 连接成功
	@Override
	public void onConnect() {
		mWSNRTConnect.sendMessage(GPS_ADDRESS, "{A0=?,A1=?}".getBytes());
		Toast.makeText(this, "{A0=?,A1=?}", Toast.LENGTH_SHORT)
				.show();
	}

	// 连接断开
	@Override
	public void onConnectLost() {

	}

	// 将GPS设备采集的原始GPS坐标转换成百度坐标
	public LatLng GPSCoord2BaiduCoord(LatLng srcLatlng) {// srcLatlng为待转换的GPS坐标
		CoordinateConverter converter = new CoordinateConverter();
		converter.from(CoordType.GPS);// CoordType.COMMON,其他地图坐标转换参数
		converter.coord(srcLatlng);
		LatLng dstLatLng = converter.convert();// dstLatLng保存转换好的百度坐标
		return dstLatLng;
	}
}
