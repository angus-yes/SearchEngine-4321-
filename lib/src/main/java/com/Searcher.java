package com;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import jdbm.helper.FastIterator;
import utils.LinkInfo;
import utils.Posting;
import utils.StopStem;
import java.lang.Math;

public class Searcher {
	private InvertedIndex ii, ti;
	private WordIndex wi;
	private LinkChildIndex lci;
	private LinkIndex li;
	private StopStem stemming;
	
	public Searcher(WordIndex wi, LinkIndex li, LinkChildIndex lci, InvertedIndex ii, InvertedIndex ti, StopStem stemming) {
		this.wi = wi;
		this.li = li;
		this.lci = lci;
		this.ii = ii;
		this.ti = ti;
		this.stemming = stemming;
	}
	
	public Set<ArrayList<String>> processQuery(String q) {
		Set<ArrayList<String>> terms = new HashSet<ArrayList<String>>();
		if (q.length() == 0)
			return terms;
		if (q.charAt(q.length()-1) != ' ' && q.charAt(q.length()-1) != '\"')
		    q += " ";
	    boolean asTerm = false;
	    String tmpWord = "";
	    ArrayList<String> tmpTerm = new ArrayList<String>();
	    for (int i = 0; i < q.length(); i++){
	        String cur = q.substring(i,i+1);
	        if (!cur.equals("\"") && !cur.equals(" ")){
	            tmpWord += cur;
	        }
	        else if (cur.equals("\"")){
	        	
	        	String stemWord = stemming.process(tmpWord.trim().toLowerCase());
	        	
                if (stemWord.length() > 0)
	                tmpTerm.add(stemWord);
	            tmpWord = "";
	            if (tmpTerm.size() > 0){
	                terms.add(tmpTerm);
	                tmpTerm = new ArrayList<String>();
	            }
	            asTerm = !asTerm;
	        }
	        else{
	        	
	        	String stemWord = stemming.process(tmpWord.trim().toLowerCase());
	        	
	            if (stemWord.length() > 0)
	                tmpTerm.add(stemWord);
	            tmpWord = "";
	            
	            if (!asTerm && tmpTerm.size() > 0){
	                terms.add(tmpTerm);
	                tmpTerm = new ArrayList<String>();
	            }
	        }
	    }
	    return terms;
	}
	
	public boolean validQuery(String q) {
		int pos = 0;
        int count = 0;
        while (q.indexOf("\"", pos) != -1){
            count++;
            pos = q.indexOf("\"", pos)+1;
        }
        return (count%2==0);
	}
	
	public HashMap<Long, Double> getWordContentScores(String w) throws IOException {
		HashMap<Long, Double> res = new HashMap<Long, Double>();
		FastIterator links = li.keys();
		String key;
		while( (key = (String)links.next())!=null) {
			long linkId = ((LinkInfo) li.getInfo(key)).id;
			res.put(linkId, 0.0);
		}
		if (wi.getValue(w) != null) {
			long wordId = (long) wi.getValue(w);
			int maxtf = ii.getMaxLength(wordId);
			if (ii.getPostings(wordId) == null)
				return res;
			int df = ((List<Posting>) ii.getPostings(wordId)).size();			
			for (Posting p: (List<Posting>) ii.getPostings(wordId)) {
				int tf = p.pos.size();
				double score = (tf+0.0)/maxtf*(Math.log((li.size()+0.0)/df)/Math.log(2));
				res.put(p.doc, score);			
			}
		}
		return res;
	}
	
	public HashMap<Long, Double> getWordTitleScores(String w) throws IOException {
		HashMap<Long, Double> res = new HashMap<Long, Double>();
		FastIterator links = li.keys();
		String key;
		while( (key = (String)links.next())!=null) {
			long linkId = ((LinkInfo) li.getInfo(key)).id;
			res.put(linkId, 0.0);
		}
		if (wi.getValue(w) != null) {
			long wordId = (long) wi.getValue(w);
			int maxtf = ti.getMaxLength(wordId);
			if (ti.getPostings(wordId) == null)
				return res;
			int df = ((List<Posting>) ti.getPostings(wordId)).size();
			for (Posting p: (List<Posting>) ti.getPostings(wordId)) {
				int tf = p.pos.size();
				double score = (tf+0.0)/maxtf*(Math.log((li.size()+0.0)/df)/Math.log(2));
				res.put(p.doc, score);
			}
		}
		return res;
	}
	
	public HashMap<Long, Double> getTermTitleScores(ArrayList<String> t) throws IOException {
		HashMap<Long, Double> res = new HashMap<Long, Double>();
		FastIterator links = li.keys();
		String key;
		while( (key = (String)links.next())!=null) {
			long linkId = ((LinkInfo) li.getInfo(key)).id;
			res.put(linkId, 0.0);
		}
		
		long termLength = t.size();
		ArrayList<HashMap<Long, ArrayList<Integer>>> wordsData = new ArrayList<HashMap<Long, ArrayList<Integer>>>();
		for (String w: t) {
			if (wi.getValue(w) == null)
				return res;
			long wordId = (long) wi.getValue(w);
			List<Posting> posts = (List<Posting>) ti.getPostings(wordId);
			if (posts == null)
				return res;
			HashMap<Long, ArrayList<Integer>> docPos = new HashMap<Long, ArrayList<Integer>>();
			posts.forEach((n) -> docPos.put(n.doc, (ArrayList<Integer>) n.pos.clone()));
			wordsData.add(docPos);
		}
		
		Set<Long> docIds = wordsData.get(0).keySet();
		for (int i = 1; i < termLength; i++) {
			docIds.retainAll(wordsData.get(i).keySet());
		}	
		
		HashMap<Long, Integer> tfs = new HashMap<Long, Integer>();
		int maxtf = 0;
		
		for (long docId: docIds) {
			ArrayList<Integer> tmpPos = wordsData.get(0).get(docId); 
			
			for (int i = 1; i < termLength; i++) {
				tmpPos.replaceAll(n -> n+1);
				tmpPos.retainAll(wordsData.get(i).get(docId));
			}
			tfs.put(docId, tmpPos.size());
			if (tmpPos.size() > maxtf) 
				maxtf = tmpPos.size(); 
		}
		double idf = (Math.log((li.size()+0.0)/tfs.size())/Math.log(2));
		if (maxtf != 0) {
			for (long docId: tfs.keySet()) {
				res.put(docId, idf*tfs.get(docId)/maxtf);
			}
		}
		
		return res;
	}
	
	public HashMap<Long, Double> getTermContentScores(ArrayList<String> t) throws IOException {
		HashMap<Long, Double> res = new HashMap<Long, Double>();
		FastIterator links = li.keys();
		String key;
		while( (key = (String)links.next())!=null) {
			long linkId = ((LinkInfo) li.getInfo(key)).id;
			res.put(linkId, 0.0);
		}
		
		long termLength = t.size();
		ArrayList<HashMap<Long, ArrayList<Integer>>> wordsData = new ArrayList<HashMap<Long, ArrayList<Integer>>>();
		for (String w: t) {
			if (wi.getValue(w) == null)
				return res;
			long wordId = (long) wi.getValue(w);
			List<Posting> posts = (List<Posting>) ii.getPostings(wordId);
			if (posts == null)
				return res;
			HashMap<Long, ArrayList<Integer>> docPos = new HashMap<Long, ArrayList<Integer>>();
			posts.forEach((n) -> docPos.put(n.doc, (ArrayList<Integer>) n.pos.clone()));
			wordsData.add(docPos);
		}
		
		Set<Long> docIds = wordsData.get(0).keySet();
		for (int i = 1; i < termLength; i++) {
			docIds.retainAll(wordsData.get(i).keySet());
		}	
		
		HashMap<Long, Integer> tfs = new HashMap<Long, Integer>();
		int maxtf = 0;
		
		for (long docId: docIds) {
			ArrayList<Integer> tmpPos = wordsData.get(0).get(docId); 
			for (int i = 1; i < termLength; i++) {
				tmpPos.replaceAll(n -> n+1);
				tmpPos.retainAll(wordsData.get(i).get(docId));
			}
			tfs.put(docId, tmpPos.size());
			if (tmpPos.size() > maxtf) 
				maxtf = tmpPos.size(); 
		}
		double idf = (Math.log((li.size()+0.0)/tfs.size())/Math.log(2));
		if (maxtf != 0) {
			for (long docId: tfs.keySet()) {
				res.put(docId, idf*tfs.get(docId)/maxtf);
			}
		}
		
		return res;
	}
	
	public HashMap<Long, Double> getDocContentLength() throws IOException {
		HashMap<Long, Double> len = new HashMap<Long, Double>();
		FastIterator links = li.keys();
		String key;
		while( (key = (String)links.next())!=null) {
			long linkId = ((LinkInfo) li.getInfo(key)).id;
			len.put(linkId, 0.0);
		}
		FastIterator wordIds = ii.keys();
		Object id;
		long wid;
		while((id = wordIds.next())!=null) {
			wid = (long) id;
			for (Posting p: (List<Posting>) ii.getPostings(wid)) {
				double score = (p.pos.size()+0.0)/ii.getMaxLength(wid)*(Math.log((li.size()+0.0)/((List<Posting>) ii.getPostings(wid)).size())/Math.log(2));
				score = Math.pow(score, 2);
				len.put(p.doc, len.get(p.doc)+score);
			}
		}
		return len;
	}
	
	public HashMap<Long, Double> getDocTitleLength() throws IOException {
		HashMap<Long, Double> len = new HashMap<Long, Double>();
		FastIterator links = li.keys();
		String key;
		while( (key = (String)links.next())!=null) {
			long linkId = ((LinkInfo) li.getInfo(key)).id;
			len.put(linkId, 0.0);
		}
		FastIterator wordIds = ti.keys();
		Object id;
		long wid;
		while((id = wordIds.next())!=null) {
			wid = (long) id;
			for (Posting p: (List<Posting>) ti.getPostings(wid)) {
				double score = (p.pos.size()+0.0)/ti.getMaxLength(wid)*(Math.log((li.size()+0.0)/((List<Posting>) ti.getPostings(wid)).size())/Math.log(2));
				score = Math.pow(score, 2);
				len.put(p.doc, len.get(p.doc)+score);
			}
		}
		return len;
	}
	
	public HashMap<Long, Double> aggregateScore(Set<ArrayList<String>> t) throws IOException{
		HashMap<Long, Double> scores = new HashMap<Long, Double>();	
		HashMap<Long, Double> cosContent = new HashMap<Long, Double>();	
		HashMap<Long, Double> cosTitle = new HashMap<Long, Double>();	

		HashMap<Long, Double> docLengthSq = getDocContentLength();
		HashMap<Long, Double> titleLengthSq = getDocTitleLength();

		FastIterator links = li.keys();
		String key;
		while( (key = (String)links.next())!=null) {
			long linkId = ((LinkInfo) li.getInfo(key)).id;
			scores.put(linkId, 0.0);
		}
		if (t.size()==0)
			return scores;
		for (ArrayList<String> term: t) {
			HashMap<Long, Double> contentScore, titleScore;
			if (term.size()>1) {
				contentScore = getTermContentScores(term);
				titleScore = getTermTitleScores(term);
				for (long linkId: contentScore.keySet()) {
					docLengthSq.put(linkId, docLengthSq.get(linkId)+Math.pow(contentScore.get(linkId), 2));
					titleLengthSq.put(linkId, titleLengthSq.get(linkId)+Math.pow(titleScore.get(linkId), 2));
					cosContent.put(linkId, cosContent.getOrDefault(linkId, 0.0)+contentScore.get(linkId));
					cosTitle.put(linkId, cosTitle.getOrDefault(linkId, 0.0)+titleScore.get(linkId));
				}
			}
			else {
				contentScore = getWordContentScores(term.get(0));
				titleScore = getWordTitleScores(term.get(0));
			}
			//aggregate with weight sum

			for (long docId: scores.keySet()) {
				cosContent.put(docId, cosContent.getOrDefault(docId, 0.0)+contentScore.get(docId));
				cosTitle.put(docId, cosTitle.getOrDefault(docId, 0.0)+titleScore.get(docId));
			}
		}

		double titleWeight = 0.7;
		double queryLength = Math.sqrt(t.size());
		// divide score by query length and doc length
		for (long docId: scores.keySet()) {
			double weightedContent = 0.0;
			if (Math.sqrt(docLengthSq.get(docId))>0) {
				weightedContent = (1-titleWeight)*cosContent.get(docId)/Math.sqrt(docLengthSq.get(docId));
			} 
			double weightedTitle = 0.0;
			if (Math.sqrt(titleLengthSq.get(docId))>0) {
				weightedTitle = titleWeight*cosTitle.get(docId)/Math.sqrt(titleLengthSq.get(docId));
			} 
			scores.put(docId, (weightedContent+weightedTitle)/queryLength);
		}
		return scores;
	}

	public HashMap<Long, Double> getQueryScore(String q) throws IOException{
		if (validQuery(q)) 
			return aggregateScore(processQuery(q));
		else
			return null;
	}
}
