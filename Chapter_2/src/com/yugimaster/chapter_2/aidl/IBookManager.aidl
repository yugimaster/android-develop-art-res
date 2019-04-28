package com.yugimaster.chapter_2.aidl;

import com.yugimaster.chapter_2.aidl.Book;
import com.yugimaster.chapter_2.aidl.IOnNewBookArrivedListener;

interface IBookManager {
	List<Book> getBookList();
	void addBook(in Book book);
	void registerListener(IOnNewBookArrivedListener listener);
	void unregisterListener(IOnNewBookArrivedListener listener);
}