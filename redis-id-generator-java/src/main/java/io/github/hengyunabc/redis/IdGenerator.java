package io.github.hengyunabc.redis;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class IdGenerator {
	/**
	 * JedisPool, luaSha
	 */
	List<Pair<JedisPool, String>> jedisPoolList;
	int retryTimes;

	int index = 0;

	private IdGenerator(List<Pair<JedisPool, String>> jedisPoolList,
			int retryTimes) {
		this.jedisPoolList = jedisPoolList;
		this.retryTimes = retryTimes;
	}

	static public IdGeneratorBuilder builder() {
		return new IdGeneratorBuilder();
	}

	static class IdGeneratorBuilder {
		List<Pair<JedisPool, String>> jedisPoolList = new ArrayList();
		int retryTimes = 5;

		public IdGeneratorBuilder addHost(String host, int port, String luaSha) {
			jedisPoolList.add(Pair.of(new JedisPool(host, port), luaSha));
			return this;
		}

		public IdGenerator build() {
			return new IdGenerator(jedisPoolList, retryTimes);
		}
	}

	public long next(String tab) {
		for (int i = 0; i < retryTimes; ++i) {
			Long id = innerNext(tab);
			if (id != null) {
				return id;
			}
		}
		throw new RuntimeException("Can not generate id!");
	}

	Long innerNext(String tab) {
		index++;
		int i = index % jedisPoolList.size();
		Pair<JedisPool, String> pair = jedisPoolList.get(i);
		JedisPool jedisPool = pair.getLeft();

		String luaSha = pair.getRight();
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			List<Long> result = (List<Long>) jedis.evalsha(luaSha, 2, tab, ""
					+ i);
			long id = buildId(result.get(0), result.get(1), result.get(2),
					result.get(3));
			return id;
		} catch (JedisConnectionException e) {
			if (jedis != null) {
				jedisPool.returnBrokenResource(jedis);
			}
		} finally {
			if (jedis != null) {
				jedisPool.returnResource(jedis);
			}
		}
		return null;
	}

	public static long buildId(long second, long microSecond, long shardId,
			long seq) {
		long miliSecond = (second * 1000 + microSecond / 1000);
		return (miliSecond << (12 + 10)) + (shardId << 10) + seq;
	}

	public static List<Long> parseId(long id) {
		long miliSecond = id >>> 22;
		long shardId = (id & (0xFFF << 10)) >> 10;

		List<Long> re = new ArrayList<Long>(4);
		re.add(miliSecond);
		re.add(shardId);
		return re;
	}
}
