package com.yugimaster.chapter_2.aidl;

import java.util.List;

import com.yugimaster.chapter_2.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class BookManagerActivity extends Activity {

	private static final String TAG = "BookManagerActivity";
	private static final int MESSAGE_NEW_BOOK_ARRIVED = 1;
	
	private IBookManager mRemoteBookManager;
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_NEW_BOOK_ARRIVED:
				Log.d(TAG, "receive new book :" + msg.obj);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	};
	
	private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
		
		/**
		 * binderDied 在客户端的Binder线程池中被回调 在这里不能访问UI
		 */
		@Override
		public void binderDied() {
			Log.d(TAG, "binder died. tname:" + Thread.currentThread().getName());
			if (mRemoteBookManager == null)
				return;
			mRemoteBookManager.asBinder().unlinkToDeath(mDeathRecipient, 0);
			mRemoteBookManager = null;
			// TODO 这里重新绑定远程service
		}
	};
	
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			IBookManager bookManager = IBookManager.Stub.asInterface(service);
			try {
				mRemoteBookManager = bookManager;
				List<Book> list = bookManager.getBookList();
				Log.i(TAG, "query book list, list type:" + list.getClass().getCanonicalName());
				Log.i(TAG, "query book list:" + list.toString());
				Book newBook = new Book(3, "Android Art Research");
				bookManager.addBook(newBook);
				Log.i(TAG, "add book:" + newBook);
				List<Book> newList = bookManager.getBookList();
				Log.i(TAG, "query book list:" + newList.toString());
				bookManager.registerListener(mOnNewBookArrivedListener);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		/**
		 * onServiceDisconnected在客户端的UI线程中被回调
		 */
		public void onServiceDisconnected(ComponentName className) {
			mRemoteBookManager = null;
			Log.e(TAG, "onServiceDisconnected. tname:" + Thread.currentThread().getName());
		}
	};
	
	private IOnNewBookArrivedListener mOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub() {
		
		@Override
		public void onNewBookArrived(Book newBook) throws RemoteException {
			/** 使用Handler切换到UI线程 */
			mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED, newBook).sendToTarget();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_book_manager);
		Intent intent = new Intent(this, BookManagerService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	public void onButton1Click(View v) {
		Toast.makeText(this, "click button1", Toast.LENGTH_SHORT).show();
		
		/** 避免在主UI线程出现客户端ANR 将下面的方法放在非UI线程中 */
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if (mRemoteBookManager != null) {
					try {
						List<Book> newList = mRemoteBookManager.getBookList();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
	
	protected void onDestroy() {
		if (mRemoteBookManager != null && mRemoteBookManager.asBinder().isBinderAlive()) {
			try {
				Log.i(TAG, "unregister listener:" + mOnNewBookArrivedListener);
				mRemoteBookManager.unregisterListener(mOnNewBookArrivedListener);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		unbindService(mConnection);
		super.onDestroy();
	}
}
