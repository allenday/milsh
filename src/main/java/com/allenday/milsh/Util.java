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

	public static List<String> stem(String text) {
		return stem(text, 1);
	}
	public static List<String> stem(String text, Integer window){
		Analyzer analyzer = new StandardAnalyzer(Version.LATEST);
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
		
//		System.err.println(buf);
		
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < buf.size() - (window - 1); i++) {
			String b = "";
			for(int j = 0; j < window; j++) {
//				System.err.print(buf.get(i+j) + " ");
				b += buf.get(i+j);
				if (j < window -1) {
					b += " ";
				}
			}
//			System.err.println("\n");
			result.add(b);
		}
		return result;    	
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
