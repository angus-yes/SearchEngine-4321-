package utils;

import java.io.Serializable;
import java.util.ArrayList;

public class Posting implements Serializable{
	public long doc;
	public ArrayList<Integer> pos;
	public Posting(long doc, ArrayList<Integer> pos)
	{
		this.doc = doc;
		this.pos = pos;
	}
}
