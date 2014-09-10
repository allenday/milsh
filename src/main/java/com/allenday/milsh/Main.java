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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.cli2.validation.InvalidArgumentException;
import org.apache.lucene.util.FixedBitSet;

public class Main {
	private static int numThreads = 2;
	private static LshTable table;
	private static MinHash mh;
	private static int linesRead = 0;

	public static int B = 32;
	public static int H = 64; //128
	public static int K = 32;
	public static int R = 1;
	
	
	public static class LineReader implements Runnable {
		BufferedReader br;
		private LineReader(BufferedReader _br) {
			br = _br;
		}
/*
		@Override
		public Object call() {
			try {
				while (br.ready()) {
					if (linesRead % 100 == 0) {
						System.err.println(linesRead);
					}
					if (linesRead > 5000) {
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

			return null;
		}
*/		
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
						throw new Exception(vA.length() + " < " + H);
					}
					System.out.println(Util.toHexString(vA)+"\t"+line);
//					table.add(vA,line);
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

	public static void main(String[] args) throws InvalidArgumentException, IOException, InterruptedException, ExecutionException, TimeoutException {
		mh = new MinHash(B, H);
		table = new LshTable(H, K, R);
		
		String filename = "b3_all_names.minhash";
		BufferedReader br = new BufferedReader(new FileReader(new File(filename)));

		int i = 0;
		while (br.ready()) {
			String[] parts = br.readLine().split("\t");
			String hex = parts[0];
			String label = parts[1];

			FixedBitSet v = Util.parseHexString(hex);
			table.add(v, label);
			if (i % 10000 == 0) {
				System.err.println(i + "\t" + Runtime.getRuntime().totalMemory());
			}
			i++;
		}
//		String hex = "AF676F1FF1B517D9";
		
/*		String filename = "/tmp/head1M.txt";
		if (args.length > 0) {
			filename = args[0];
		}
		if (args.length > 1) {
			numThreads = Integer.parseInt(args[1]);
		}
System.err.println(filename);
System.err.println(numThreads);
		BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
		
//		String q = "South Park: The Stick of Truth";
//		List<String> tokq1 = Util.stem(q, 1);
//		List<String> tokq2 = Util.stem(q, 2);
//		List<String> tokq3 = new ArrayList<String>();
//		tokq3.addAll(tokq1);
//		tokq3.addAll(tokq2);
//
//		BigInteger bQ = mh.bitsample(tokq3);
//		FixedBitSet vQ = new FixedBitSet(bQ.bitLength());
//		for (int i = 0; i < bQ.bitLength(); i++) {
//			if (bQ.testBit(i)) {
//				vQ.set(i);
//			}
//		}
//
//		System.err.println(Util.stem("South Park S10E05.720p WEBRip H264-DEADPOOL[rarbg]".replaceAll("\\.", " "),1));
//		System.err.println(Util.stem("South Park S10E05.720p WEBRip H264-DEADPOOL[rarbg]".replaceAll("\\.", " "),2));

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0 ; i < numThreads ; i++) {
			Runnable task = new LineReader(br);
			executor.execute(task);
//			f.get(180, TimeUnit.SECONDS);
		}
		
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
	
//		System.err.println("done reading");
//
//		Set<FixedBitSet> results = table.search(vQ);
//		TreeMap<Double,String> res = new TreeMap<Double,String>();
//		for (FixedBitSet result : results) {
//			res.put(Util.distance(vQ, result), table.getDocument(result));
//			//			System.err.println(result + " " + Util.distance(vQ, result) + " " + table.getDocument(result) );
//		}
//		for (Double d : res.descendingKeySet()) {
//			System.err.println(d + "\t" + res.get(d));
//		}
*/
	}
}
