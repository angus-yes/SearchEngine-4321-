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
import utils.Posting;

public class InvertedIndex {
	private RecordManager recman;
	private HTree hashtable;
	private HashMap<Long, List<Long>> docDict = new HashMap<Long, List<Long>>();
	private long size = 0;

	public InvertedIndex(String recordmanager, String objectname) throws IOException
	{
		recman = RecordManagerFactory.createRecordManager(recordmanager);
		long recid = recman.getNamedObject(objectname);
			
		if (recid != 0) {
			hashtable = HTree.load(recman, recid);
			
			FastIterator iter = hashtable.keys();
			Object key;
			while( (key = iter.next())!=null) {
				size++;
				List<Posting> posts = (List<Posting>) hashtable.get((long) key);
				for (Posting post: posts) {
					List<Long> docWords = docDict.get(post.doc);
					if (docWords == null)
						docWords = new ArrayList<Long>();
					docWords.add((long) key);
					docDict.put(post.doc, docWords);
				}
			}
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
	
	public FastIterator keys() throws IOException {
		return hashtable.keys();
	}

	public void finalize() throws IOException
	{
		recman.commit();
		recman.close();				
	} 

	public Object getPostings(long id) throws IOException {
		return hashtable.get(id);
	}
	
	public List<Long> getWords(long id){
		return docDict.get(id);
	}
	
	public int getMaxLength(long id) throws IOException {
		ArrayList<Posting> posts = (ArrayList<Posting>) this.getPostings(id);
		if (posts == null)
			return 0;
		else {
			int max = 0;
			for (Posting p: posts) {
				if (p.pos.size() > max)
					max = p.pos.size();
			}
			return max;
		}
	}
	
	public void addEntry(long wordId, long docId, ArrayList<Integer> pos) throws IOException
	{

		Object content = hashtable.get(wordId);
		Posting p = new Posting(docId, pos);
		if (content == null){
			List<Posting> pList = new ArrayList<Posting>();
			pList.add(p);
			hashtable.put(wordId, pList);
			size++;
		}
		else {
			List<Posting> pList = ((List<Posting>) hashtable.get(wordId));
			for (int i = 0; i < pList.size(); i++) {
				if (pList.get(i).doc == docId) {
					pList.remove(i);
					size--;
				}
			}
			pList.add(p);
			size++;
			hashtable.put(wordId, pList);				
		}
		if (docDict.containsKey(docId)==false) {
			List<Long> words = new ArrayList<Long>();
			words.add(wordId);
			docDict.put(docId, words);
			return;
		}
		List<Long> words = (List<Long>) docDict.get(docId);
		if (words.contains(wordId) == false) {
			words.add(wordId);
			docDict.replace(docId, words);
		}
		
	}

	
	public void delDoc(long docId) throws IOException
	{
		if (docDict.get(docId)!=null) {
			List<Long> wordList = docDict.get(docId);
			for (long wordId: wordList)
				delWordDoc(wordId, docId);
			docDict.remove(docId);
		}
	} 
	
	public void delWordDoc(long wordId, long docId) throws IOException{
		if (hashtable.get(wordId)!=null) {
			List<Posting> postings = (List<Posting>) hashtable.get(wordId);
			for (Posting p: postings)
				if (p.doc == docId) {
					postings.remove(p);
					break;
				}
			if (postings.size() == 0)
				hashtable.remove(wordId);
		}
	}
	
	public void printAll() throws IOException
	{

		FastIterator iter = hashtable.keys();
		Object key = iter.next();
		while(key != null)
		{
			System.out.println(key + " " +  hashtable.get(key));
			key = iter.next();
		}
	}	
	public void printInv() throws IOException
	{
			System.out.println(docDict);
	}		
	public void deleteAll() throws IOException
	{

		FastIterator iter = hashtable.keys();
		List<Long> keys = new ArrayList<Long>();
		
		Object key = iter.next();
		while(key != null)
		{
			keys.add((long) key);
			key = iter.next();
		}
		
		for (long k : keys) 
		{ 		      
	         hashtable.remove(k); 		
	    }
		docDict.clear();
		size = 0;
	}
}
