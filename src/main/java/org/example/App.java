package org.example;

/**
 * Hello world!
 *
 */

import java.io.IOException;
import java.util.concurrent.*;

import static java.lang.System.*;

/**
 * @Auther: wangsiwei
 * @Date: 2022/2/18 10:49
 * @Description:
 */
public class App {

    public static void main(String[] args) throws  IOException {
        int corePoolSize = 4;
        int maximumPoolSize = 4;
        long keepAliveTime = 10;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(2);
        RejectedExecutionHandler handler = new MyIgnorePolicy();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit,
                workQueue, handler);
        int precnt = executor.prestartAllCoreThreads();// 预启动所有核心线程
        for (int i = 1; i <= 10; i++) {
            MyTask task = new MyTask(String.valueOf(i),executor);
            executor.execute(task);
        }

        //其中一次的执行结果:
        //task:1 has been createdremainingCapacity:2
        //task:2 has been createdremainingCapacity:1
        //task:3 has been createdremainingCapacity:0
        //task:4 has been createdremainingCapacity:0
        //task:5 has been createdremainingCapacity:0
        //task:6 has been createdremainingCapacity:0
        //task:7 has been createdremainingCapacity:0
        //task:8 has been createdremainingCapacity:0
        //task:9 has been createdremainingCapacity:0
        //task:10 has been createdremainingCapacity:0
        //MyTask [name=3] rejected
        //MyTask [name=4] rejected
        //MyTask [name=5] rejected
        //MyTask [name=6] rejected
        //MyTask [name=7] rejected
        //MyTask [name=8] rejected
        //MyTask [name=9] rejected
        //MyTask [name=10] rejected

        in.read(); //阻塞主线程
    }

    public static class MyIgnorePolicy implements RejectedExecutionHandler {

        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            doLog(r, e);
        }

        private void doLog(Runnable r, ThreadPoolExecutor e) {
            // 可做日志记录等
            err.println( r.toString() + " rejected");
        }
    }

    static class MyTask implements Runnable {
        private String name;
        private ThreadPoolExecutor executor;

        public MyTask(String name,ThreadPoolExecutor executor) {
            this.name = name;
            this.executor = executor;
            int i = executor.getQueue().remainingCapacity();
            out.println( "task:"+name + " has been created"+"remainingCapacity:"+i);
        }

        @Override
        public void run() {
            try {
                Thread.sleep(3000); //让任务执行慢点
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "MyTask [name=" + name + "]";
        }
    }

}
// 观察其中一次的执行结果，从第三个线程就被拒绝。 本来有4个核心线程，为什么第三个就开始拒绝呢？
// 就是因为我们 预启动了核心线程。 预启动之后 executor 中的worker数量从0到4（核心线程数）
// 这样后面再到达的任务就会直接进入队列，而不是直接被核心线程执行。
// 通过 打印创建任务时 队列中剩余位置的数量，我们可以观察到，任务1 2 进入队列之后 3无法进入队列 而且此时 核心线程数=最大线程数 导致任务3直接被拒绝。

//这就是为什么会出现 明明4个核心线程，却执行到第三个线程 就被拒绝。
// 因此我们在 使用 Executors.newFixedThreadPool() 这种 固定线程池大小的时候 要注意队列的长度导致的任务被拒绝的情况。
// 可以拉下源码尝试执行一下 。

//如果我们不预启动核心线程  那么当任务到来时，executor会创建worker 直接执行到来的任务，也就是 任务1 2 3 4 肯定不会被拒绝。

