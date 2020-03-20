package jedis;

import static config.Const.SINGLE_HOST;
import static config.Const.SINGLE_PORT;

import java.util.List;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class PipeLineDemo {

  public static void main(String[] args) {

  }

  //没有使用pipieline的情况下
  public static void testWithoutPipeline() {
    Jedis jedis = new Jedis(SINGLE_HOST, SINGLE_PORT);
    for (int i = 1; i <= 10000; i++) {
      jedis.hset("hashKey-" + i, "field-" + i, "value-" + i);
    }
  }

  //使用pipeline的情况下
  public static void testPipeline() {
    Jedis jedis = new Jedis(SINGLE_HOST, SINGLE_PORT);
    for (int i = 0; i < 100; i++) {
      Pipeline pipeline = jedis.pipelined();
      for (int j = i * 100; i < (i + 1) * 100; j++) {
        pipeline.hset("hashKey-" + j, "field-" + j, "value-" + j);
      }
      List<Object> objects = pipeline.syncAndReturnAll();
      for (Object object : objects) {
        System.out.println(object);
      }
    }
  }
}
