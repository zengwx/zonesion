package com.zonesion.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.zhiyun360.wsn.droid.WSNRTConnect;
import com.zonesion.app.IOnWSNDataListener;
import com.zonesion.app.ZApplication;
import com.zonesion.database.DBManager;
import com.zonesion.doorcheck.R;

public class DoorCheckFragment extends Fragment implements
 IOnWSNDataListener {
	// 应用和连接
	private ZApplication mApplication;
	private WSNRTConnect mWSNRTConnect;

	private String ID = "1214";// 用户账号
	private String KEY = "1214";// 用户密钥
	// 传感器MAC地址
	private String rfid_mac = "00:12:4B:00:03:D4:42:1F";
	private String relay_mac = "00:12:4B:00:03:D4:41:16";

	private DBManager dbManager;// 数据库管理器

	private TextView tvWelcome, tvCard;// 文本显示控件

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mApplication = (ZApplication) getActivity().getApplication();// 获取应用
		mWSNRTConnect = mApplication.getWSNRTConnect();// 获取连接
		mWSNRTConnect.setIdKey(ID, KEY);// 设置用户ID和秘钥
		mWSNRTConnect.setServerAddr("zhiyun360.com:28081");// 设置智云服务地址
		mApplication.registerOnSensorDataListener(this);// 注册监听
		mWSNRTConnect.connect();// 建立连接

		dbManager = new DBManager(getActivity());// 创建数据库管理器对象

		// 用布局填充视图
		View view = inflater.inflate(R.layout.door_check, container, false);
		// 初始化文本显示控制
		tvCard = (TextView) view.findViewById(R.id.tvCard);
		tvWelcome = (TextView) view.findViewById(R.id.tvWelcome);
		tvWelcome.setEnabled(false);// 设置文本显示控件事件失效
		tvWelcome.setOnLongClickListener(mOnLongClickListener);// 设置长按监听器

		return view;
	}

	@Override
	public void onDestroyView() {
		mApplication.unregisterOnSensorDataListener(this);// 移除监听
		mWSNRTConnect.disconnect();// 断开连接
		dbManager.closeDB(); // 关闭数据库
		super.onDestroyView();
	}

	@Override
	public void onMessageArrive(String mac, String tag, String val) {
		if (rfid_mac.equalsIgnoreCase(mac)) {
			if (tag.equals("A0")) {
				tvCard.setText(val);
				tvWelcome.setEnabled(true);
				// 在数据库中查询卡号,返回用户名
				String db_user_name = dbManager.query(val);
				// 如果用户名不为空,则发送打开命令,并在文本显示控件上提示该用户打卡成功
				if (!db_user_name.equals("")) {
					mWSNRTConnect.sendMessage(relay_mac,
							"{OD1=1,D1=?}".getBytes());
					tvWelcome.setText(db_user_name + "打卡成功");
				} else {// 如果用户名为空,则提示该卡号未注册
					tvWelcome.setText(val + "未注册");
				}
				// 如果收到主动上报的A0=0消息,文本显示控件文本清空,事件功能失效,并发送关闭命令
				if (val.equals("0")) {
					tvCard.setText("");
					tvWelcome.setText("");
					tvWelcome.setEnabled(false);
					mWSNRTConnect.sendMessage(relay_mac,
							"{CD1=1,D1=?}".getBytes());
				}
			}
		}

		if (relay_mac.equalsIgnoreCase(mac)) {
			if (tag.equals("D1")) {
				int iValue = Integer.parseInt(val);
				// 根据继电器开关状态设置相应的文本控件背景
				if ((iValue & 0x01) == 0x01) {
					tvWelcome.setBackgroundResource(R.drawable.door_state_open);
				} else {
					tvWelcome
							.setBackgroundResource(R.drawable.door_state_close);
				}
			}
		}
	}
	
	@Override
	public void onConnect() {

	}

	@Override
	public void onConnectLost() {

	}
	View.OnLongClickListener mOnLongClickListener = new  View.OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {

			final EditText editText = new EditText(getActivity());// 编辑框
			editText.setHint("请输入用户名");// 编辑框提示
			// 对话框
			new AlertDialog.Builder(getActivity())
					.setTitle("添加用户")
					.setView(editText)
					.setPositiveButton("添加",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// 获取卡号
									String card_num = tvCard.getText()
											.toString().trim();
									// 获取用户名
									String user_name = editText.getText()
											.toString().trim();
									if (!user_name.equals("")
											&& !card_num.equals("")) {
										// 在数据库中查询卡号
										String db_user_name = dbManager
												.query(card_num);
										// 如果查不到,则添加用户
										if (db_user_name.equals("")) {
											dbManager.add(card_num,
													user_name);
										} else { // 否则提示该卡号已注册用户名
											tvWelcome.setText("卡号" + card_num
													+ "已注册用户名"
													+ db_user_name);
										}
									}
								}
							}).setNegativeButton("取消", null).show();
			return false;
		}
	};
}
