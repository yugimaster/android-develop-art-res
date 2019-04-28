package com.yugimaster.chapter_2.aidl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

public class BookManagerService extends Service {

	private static final String TAG = "BMS";
	
	private AtomicBoolean mIsServiceDestroyed = new AtomicBoolean(false);
	
	private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<Book>();
//	private CopyOnWriteArrayList<IOnNewBookArrivedListener> mListenerList = new CopyOnWriteArrayList<IOnNewBookArrivedListener>();
	/** 
	 * 使用RemoteCallbackList类来删除跨进程的listener
	*/
	private RemoteCallbackList<IOnNewBookArrivedListener> mListenerList = new RemoteCallbackList<IOnNewBookArrivedListener>();
	
	private Binder mBinder = new IBookManager.Stub() {
		
		@Override
		public void unregisterListener(IOnNewBookArrivedListener listener)
				throws RemoteException {
//			if (mListenerList.contains(listener)) {
//				mListenerList.remove(listener);
//				Log.d(TAG, "unregister listener succeed.");
//			} else {
//				Log.d(TAG, "not found, can not unregister.");
//			}
//			Log.d(TAG, "unregisterListener, current size:" + mListenerList.size());
			/** 使用RemoteCallbackList来进行解注册 */
			boolean success = mListenerList.unregister(listener);
			if (success) {
				Log.d(TAG, "unregister success.");
			} else {
				Log.d(TAG, "not found, can not unregister.");
			}
			final int N = mListenerList.beginBroadcast();
			mListenerList.finishBroadcast();
			Log.d(TAG, "unregisterListener, current size:" + N);
		}
		
		@Override
		public void registerListener(IOnNewBookArrivedListener listener)
				throws RemoteException {
//			if (!mListenerList.contains(listener)) {
//				mListenerList.add(listener);
//			} else {
//				Log.d(TAG, "already exists.");
//			}
//			Log.d(TAG, "registerListener, size:" + mListenerList.size());
			/** 使用RemoteCallbackList来进行注册 */
			mListenerList.register(listener);
			
			final int N = mListenerList.beginBroadcast();
			mListenerList.finishBroadcast();
			Log.d(TAG, "registerListener, current size:" + N);
		}
		
		@Override
		public List<Book> getBookList() throws RemoteException {
			/** 测试 为getBookList()加个耗时操作 */
			SystemClock.sleep(5000);
			
			return mBookList;
		}
		
		@Override
		public void addBook(Book book) throws RemoteException {
			mBookList.add(book);
		}
	};
	
	@Override
	public void onCreate() {
		super.onCreate();
		mBookList.add(new Book(1, "Android"));
		mBookList.add(new Book(2, "IOS"));
		new Thread(new ServiceWorker()).start();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public void onDestroy() {
		mIsServiceDestroyed.set(true);
		super.onDestroy();
	}
	
	private void onNewBookArrived(Book book) throws RemoteException {
		mBookList.add(book);
		final int N = mListenerList.beginBroadcast();
		for (int i = 0; i < N; i++) {
			IOnNewBookArrivedListener l = mListenerList.getBroadcastItem(i);
			if (l != null) {
				try {
					l.onNewBookArrived(book);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
		mListenerList.finishBroadcast();
//		Log.d(TAG, "onNewBookArrived, notify listeners:" + mListenerList.size());
//		for (int i = 0; i < mListenerList.size(); i++) {
//			IOnNewBookArrivedListener listener = mListenerList.get(i);
//			Log.d(TAG, "onNewBookArrived, notify listener:" + listener);
//			listener.onNewBookArrived(book);
//		}
	}
	
	/** 每隔5秒向书库加一本新书并通知所有感兴趣的用户 */
	private class ServiceWorker implements Runnable {
		@Override
		public void run() {
			// do background processing here..... 
			while (!mIsServiceDestroyed.get()) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int bookId = mBookList.size() + 1;
				Book newBook = new Book(bookId, "new book#" + bookId);
				try {
					onNewBookArrived(newBook);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
