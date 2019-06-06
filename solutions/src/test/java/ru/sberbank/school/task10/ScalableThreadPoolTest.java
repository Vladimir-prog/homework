package ru.sberbank.school.task10;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ScalableThreadPoolTest {
    private ThreadPool threadPool;

    @Before
    public void init() {
        threadPool = new ScalableThreadPool(3, 10);
        threadPool.start();
    }

    @After
    public void stop() {
        threadPool.stopNow();
    }

    @Test(timeout = 400)
    public void normalWork() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 25; i++) {
            threadPool.execute(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(40);
                } catch (InterruptedException ignore) {
//                    Thread.currentThread().interrupt();
                }
                synchronized (counter) {
                    System.out.println("Task: " + counter.incrementAndGet()
                            + " - Thread: " + Thread.currentThread().getName());
                }
            });
        }
        TimeUnit.MILLISECONDS.sleep(100);
        for (int i = 0; i < 25; i++) {
            TimeUnit.MILLISECONDS.sleep(6);
            threadPool.execute(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(5);
                } catch (InterruptedException ignore) {
//                        Thread.currentThread().interrupt();
                }
                synchronized (counter) {
                    System.out.println("Task: " + counter.incrementAndGet()
                            + " - Thread: " + Thread.currentThread().getName());
                }
            });
        }
        TimeUnit.MILLISECONDS.sleep(50);
        Assertions.assertEquals(50, counter.get());
    }

    @Test(timeout = 400)
    public void callableService() throws InterruptedException, ExecutionException {
        final List<Future> futureList = new ArrayList<>();
        final AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 25; i++) {
            Future<String> future = threadPool.execute(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException ignore) {
//                        Thread.currentThread().interrupt();
                }
                String res;

                synchronized (counter) {
                    res = "Task: " + counter.incrementAndGet() + " - Thread: " + Thread.currentThread().getName();
                }
                return res + " " + LocalDateTime.now().toString();

            });
            futureList.add(future);
        }
        for (Future future : futureList) {
            System.out.println(future.get());
        }
        Assertions.assertEquals(25, counter.get());
    }
}

