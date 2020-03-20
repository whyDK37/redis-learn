package jedis.bloom;

import org.junit.jupiter.api.Test;
import redis.clients.jedis.JedisPool;

class BloomFilterTest {

  @Test
  public void testFilter() {
    JedisPool jedisPool = new JedisPool();

    BloomFilter filter = new BloomFilter(jedisPool);
    System.out.println(filter.isExist("aaa", "aaa"));
    System.out.println(filter.isExist("aaa", "aaa"));
    System.out.println(filter.isExist("aaa", "bbb"));
    System.out.println(filter.isExist("bbb", "aaa"));
  }
}