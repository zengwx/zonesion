package com.zonesion.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

	private static final int DB_VERSION = 1;// 数据库版本
	private static final String DB_NAME = "doorcheck.db";// 数据库名
	public static final String DB_TABLE_NAME = "users";// 数据库表名

	private static final String TAG = "DBHelper";// 调试标记

	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// 创建users表
		db.execSQL("create table if not exists users"
				+ "(_id integer primary key autoincrement, card_num string not null, user_name string not null);");
		Log.i(TAG, "create table");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// 在表users中增加一列other
		// db.execSQL("ALTER TABLE users ADD COLUMN other STRING");
		Log.i(TAG, "update sqlite " + oldVersion + "---->"
				+ newVersion);
	}
}
