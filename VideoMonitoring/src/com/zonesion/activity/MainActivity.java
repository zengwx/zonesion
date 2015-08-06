package com.zonesion.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.zhiyun360.wsn.droid.WSNCamera;
import com.zhiyun360.wsn.droid.WSNCameraListener;
import com.zonesion.videomonitoring.R;

// Developer:zengwx
// Date:2015-08-06

public class MainActivity extends Activity implements WSNCameraListener,
		OnTouchListener, OnLongClickListener {
	private WSNCamera mWSNCamera;// 摄像头
	private String cameraIP = "ayari.easyn.hk";// 摄像头IP地址
	private String cameraType = "H3-Series";// 摄像头类型
	private String userName = "admin";// 用户名
	private String pwd = "admin";// 密码
	private String ID = "23710173";// 用户账号
	private String KEY = "kvpOdBCLSf2TKQLzm7g8TGxn2YWtTqAaaIA3X7NDL4QYGHko";// 用户密钥

	private ImageView ivVideo;// 图片视图
	private float mPosX;
	private float mPosY;
	private int threshold = 50;
	private boolean openFlag = false;
	double mLastTime = 0;
	double mCurTime = 0;

	// 创建Activity时会自动调用该方法
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ivVideo = (ImageView) findViewById(R.id.ivVideo);
		ivVideo.setOnTouchListener(this);
		ivVideo.setOnLongClickListener(this);

		mWSNCamera = new WSNCamera();
		mWSNCamera.setIdKey(ID, KEY);
		mWSNCamera.setCameraListener(this);
		mWSNCamera.initCamera(cameraIP, userName, pwd, cameraType);
		mWSNCamera.checkOnline();
	}

	// 销毁Activity时会自动调用该方法
	@Override
	protected void onDestroy() {
		mWSNCamera.freeCamera();
		super.onDestroy();
	}

	@Override
	public void onOnline(String cameraIP, boolean online) {
		Toast.makeText(this, "camera:" + cameraIP + (online ? "在线" : "不在线"),
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void onSnapshot(String cameraIP, Bitmap bitmap) {
		saveBitmap(bitmap);
	}

	@Override
	public void onVideoCallBack(String cameraIP, Bitmap bitmap) {
		if (openFlag) {
			ivVideo.setImageBitmap(bitmap);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		// 按下
		case MotionEvent.ACTION_DOWN:
			mPosX = event.getX();
			mPosY = event.getY();

			mLastTime = mCurTime;
			mCurTime = System.currentTimeMillis();
			if (mCurTime - mLastTime < 300) {
				if (!openFlag) {
					mWSNCamera.openVideo();
					openFlag = true;
				} else {
					mWSNCamera.closeVideo();
					ivVideo.setImageBitmap(null);
					openFlag = false;
				}
				ivVideo.setLongClickable(openFlag);
			}
			break;
		// 拿起
		case MotionEvent.ACTION_UP:
			if (openFlag) {
				int diffX = (int) (event.getX() - mPosX);
				int diffY = (int) (event.getY() - mPosY);
				// Toast.makeText(this, "diffX=" + diffX + ",diffY=" + diffY,
				// Toast.LENGTH_SHORT).show();
				if (diffX > 11 * threshold) {
					mWSNCamera.control("HPATROL");
				} else if (diffX > threshold) {
					control(diffX, "LEFT");
				} else if (diffX < -threshold) {
					control(diffX, "RIGHT");
				}
				if (diffY > 6 * threshold) {
					mWSNCamera.control("VPATROL");
				} else if (diffY > threshold) {
					control(diffY, "UP");
				} else if (diffY < -threshold) {
					control(diffY, "DOWN");
				}
			}
			break;
		// 移动
		case MotionEvent.ACTION_MOVE:
			break;
		default:
			break;
		}
		return false;//
	}

	@Override
	public boolean onLongClick(View v) {
		mWSNCamera.snapshot();
		return true;
	}

	public void control(int diffXY, String direction) {
		for (int i = 0; i < (diffXY > 0 ? diffXY : -diffXY) / threshold; i++) {
			mWSNCamera.control(direction);
		}
	}

	// 保存图片方法
	public boolean saveBitmap(Bitmap bitmap) {
		String picName = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss",
				Locale.CHINA).format(new Date(System.currentTimeMillis()))
				+ ".jpeg";
		File sdcardPath = Environment.getExternalStorageDirectory();
		File dstPath = new File(sdcardPath, "VideoMonitor/");
		try {
			if (!dstPath.exists()) {
				dstPath.mkdir();
			}
			File file = new File(dstPath, picName);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();
			Toast.makeText(this, "图片" + picName + "保存在" + dstPath.toString(),
					Toast.LENGTH_LONG)
					.show();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
