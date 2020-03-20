package redisson.lock;

import org.redisson.Redisson;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class CountDownLatchDemo {

  public static void main(String[] args) throws InterruptedException {
    Config config = new Config();
    config.useSingleServer().setAddress("redis://127.0.0.1:6379");

// Sync and Async API
    final RedissonClient redisson = Redisson.create(config);
    final RCountDownLatch latch = redisson.getCountDownLatch("anyCountDownLatch");

    while (true) {
      latch.trySetCount(1);
      latch.await();
    }
  }
}
