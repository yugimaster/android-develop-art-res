package com.yugimaster.chapter_2.aidl;

import com.yugimaster.chapter_2.aidl.Book;

interface IOnNewBookArrivedListener {
	void onNewBookArrived(in Book newBook);
}