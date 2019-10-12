package redis.DistributedObjects;

import org.redisson.Redisson;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.concurrent.TimeUnit;

public class RRateLimiterDemo {

    public static void main(String[] args) {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");

// Sync and Async API
        final RedissonClient redisson = Redisson.create(config);

        RRateLimiter limiter = redisson.getRateLimiter("rl");
// Initialization required only once.
// 5 permits per 2 seconds
        System.out.println("trySetRate:" + limiter.trySetRate(RateType.OVERALL, 1, 1, RateIntervalUnit.SECONDS));

// ...

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                for (int j = 0; j < Integer.MAX_VALUE; j++) {
//                    System.out.println("limiter.tryAcquire:" + limiter.tryAcquire(1, 1L, TimeUnit.MICROSECONDS));
                    if (limiter.tryAcquire(1, 1L, TimeUnit.MICROSECONDS))
                        System.out.println(Thread.currentThread().getName() + "-" + j);
                }
                // ...
            }).start();
        }
    }
}
