package redisson;

import java.util.concurrent.TimeUnit;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RLockReactive;
import org.redisson.api.RLockRx;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.api.RedissonRxClient;
import org.redisson.config.Config;

public class RedisDemo {

  public static void main(String[] args) throws InterruptedException {
    Config config = new Config();
    config.useSingleServer().setAddress("redis://127.0.0.1:6379");

    // Sync and Async API
    RedissonClient redisson = Redisson.create(config);

    RLock lock = redisson.getLock("anyLock");
// Most familiar locking method
    lock.lock();
    lock.lock();
    System.out.println("locking...");
    TimeUnit.SECONDS.sleep(30L);
    lock.unlock();
    System.out.println(lock.isLocked());

// Reactive API
    RedissonReactiveClient redissonReactive = Redisson.createReactive(config);

    RLockReactive rLockReactive = redissonReactive.getLock("anyLock");

// RxJava2 API
    RedissonRxClient redissonRx = Redisson.createRx(config);
    RLockRx rLockRx = redissonRx.getLock("anyLock");
  }
}
