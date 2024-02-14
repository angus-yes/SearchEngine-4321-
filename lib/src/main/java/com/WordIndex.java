package com;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

public class WordIndex {
	private RecordManager recman;
	private HTree hashtable;
	private HashMap<Long, String> decodeMap = new HashMap<Long, String>();
	private long maxVal = 0;
	private long size = 0;

	public WordIndex(String recordmanager, String objectname) throws IOException
	{
		recman = RecordManagerFactory.createRecordManager(recordmanager);
		long recid = recman.getNamedObject(objectname);
			
		if (recid != 0) {
			hashtable = HTree.load(recman, recid);
		
			FastIterator iter = hashtable.keys();
			String key;
			while( (key = (String)iter.next())!=null) {
				decodeMap.put((long) hashtable.get(key), key);
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
	
	public Object getValue(String word) throws IOException {
		return hashtable.get(word);
	}
	
	public String getWord(long id) {
		return decodeMap.get(id);
	}
	
	public Object addEntry(String word) throws IOException
	{
		if (this.getValue(word) == null) {
			hashtable.put(word, maxVal);
			decodeMap.put(maxVal, word);
			size++;
			while (decodeMap.containsKey(maxVal))
				maxVal++;	
		}
		return this.getValue(word);	
		
	}
	
	public void deleteEntry(String word) throws IOException {
		if (hashtable.get(word)!=null) {
			long id = (long) hashtable.get(word);
			decodeMap.remove(id);
			hashtable.remove(word);
			if (id < maxVal) maxVal = id;
		}
	}
	
	public void printAll() throws IOException {
		FastIterator iter = hashtable.keys();
		String key;
		while( (key = (String)iter.next())!=null)
		{
			System.out.println(key + " " +  this.getValue(key));
		
		}
	}
	
	public void deleteAll() throws IOException
	{
		FastIterator iter = hashtable.keys();
		String key;
		List<String> keys = new ArrayList<String>();
		while( (key = (String)iter.next())!=null)
			keys.add(key);		
		for (String k : keys) 				      
	         hashtable.remove(k); 	
		decodeMap.clear();
	    size = 0;
	}
}

