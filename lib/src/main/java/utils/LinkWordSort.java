package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class LinkWordSort {

	
	public static ArrayList<Long> sortLinks(HashMap<Long, Double> score){
		class linkEntry{
			public long id;
			public double score;
			public linkEntry(long id, double score) {
				this.id = id;
				this.score = score;
			}
			public String toString() {
				return id + ": " + score + "; ";
			}
		}
		
		Set<Long> ids = score.keySet();
		if (ids.size() == 0)
			return null;
		ArrayList<linkEntry> sortId = new ArrayList<linkEntry>();
		for (long id: ids) {
			linkEntry le = new linkEntry(id, score.get(id));
			sortId.add(le);
		}
		sortId.sort((linkEntry l1, linkEntry l2) -> -1*Double.compare(l1.score, l2.score));
		ArrayList<Long> res = new ArrayList<Long>();
		sortId.forEach(n -> res.add(n.id));
		return res;
	}
	
	public static ArrayList<Long> sortWords(HashMap<Long, Integer> freq){
		class wordEntry{
			public long id;
			public int freq;
			public wordEntry(long id, int freq) {
				this.id = id;
				this.freq = freq;
			}
			public String toString() {
				return id + ": " + freq + "; ";
			}
		}
		
		Set<Long> ids = freq.keySet();
		if (ids.size() == 0)
			return null;
		ArrayList<wordEntry> sortId = new ArrayList<wordEntry>();
		for (long id: ids) {
			wordEntry le = new wordEntry(id, freq.get(id));
			sortId.add(le);
		}
		sortId.sort((wordEntry w1, wordEntry w2) -> -1*Integer.compare(w1.freq, w2.freq));
		ArrayList<Long> res = new ArrayList<Long>();
		sortId.forEach(n -> res.add(n.id));
		return res;
	}
	
}
