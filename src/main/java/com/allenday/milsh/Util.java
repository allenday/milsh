package com.allenday.milsh;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.FixedBitSet;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.ext.EnglishStemmer;

public class Util {
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	final protected static String hex= "0123456789ABCDEF";
	final protected static String[] dehex = {
		"0000","0001","0010","0011","0100","0101","0110","0111",
		"1000","1001","1010","1011","1100","1101","1110","1111",
	};

	public static List<String> stem(String text) {
		return stem(text, 1);
	}
	public static List<String> stem(String text, Integer window){
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_4_10_0);//Version.LATEST);
		EnglishStemmer english = new EnglishStemmer();

		List<String> buf = new ArrayList<String>();
		try {
			TokenStream stream  = analyzer.tokenStream(null, new StringReader(text));
			stream.reset();
			while (stream.incrementToken()) {
				String analyzed = stream.getAttribute(CharTermAttribute.class).toString();
				english.setCurrent(analyzed);
				english.stem();
				buf.add(english.getCurrent());
			}
		} catch (IOException e) {
			// not thrown b/c we're using a string reader...
			throw new RuntimeException(e);
		}
		analyzer.close();
				
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < buf.size() - (window - 1); i++) {
			String b = "";
			for(int j = 0; j < window; j++) {
				b += buf.get(i+j);
				if (j < window -1) {
					b += " ";
				}
			}
			result.add(b);
		}
		return result;    	
	}
	
	public static FixedBitSet parseHexString(String x) {
		FixedBitSet v = new FixedBitSet(x.length() * 4);
		
		int i = 0;
		for (char c : x.toCharArray()) {
			String bits = dehex[hex.indexOf(c)];
			for (char b : bits.toCharArray()) {
				if (b == '1')
					v.set(i);
				i++;
			}
		}		
		return v;
	}

	public static String toHexString(FixedBitSet s) {
		String bb = "";
		for (int i = 0; i < s.length(); i+=8) {
			Integer x = (s.get(i)?1:0)*8
					+ (s.get(i+1)?1:0)*4
					+ (s.get(i+2)?1:0)*2
					+ (s.get(i+3)?1:0)*1
					;
			bb += hexArray[x];
		}
		return bb;
	}
	
	public static String toBitString(FixedBitSet s) {
		String bb = "";
		for (int i = 0; i < s.length(); i++) {
			bb += s.get(i) ? "1" : "0";
		}
		return bb;
	}

	public static Double distance(FixedBitSet a, FixedBitSet b) {
		Double d = 0d;
		Integer h = 0;
		//XXX check for same length
		for (int i = 0; i < a.length(); i++) {
			if (a.get(i) == b.get(i)) {
				h++;
			}
		}
		return 1 - (new Double(h) / new Double(a.length()));
	}

	public static String toHexString(BigInteger i) {
		byte[] b = i.toByteArray();
		char[] hexChars = new char[b.length * 2];
		for ( int j = 0; j < b.length; j++ ) {
			int v = b[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

}
