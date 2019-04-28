package com.yugimaster.chapter_2.messenger;

import com.yugimaster.chapter_2.utils.MyConstants;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class MessengerService extends Service {

	private static final String TAG = "MessengerService";
	
	private static class MessengerHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MyConstants.MSG_FROM_CLIENT:
				Log.i(TAG, "receive msg from Client:" + msg.getData().getString("msg"));
				
				/** 服务器对消息的反馈 */
				Messenger client = msg.replyTo;
				Message replyMessage = Message.obtain(null, MyConstants.MSG_FROM_SERVICE);
				Bundle bundle = new Bundle();
				bundle.putString("reply", "嗯，你的消息我已经收到了，稍后会回复你。");
				replyMessage.setData(bundle);
				try {
					client.send(replyMessage);
				} catch(RemoteException e) {
					e.printStackTrace();
				}
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	
	private final Messenger mMessenger = new Messenger(new MessengerHandler());
	
	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}
}
