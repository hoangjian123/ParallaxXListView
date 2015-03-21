package com.example.parallaxxlistview;

import android.app.Application;

public class MyApplication extends Application{
	public static MyApplication instance;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		instance=this;
	}
}
