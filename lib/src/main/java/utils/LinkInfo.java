package utils;

import java.io.Serializable;

public class LinkInfo implements Serializable{
	public long id;
	public long size;
	public long date;
	public String title;
	public boolean crawled = false;
	
	public LinkInfo(long _id, long _date, long _size, String _title) {
		id = _id;
		size = _size;
		date = _date;
		title = _title;
	}
	
	public String toString() {
		return "ID: " + id + "; Size: " + size + "; date: " + date + "; title: " + title + crawled;
	}
	
}
