package io.github.hengyunabc.redis;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Test {

	public static void main(String[] args) {
		long buildId = buildId(1565165536640L, 53, 1);
		parseId(buildId);
		long buildIdLast = buildId(1565165536640L, 53, 1023);
		parseId(buildIdLast);
	}
	
	public static long buildId(long miliSecond, long shardId, long seq) {
		return (miliSecond << (12 + 10)) + (shardId << 10) + seq;
	}

	public static void parseId(long id) {
		long miliSecond = id >>> 22;
		long shardId = (id & (0xFFF << 10)) >> 10;
		System.err.println("分布式id-"+id+"生成的时间是："+new SimpleDateFormat("yyyy-MM-dd").format(new Date(miliSecond)));
		System.err.println("分布式id-"+id+"在第"+shardId+"号redis节点生成");
	}

}
