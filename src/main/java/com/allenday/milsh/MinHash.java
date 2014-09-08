package com.allenday.milsh;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.cli2.validation.InvalidArgumentException;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * A simple MinHash implementation inspired by https://github.com/jmhodges/minhash
 *
 * @author tpeng (pengtaoo@gmail.com)
 */
public class MinHash {
	private static int MAX_BITCOUNT = 512;
	private static int DEFAULT_BITCOUNT = 32;
	private static int DEFAULT_HASHCOUNT = 10;
	private int bitCount;
	private int hashCount;
	private Random rnd = new Random();
	private BigInteger min;

	public MinHash() throws InvalidArgumentException {
		this(DEFAULT_BITCOUNT, DEFAULT_HASHCOUNT);	
	}

	public MinHash(int bits) throws InvalidArgumentException {
		this(bits, DEFAULT_HASHCOUNT);
	}
	
	public MinHash(int bits, int hashes) throws InvalidArgumentException {
		if ( bits > MAX_BITCOUNT)
			throw new InvalidArgumentException("max bits="+MAX_BITCOUNT);

		bitCount = bits;
		hashCount = hashes;

		min = new BigInteger(bitCount,rnd);
		for (int i = 0; i < bitCount; i++) {
			min = min.setBit(i);			
		}
	}

	public BigInteger bitsample(List<String> tokens) {
		List<BigInteger> mins = hash(tokens);
		BigInteger lsb = new BigInteger(hashCount,rnd);
		for (int h = 0; h < hashCount; h++) {
			lsb = lsb.clearBit(h);
			if (mins.get(h).testBit(bitCount)) {
				lsb = lsb.setBit(h);
			}
		}
		return lsb;
	}
	
	public List<BigInteger> hash(List<String> tokens) {
		List<String> hashes = new ArrayList<String>();
		List<BigInteger> localMins = new ArrayList<BigInteger>();
		
		for (int h = 0; h < hashCount; h++) {
			BigInteger localMin = min;
			for (String token : tokens) {
				try {
					String jj = new String(token.getBytes("UTF-8"));
					String kk = new String(token.getBytes("UTF-8"))+h;
										
					Long j = hash512.hashBytes(jj.getBytes()).asLong();
					Long k = hash512.hashBytes(kk.getBytes()).asLong();
					BigInteger pow = BigInteger.valueOf(j * k);
					BigInteger res = new BigInteger(bitCount,rnd);
					for (int i = 0; i < bitCount; i++) {
						res = res.clearBit(i);		
					}
					res = res.add(pow);

					if (res.compareTo(localMin) < 0) {
						localMin = res;
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}			
			localMins.add(localMin);
		}
		
		return localMins;
	}

	private HashFunction hash = Hashing.murmur3_32();
	private HashFunction hash512 = Hashing.sha512();
}