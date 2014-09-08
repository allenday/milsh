package com.allenday.milsh;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.cli2.validation.InvalidArgumentException;
import org.apache.lucene.util.FixedBitSet;

public class Main {
	private static int numThreads = 3;
	private static LshTable table;
	private static MinHash mh;
	private static int linesRead = 0;

	public static int B = 32;
	public static int H = 128;
	public static int K = 32;
	public static int R = 1;
	
	
	public static class LineReader implements Runnable {
		BufferedReader br;
		private LineReader(BufferedReader _br) {
			br = _br;
		}

		public synchronized void run() {
			try {
				while (br.ready()) {
					if (linesRead % 100 == 0) {
						System.err.println(linesRead);
					}
					if (linesRead > 30000) {
						break;
					}
					linesRead++;
					String line = br.readLine();
					line.replaceAll("\\.", " ");
					List<String> tok1 = Util.stem(line, 1);
					List<String> tok2 = Util.stem(line, 2);
					List<String> tok3 = new ArrayList<String>();
					tok3.addAll(tok1);
					tok3.addAll(tok2);
					if (tok3.size() == 0) {
						System.err.println("no tokens: " + line);
						continue;
					}
					BigInteger bA = mh.bitsample(tok3);
					FixedBitSet vA = new FixedBitSet(H);
					for (int i = 0; i < H; i++) {
						if (bA.testBit(i)) {
							vA.set(i);
						}
					}
					if (vA.length() < H) {
						System.err.println("X");
						throw new Exception(vA.length() + " < " + H);
					}
					table.add(vA,line);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws InvalidArgumentException, IOException, InterruptedException {
		String filename = "/tmp/head1M.txt";
		if (args.length > 0) {
			filename = args[0];
		}
		if (args.length > 1) {
			numThreads = Integer.parseInt(args[1]);
		}
System.err.println(filename);
System.err.println(numThreads);
		BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
		mh = new MinHash(B, H);
		table = new LshTable(H, K, R);

		String q = "South Park: The Stick of Truth";
		List<String> tokq1 = Util.stem(q, 1);
		List<String> tokq2 = Util.stem(q, 2);
		List<String> tokq3 = new ArrayList<String>();
		tokq3.addAll(tokq1);
		tokq3.addAll(tokq2);

		BigInteger bQ = mh.bitsample(tokq3);
		FixedBitSet vQ = new FixedBitSet(bQ.bitLength());
		for (int i = 0; i < bQ.bitLength(); i++) {
			if (bQ.testBit(i)) {
				vQ.set(i);
			}
		}

		System.err.println(Util.stem("South Park S10E05.720p WEBRip H264-DEADPOOL[rarbg]".replaceAll("\\.", " "),1));
		System.err.println(Util.stem("South Park S10E05.720p WEBRip H264-DEADPOOL[rarbg]".replaceAll("\\.", " "),2));

		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0 ; i < numThreads ; i++) {
			Runnable task = new LineReader(br);
			Thread worker = new Thread(task);
			worker.start();
			threads.add(worker);
		}

		int tc = 0;
		/*
		do {
			tc = 0;
			for (Thread thread : threads) {
				if (thread.isAlive()) {
					tc++;
				} else {
					thread.join();
				}
			}
			System.out.println("We have " + tc + " running threads. ");
			Thread.sleep(10000);
		} while (tc > 0);
		*/
		
		for (Thread thread : threads) {
			System.err.println("waiting on thread "+thread);
			thread.join();
		}
		
		System.err.println("done reading");

		Set<FixedBitSet> results = table.search(vQ);
		TreeMap<Double,String> res = new TreeMap<Double,String>();
		for (FixedBitSet result : results) {
			System.err.println(result.length() + " " + vQ.length());
			res.put(Util.distance(vQ, result), table.getDocument(result));
			//			System.err.println(result + " " + Util.distance(vQ, result) + " " + table.getDocument(result) );
		}
		for (Double d : res.descendingKeySet()) {
			System.err.println(d + "\t" + res.get(d));
		}

		//		Set<FixedBitSet> results = table.search(vB);
		//		for (FixedBitSet result : results) {
		//			System.err.println("\nd "+Util.toBitString(vA)+"\nq "+Util.toBitString(vB)+"\nr "+Util.toBitString(result));			
		//		}
		//		System.err.println("dist="+Util.distance(vA, vB));

		//String bb = "";
		//for (FixedBitSet chunk : lA) {
		//	bb += Util.toBitString(chunk) + ",";
		//}
		//System.err.println(bb);

	}
}
