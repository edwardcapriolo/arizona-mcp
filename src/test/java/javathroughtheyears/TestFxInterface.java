package javathroughtheyears;

import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestFxInterface {

    @Test
    void oldOldWay() throws InterruptedException {
        abstract class MyFuture implements Runnable {
            protected volatile Object result;
            private volatile boolean done = false;
            @Override
            public void run() {
                this.done= true;
            }
            public Object getResult(){
                return this.result;
            }
            public boolean isDone(){
                return done;
            }
        }
        MyFuture meaningOfLife = new MyFuture() {
            @Override
            public void run() {
                this.result = Long.valueOf(42);
                super.run();
            }
        };
        Thread t = new Thread(meaningOfLife);
        t.start();
        t.join();
        // we have computed the meaning of life
        assertTrue(meaningOfLife.isDone());
        assertEquals(Long.valueOf(42), meaningOfLife.getResult());
    }


    @Test
    void oldWay(){
        Callable<Integer> something = new Callable<>() {
            @Override
            public Integer call() {
                return 4;
            }
        };
        ExecutorService executor = null;
        try {
            //normally you would create this once at application init time
            executor = Executors.newCachedThreadPool();
            Future<Integer> future = executor.submit(something);
            assertEquals(4, future.get());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (executor != null){
                executor.close();
            }
        }
    }

    @Test
    void newWay(){
        Callable<Integer> something = () -> 4;
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<Integer> f = executor.submit(something);
            assertEquals(4, f.get());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void moreConciseWay(){
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            assertEquals(4, executor.submit(() -> 4).get());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void alsoWithRun(){
        Runnable r = () -> System.out.println("hi");
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            assertNull(executor.submit(r).get());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
