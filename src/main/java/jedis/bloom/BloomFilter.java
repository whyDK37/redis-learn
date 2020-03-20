package jedis.bloom;

import com.google.common.hash.Funnels;
import com.google.common.hash.Hashing;
import java.nio.charset.Charset;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

public class BloomFilter {

  private JedisPool jedisPool;
  //预计插入量
  private long expectedInsertions = 1000;
  //可接受的错误率
  private double fpp = 0.001F;
  //布隆过滤器的键在Redis中的前缀 利用它可以统计过滤器对Redis的使用情况
  private String redisKeyPrefix = "bf:";
  private Jedis jedis;

  public BloomFilter(JedisPool jedisPool) {
    this.jedisPool = jedisPool;
    init();
  }

  //利用该初始化方法从Redis连接池中获取一个Redis链接
  private void init() {
    this.jedis = jedisPool.getResource();
  }

  public void setExpectedInsertions(long expectedInsertions) {
    this.expectedInsertions = expectedInsertions;
  }

  public void setFpp(double fpp) {
    this.fpp = fpp;
  }

  public void setRedisKeyPrefix(String redisKeyPrefix) {
    this.redisKeyPrefix = redisKeyPrefix;
  }

  //bit数组长度
  private long numBits = optimalNumOfBits(expectedInsertions, fpp);
  //hash函数数量
  private int numHashFunctions = optimalNumOfHashFunctions(expectedInsertions, numBits);

  //计算hash函数个数 方法来自guava
  private int optimalNumOfHashFunctions(long n, long m) {
    return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
  }

  //计算bit数组长度 方法来自guava
  private long optimalNumOfBits(long n, double p) {
    if (p == 0) {
      p = Double.MIN_VALUE;
    }
    return (long) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
  }

  /**
   * 判断keys是否存在于集合where中
   */
  public boolean isExist(String where, String key) {
    long[] indexs = getIndexs(key);
    boolean result;
    //这里使用了Redis管道来降低过滤器运行当中访问Redis次数 降低Redis并发量
    Pipeline pipeline = jedis.pipelined();
    try {
      for (long index : indexs) {
        pipeline.getbit(getRedisKey(where), index);
      }
      result = !pipeline.syncAndReturnAll().contains(false);
    } finally {
      pipeline.close();
    }
    if (!result) {
      put(where, key);
    }
    return result;
  }

  /**
   * 将key存入redis bitmap
   */
  private void put(String where, String key) {
    long[] indexs = getIndexs(key);
    //这里使用了Redis管道来降低过滤器运行当中访问Redis次数 降低Redis并发量
    Pipeline pipeline = jedis.pipelined();
    try {
      for (long index : indexs) {
        pipeline.setbit(getRedisKey(where), index, true);
      }
      pipeline.sync();
    } finally {
      pipeline.close();
    }
  }

  /**
   * 根据key获取bitmap下标 方法来自guava
   */
  private long[] getIndexs(String key) {
    long hash1 = hash(key);
    long hash2 = hash1 >>> 16;
    long[] result = new long[numHashFunctions];
    for (int i = 0; i < numHashFunctions; i++) {
      long combinedHash = hash1 + i * hash2;
      if (combinedHash < 0) {
        combinedHash = ~combinedHash;
      }
      result[i] = combinedHash % numBits;
    }
    return result;
  }

  /**
   * 获取一个hash值 方法来自guava
   */
  private long hash(String key) {
    Charset charset = Charset.forName("UTF-8");
    return Hashing.murmur3_128().hashObject(key, Funnels.stringFunnel(charset)).asLong();
  }

  private String getRedisKey(String where) {
    return redisKeyPrefix + where;
  }
}