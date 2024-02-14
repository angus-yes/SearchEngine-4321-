package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

import utils.Porter;

public class StopStem {
	private Porter porter;
	private HashSet<String> stopWords;

	public StopStem(String str) 
	{
		super();
		porter = new Porter();
		stopWords = new HashSet<String>();
				
		BufferedReader in = null;
		try {
		in = new BufferedReader(new FileReader(new File(str)));
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		while(true) {
		try {
			String word = in.readLine();
			if (word==null)
				break;
			stopWords.add(word);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		}
	}
	public String stem(String str)
	{
		return porter.stripAffixes(str);
	}
	
	public String process(String str) {
		if (stopWords.contains(str) || str.length() == 0) 
			return "";
		else
			return stem(str);
	}
}
