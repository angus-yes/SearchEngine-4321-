package utils;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.htmlparser.beans.LinkBean;
import org.htmlparser.util.ParserException;

public class LinkExtractor {
	private String link = "";
	public LinkExtractor(String url){
		link = url;
	}
	
	public void printLinks() throws ParserException

	{

	    LinkBean lb = new LinkBean();
	    lb.setURL(link);
	    URL[] URL_array = lb.getLinks();
	    for(int i=0; i<URL_array.length; i++){
	    	System.out.println(URL_array[i]);
	    }
	}
	public Set<String> extractLinks() throws ParserException

	{
	    Set<String> v_link = new HashSet<String>();
	    LinkBean lb = new LinkBean();
	    lb.setURL(link);
	    URL[] URL_array = lb.getLinks();
	    for(int i=0; i<URL_array.length; i++){
	    	v_link.add(URL_array[i].toString());
	    }
	    return v_link;
	}
}
