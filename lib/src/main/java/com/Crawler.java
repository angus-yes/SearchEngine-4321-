package com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;
import java.util.Vector;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import utils.LinkExtractor;
import utils.StringExtractor;

public class Crawler {
	public String url;
	Crawler(String _url)
	{
		url = _url;
	}
	public Vector<String> extractWords() throws ParserException, IOException

	{
		
		StringExtractor se = new StringExtractor (url);
		String str = se.extractStrings(false).replace("\r", " ").replace("\n", " ").replace("\t", " ");
		Vector<String> rtn = new Vector<String>();
		for (String s: str.split("[ ]+")){
	           rtn.add(s);
	        }
		return rtn;

	}
	public Set<String> extractLinks() throws ParserException

	{

	    LinkExtractor le = new LinkExtractor(url);
		return le.extractLinks();
	}
	public long extractDate() throws IOException {
		URLConnection connection = new URL(url).openConnection();
		long mod = connection.getLastModified();
		if (mod == 0)
			mod = connection.getDate();
		return mod;
	}
	public long extractSize() throws IOException, ParserException {
		URLConnection connection = new URL(url).openConnection();
		long size = connection.getContentLengthLong();
		if (size == -1) {
			BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                        connection.getInputStream()));
			size = 0;
			String inputLine;
			while ((inputLine = in.readLine()) != null) 
			size += inputLine.length()+1;
			in.close();
		}
		return size;
	}
	public String extractTitle() throws IOException, ParserException {
		URLConnection connection = new URL(url).openConnection();
		Parser parser = new Parser(connection);
        TagNameFilter filter = new TagNameFilter("title");
        NodeList nodes = parser.extractAllNodesThatMatch(filter);
        Node node = nodes.elementAt(0);
        if (node != null && node.getFirstChild() != null)
        	return node.getFirstChild().getText();
        else
        	return "";
		
	}
}
