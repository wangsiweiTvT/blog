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
        int corePoolSize = 2;
        int maximumPoolSize = 4;
        long keepAliveTime = 10;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(4);
        RejectedExecutionHandler handler = new MyIgnorePolicy();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit,
                workQueue, handler);
        int precnt = executor.prestartAllCoreThreads();// 预启动所有核心线程
        for (int i = 1; i <= 10; i++) {
            MyTask task = new MyTask(String.valueOf(i),executor);
            executor.execute(task);
        }

        in.read(); //阻塞主线程
    }

    public static class MyIgnorePolicy implements RejectedExecutionHandler {

        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            doLog(r, e);
        }

        private void doLog(Runnable r, ThreadPoolExecutor e) {
            // 可做日志记录等
            int i = e.getQueue().remainingCapacity();
            err.println( r.toString() + " rejected  remainingCapacity:"+i);
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
                int i = executor.getQueue().remainingCapacity();
                out.println(this.toString() + " is running!"+"remainingCapacity:"+i);
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

