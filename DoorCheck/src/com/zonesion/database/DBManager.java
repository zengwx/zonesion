package com.zonesion.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBManager {
	// 数据库助手
	private DBHelper mDBHelper;
	// 数据库
	private SQLiteDatabase db;
	
	public DBManager(Context context){
		mDBHelper = new DBHelper(context);// 创建数据库助手对象
		db = mDBHelper.getWritableDatabase();// 获取可写的数据库
	}
	
	// 添加
	public void add(String card_num, String user_name) {
		Log.i("DBManager.add()", "-----add data-----");
		ContentValues cv = new ContentValues();
		cv.put("card_num", card_num);
		cv.put("user_name", user_name);
		db.insert(DBHelper.DB_TABLE_NAME, null, cv);
		Log.i("DBManager.add()", card_num + "|" + user_name);
	}

	// 根据卡号查询
	public String query(String card_num) {
		String sql = "select * from users where card_num =" + "'"
				+ card_num + "'";
		String user_name = "";
		Cursor c = db.rawQuery(sql, null);
		while (c.moveToNext()) {
			user_name = c.getString(c.getColumnIndex("user_name"));
		}
		c.close();
		return user_name;
	}

	// 关闭数据库
	public void closeDB() {
		db.close();
	}
}
