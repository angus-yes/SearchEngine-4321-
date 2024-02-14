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

public class LinkChildIndex {
	private RecordManager recman;
	private HTree childTable;
	private HashMap<String, List<String>> parentTable = new HashMap<String, List<String>>();
	private long size = 0;

	public LinkChildIndex(String recordmanager, String objectname) throws IOException
	{
		recman = RecordManagerFactory.createRecordManager(recordmanager);
		long recid = recman.getNamedObject(objectname);
			
		if (recid != 0) {
			childTable = HTree.load(recman, recid);		
		
			FastIterator iter = childTable.keys();
			Object key;
			while( (key = iter.next())!=null) {
				size++;
				List<String> links = (List<String>) childTable.get(key);
				for (String link: links) {
					List<String> parLink = parentTable.get(link);
					if (parLink == null)
						parLink = new ArrayList<String>();
					parLink.add((String) key);
					parentTable.put(link, parLink);
				}
			}
		}
		else
		{
			childTable = HTree.createInstance(recman);
			recman.setNamedObject( objectname, childTable.getRecid() );
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
	
	public Object getChild(String link) throws IOException {
		return childTable.get(link);
	}
	
	public List<String> getParent(String link) {
		return parentTable.get(link);
	}
	
	public Object addEntry(String link, List<String> child) throws IOException
	{
		if (this.getChild(link) == null) {
			childTable.put(link, child);
			for (String c: child) {
				List<String> parLink = parentTable.get(c);
				if (parLink == null)
					parLink = new ArrayList<String>();
				parLink.add((String) link);
				parentTable.put(c, parLink);
			}
			size++;
		}
		return this.getChild(link);	
		
	}
	
	public void delEntry(String link) throws IOException {
		if (childTable.get(link)!=null) {
			List<String> children = (List<String>) childTable.get(link);
			for (String child: children) {
				List<String> parents = (List<String>) parentTable.get(child);
				parents.remove(link);
			}
			childTable.remove(link);	
		}
	}
	
	public void printAll() throws IOException {
		FastIterator iter = childTable.keys();
		Object key = iter.next();
		while(key != null)
		{
			System.out.println(key + " " +  childTable.get(key));
			key = iter.next();
		}
	}
	
	public void deleteAll() throws IOException
	{

		FastIterator iter = childTable.keys();
		List<String> keys = new ArrayList<String>();
		
		Object key = iter.next();
		while(key != null)
		{
			keys.add((String) key);
			key = iter.next();
		}
		
		for (String k : keys) 
		{ 		      
			childTable.remove(k); 		
	    }
		parentTable.clear();
		size = 0;
	}
}

