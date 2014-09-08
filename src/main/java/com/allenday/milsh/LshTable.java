package com.allenday.milsh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.util.FixedBitSet;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

public class LshTable {
	private Integer bitCount;
	private Integer chunkCount;
	private Integer radius;
	private LocalitySensitiveHash lsh;
	private Map<FixedBitSet,String> registry = new HashMap<FixedBitSet,String>();
	private List<Map<FixedBitSet,Set<FixedBitSet>>> table = new ArrayList<Map<FixedBitSet,Set<FixedBitSet>>>();

	public String getDocument(FixedBitSet f) {
		return registry.get(f);
	}
	
	public LshTable(Integer _bits, Integer _chunks, Integer _radius) {
		bitCount = _bits;
		chunkCount = _chunks;
		radius = _radius;
		lsh = new LocalitySensitiveHash(bitCount, chunkCount);
		
		for (int i = 0; i < chunkCount; i++) {
			table.add(new HashMap<FixedBitSet,Set<FixedBitSet>>());
		}
	}
	
	public boolean add(FixedBitSet v, String orig) {
		registry.put(v, orig);
		List<FixedBitSet> chunks = lsh.split(v);
		for (int i = 0; i < chunkCount; i++) {
			List<FixedBitSet> neighbors = lsh.neighbors(chunks.get(i),radius);
			for (FixedBitSet neighbor : neighbors) {
				if (!table.get(i).containsKey(neighbor)) {
					table.get(i).put(neighbor, new HashSet<FixedBitSet>());
				}
				table.get(i).get(neighbor).add(v);
			}
		}
		return true;
	}
	
	public Set<FixedBitSet> search(FixedBitSet v) {
		Set<FixedBitSet> results = new HashSet<FixedBitSet>();
		List<FixedBitSet> chunks = lsh.split(v);
		for (int i = 0; i < chunkCount; i++) {
			FixedBitSet chunk = chunks.get(i);
			List<FixedBitSet> neighbors = lsh.neighbors(chunk, radius);
			
			for (FixedBitSet neighbor : neighbors) {
				if (table.get(i).containsKey(neighbor)) {
					results.addAll(table.get(i).get(neighbor));
				}
			}
		}
		return results;
	}
	
	public class LocalitySensitiveHash {
		private Integer N;
		private Integer K;

		public LocalitySensitiveHash(Integer bits, Integer chunks) {
			if (bits % chunks != 0)
				throw new IllegalArgumentException("bits must be divisible by chunks");
			N = bits;
			K = chunks;
		}

		public List<FixedBitSet> neighbors(FixedBitSet v, Integer radius) {
			//XXX throw if radius < 1
			List<FixedBitSet> neighbors = new ArrayList<FixedBitSet>();
			neighbors.add(v.clone());
			
			Integer[] p = new Integer[v.length()];
			for (int i = 0; i < v.length(); i++) {
				p[i] = i;
			}
			
			for (int r = 1; r <= radius; r++) {
				ICombinatoricsVector<Integer> u = Factory.createVector(p);
				Generator<Integer> gen = Factory.createSimpleCombinationGenerator(u, r);
				
				for (ICombinatoricsVector<Integer> slots : gen) {
					FixedBitSet w = (FixedBitSet) v.clone();
					for (Integer slot : slots) {
						w.flip(slot, slot+1);
					}
					neighbors.add(w);
				}

			}
			return neighbors;
		}

		public List<FixedBitSet> split(FixedBitSet v) {
			List<FixedBitSet> chunks = new ArrayList<FixedBitSet>();
			Integer step = N/K;
			
			for (int i = 0; i < N; i += step) {
				FixedBitSet chunk = new FixedBitSet(step);
				chunk.clear(0, chunk.length());
				for (int j = 0; j < step; j++) {
					if (v.length() < i+j) {
					}
					if (v.get(i+j)) {
						chunk.set(j);
					}
				}
				chunks.add(chunk);
			}
			return chunks;
		}
	}
}
