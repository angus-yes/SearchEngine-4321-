package com;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;
import utils.LinkInfo;

public class LinkIndex {
	private RecordManager recman;
	private HTree hashtable;
	private HashMap<Long, String> decodeMap = new HashMap<Long, String>();;
	private long maxVal = 0;
	private long size = 0;

	public LinkIndex(String recordmanager, String objectname) throws IOException
	{
		recman = RecordManagerFactory.createRecordManager(recordmanager);
		long recid = recman.getNamedObject(objectname);
			
		if (recid != 0) {
			hashtable = HTree.load(recman, recid);
		
			FastIterator iter = hashtable.keys();
			String key;
			while( (key = (String)iter.next())!=null) {
				decodeMap.put(((LinkInfo) hashtable.get(key)).id, key);
				size++;
			}
			
			while (decodeMap.containsKey(maxVal))
				maxVal++;			
		}
		else
		{
			hashtable = HTree.createInstance(recman);
			recman.setNamedObject( objectname, hashtable.getRecid() );
		}
	}
	
	public long size() {
		return size;
	}
	
	public void finalize() throws IOException
	{
		recman.commit();
		recman.close();				
	} 
	
	public Object getInfo(String link) throws IOException {
		return hashtable.get(link);
	}
	
	public String getLink(long id) {
		return decodeMap.get(id);
	}
	
	public FastIterator keys() throws IOException {
		return hashtable.keys();
	}
	
	public Object addEntry(String link, long date, long size, String title) throws IOException
	{
		if (this.getInfo(link) == null) {
			hashtable.put(link, new LinkInfo(maxVal, date, size, title));
			decodeMap.put(maxVal, link);
			size++;
			while (decodeMap.containsKey(maxVal))
				maxVal++;	
		}
		return this.getInfo(link);	
		
	}
	
	public void deleteEntry(String link) throws IOException {
		if (hashtable.get(link)!=null) {
			long id = ((LinkInfo) hashtable.get(link)).id;
			decodeMap.remove(id);
			hashtable.remove(link);
			if (id < maxVal) maxVal = id;
		}
	}
	
	public void updateEntry(String link, long date, long size, String title) throws IOException{
		LinkInfo info = (LinkInfo) this.getInfo(link);
		if (info != null) {
			info.date = date;
			info.size = size;
			info.title = title;
		}
	}
	
	public void setCrawled(String link) throws IOException
	{
		LinkInfo info = (LinkInfo) this.getInfo(link);
		if (info != null) {
			info.crawled = true;
			hashtable.remove(link);
			hashtable.put(link, info);
		}
		
	}
	
	public void printAll() throws IOException {
		FastIterator iter = hashtable.keys();
		String key;
		while( (key = (String)iter.next())!=null)
		{
			System.out.println(key + " " +  this.getInfo(key));
		
		}
	}
	
	public void deleteAll() throws IOException
	{

		FastIterator iter = hashtable.keys();
		String key;
		List<String> keys = new ArrayList<String>();
		while( (key = (String)iter.next())!=null)
		{
			keys.add(key);
		}
		for (String k : keys) 
		{ 		      
	         hashtable.remove(k); 		
	    }
		decodeMap.clear();
		size = 0;
	}
}

