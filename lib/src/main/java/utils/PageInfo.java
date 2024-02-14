package utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class PageInfo {
	public long size;
	public long date;
	public String title, url;
	public HashMap<String, Integer> wordFreq;
	public ArrayList<String> parent, child;
	public double score;
	
	
	public PageInfo(double _score, String _url, long _date, long _size, String _title, HashMap<String, Integer> _wordFreq, ArrayList<String> _parent, ArrayList<String> _child) {
		score = _score;
		size = _size;
		date = _date;
		title = _title;
		url = _url;
		wordFreq = _wordFreq;
		parent = _parent;
		child = _child;
	}
	
	public String dateToString() {
		SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
		return "Date: " + ft.format(date);
	}
	
	public String wordToString() {
		String res = "Keywords: ";
		for (String s: wordFreq.keySet()) {
			res += (s + " " + wordFreq.get(s) + "; ");
		}
		return res;
	}

	public String toString() {
		String res =	"Score: " + score + "\n" +
						"Title: " + title + "\n" +
						"URL: " + url + "\n" +
						dateToString() + " Size: " + size + "\n" +
						wordToString() + "\n" + 
						"Parent:\n";
		for (String s: parent)
			res += (s + "\n");
		res += "Children:\n";
		for (String s: child)
			res += (s + "\n");
		return (res + "-------------------------------------------\n\n");
		
	}
	
	public String toJSPString() {
		String res =	"Score: " + score + "<BR>" +
						"Title: " + "<a href=" + url + ">" + title + "</a>" + "<BR>" +
						"URL: " + "<a href=" + url + ">" + url + "</a>" + "<BR>" +
						dateToString() + " Size: " + size + "<BR>" +
						wordToString() + "<BR>" + 
						"Parent:<BR>";
		for (String s: parent)
			res += ("<a href=" + s + ">" + s + "</a>" + "<BR>");
		res += "Children:<BR>";
		for (String s: child)
			res += ("<a href=" + s + ">" + s + "</a>" + "<BR>");
		return (res + "<hr>");
		
	}
	
}
