package com.yugimaster.chapter_1;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity {

	private static final String TAG = "MainActivity";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
        	String test = savedInstanceState.getString("extra_test");
        	Log.d(TAG, "[onCreate]restore extra_test:" + test);
        }
        findViewById(R.id.button1).setOnClickListener(new OnClickListener() {
        	
        	@Override
        	public void onClick(View v) {
        		Intent intent = new Intent("com.yugimaster.chapter_1.c");
//        		intent.setClass(MainActivity.this, SecondActivity.class);
//        		Intent intent = new Intent();
//        		intent.setClass(MainActivity.this, MainActivity.class);
        		intent.putExtra("time", System.currentTimeMillis());
        		intent.addCategory("com.yugimaster.category.c");
        		intent.setDataAndType(Uri.parse("http://abc"), "text/plain");
        		startActivity(intent);
        	}
        });
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
    	super.onNewIntent(intent);
    	Log.d(TAG, "onNewIntent, time=" + intent.getLongExtra("time", 0));
    }
    
    @Override
    protected void onStart() {
    	Log.d(TAG, "onStart");
    	super.onStart();
    }
    
    @Override
    protected void onResume() {
    	Log.d(TAG, "onResume");
    	super.onResume();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	Log.d(TAG, "onSaveInstanceState");
    	outState.putString("extra_test", "test");
    }
    
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
//    	Log.d(TAG, "onRestoreInstanceState");
    	String test = savedInstanceState.getString("extra_test");
    	Log.d(TAG, "[onRestoreInstanceState]restore extra_test:" + test);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	Log.d(TAG, "onConfigurationChanged, newOritentation:" + newConfig.orientation);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	Log.d(TAG, "onPause");
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	Log.d(TAG, "onStop");
    }
    
    protected void onDestroy() {
    	super.onDestroy();
    	Log.d(TAG, "onDestroy");
    }
}
