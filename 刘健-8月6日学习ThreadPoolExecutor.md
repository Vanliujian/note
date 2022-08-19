# 并发编程

> 今日任务

学习ThreadPoolExecutor



- ThreadPoolExecutor源码
- ScheduledThreadPoolExecutor源码
- ForkJoinPool源码
- CopyOnWriteArrayList源码
- ConcurrentHashMap源码
- ArrayBlockingQueue源码
- LinkedBlockQueue源码
- CountDownLatch源码
- Semaphore源码
- CAS、volatile、synchronized、ReentrantLock源码
- ......

以下都是基于jdk1.8的分析

## ThreadPoolExecutor源码分析

### ThreadPoolExecutor应用方式

> 灵魂问手

为什么要使用线程池？

- （参考jdbc一样，原来是每次都需要创建一个连接，再关闭连接，这样很耗费资源，使用jdbc连接池就不需要频繁的创建释放资源）

JDK中提供了Executors，提供了很多封装好的线程池，为什么不直接用封装好的，而是要自己新建一个？

- 提供好的线程池写死了参数，限制很大，不利于执行效率（阿里规范中也建议自己实现线程池）

基本创建

- 下述方法execute和submit区别在于execute只能传Runnable，submit可以传Callable有返回值

```java
import java.util.concurrent.*;

public class Test {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1,
                2,
                500,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactory() {

                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r);
                        t.setName("测试");
                        return t;
                    }
                },
                new ThreadPoolExecutor.AbortPolicy()
        );


        executor.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("aaa");
            }
        });
        Future<Object> future = executor.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return "aaa";
            }
        });
        //get方法会阻塞
        System.out.println(future.get());
    }
}
```



### ThreadPoolExecutor核心参数

构造方法

```java
public ThreadPoolExecutor(int corePoolSize,		//核心线程数
                          int maximumPoolSize,	//最大线程数
                          long keepAliveTime,	//非核心线程存活时间
                          TimeUnit unit,		//keepAliveTime的单位
                          BlockingQueue<Runnable> workQueue,	//阻塞队列
                          ThreadFactory threadFactory,			//线程工厂
                          RejectedExecutionHandler handler) {	//拒绝策略
}
```

例如：

核心线程数现在有2个，最大线程数3个，阻塞队列能存储2个

现在来了六个线程：第一个线程去核心线程，第二个线程去核心线程，第三个线程来的时候，核心线程满了，添加进阻塞队列，第四个线程来了添加进阻塞队列，第五个线程来了发现阻塞队列也满了，线程工厂会创建一个非核心线程，第五个线程进入非核心线程执行。（非核心线程数最大为`最大线程数减核心线程数`）。第六个线程来了发现全满了，会走拒绝策略。

拒绝策略是一个接口，只有一个需要实现的方法，它的实现类有四个（也可以自己实现）

```java
public interface RejectedExecutionHandler {
    void rejectedExecution(Runnable r, ThreadPoolExecutor executor);
}
```

拒绝策略的四个实现类

AbortPolicy：内部只是抛出了一个异常

```java
public static class AbortPolicy implements RejectedExecutionHandler {
    public AbortPolicy() { }
    
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        throw new RejectedExecutionException("Task " + r.toString() +
                                             " rejected from " +
                                             e.toString());
    }
}
```

CallerRunsPolicy：把这个线程又交给主线程来做

```java
public static class CallerRunsPolicy implements RejectedExecutionHandler {
    public CallerRunsPolicy() { }

    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        if (!e.isShutdown()) {
            r.run();
        }
    }
}
```

DiscardOldestPolicy：把阻塞队列的第一个线程poll，把自己线程加进去（如果可以容忍某些线程丢失，可以这样做）

```java
public static class DiscardOldestPolicy implements RejectedExecutionHandler {
    public DiscardOldestPolicy() { }

    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        if (!e.isShutdown()) {
            e.getQueue().poll();
            e.execute(r);
        }
    }
}
```

DiscardPolicy：什么也不做

```java
public static class DiscardPolicy implements RejectedExecutionHandler {
    public DiscardPolicy() { }

    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
    }
}
```



### ThreadPoolExecutor执行流程

<a href="https://sm.ms/image/CDzJ9Px8skE2BYN" target="_blank"><img src="https://s2.loli.net/2022/08/06/CDzJ9Px8skE2BYN.png" ></a> 通过execute方法源码对比

```java
public void execute(Runnable command) {
    if (command == null)
        throw new NullPointerException();
    
    int c = ctl.get();
    //如果当前工作线程小于核心线程数
    if (workerCountOf(c) < corePoolSize) {
        //创建核心线程并执行任务
        //addWorker方法的false表示创建非核心线程，true表示创建核心线程
        if (addWorker(command, true))
            return;
        c = ctl.get();
    }
    //如果当前是running状态且阻塞队列没有排满
    if (isRunning(c) && workQueue.offer(command)) {
        int recheck = ctl.get();
        if (! isRunning(recheck) && remove(command))
            reject(command);
        else if (workerCountOf(recheck) == 0)
            addWorker(null, false);
    }
    //如果创建非核心线程并执行失败就执行拒绝策略
    else if (!addWorker(command, false))
        reject(command);
}
```



### ThreadPoolExecutor状态

#### 线程池的ctl属性

```java
//AtomicInteger本质为int类型数值，只是在加减的时候基于cas实现了线程安全（默认值为0）
private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));

//Integer.SIZE=32表示占用的bit位数，那么COUNT_BITS就等于29
//ctl表述了两个状态
	//1. 当前线程池的状态（高三位）
	//2. 表示线程池当前的工作线程个数（低29位）
private static final int COUNT_BITS = Integer.SIZE - 3;
// 1的二进制
// 00000000 00000000 00000000 00000001
// 左移COUNT_BITS（29）位后
// 00100000 00000000 00000000 00000000
// 再减1
// 00011111 11111111 11111111 11111111
// 得到CAPACITY工作线程的最大数量（包含核心线程和非核心线程）
private static final int CAPACITY   = (1 << COUNT_BITS) - 1;

//线程池的五种状态
// -1的二进制
// 11111111 11111111 11111111 11111111
// 左移29位后
// 11100000 00000000 00000000 00000000
// 高位111表示RUNNING
private static final int RUNNING    = -1 << COUNT_BITS;
//同理000表示SHUTOWN
private static final int SHUTDOWN   =  0 << COUNT_BITS;
//001表示STOP
private static final int STOP       =  1 << COUNT_BITS;
//010表示TIDYING
private static final int TIDYING    =  2 << COUNT_BITS;
//011表示TERMINATED
private static final int TERMINATED =  3 << COUNT_BITS;

// 计算线程池的状态
// c就是当前的ctl假设是  01000000 00000000 00000000 00000100
// ~CAPACITY是取反      11100000 00000000 00000000 00000000
// 两者与操作会得到c的前三位，前三位就是状态
private static int runStateOf(int c)     { return c & ~CAPACITY; }
//同理计算线程池的当前工作线程数
private static int workerCountOf(int c)  { return c & CAPACITY; }
```

#### 线程池的状态变换

<a href="https://sm.ms/image/T3LDCbgprJyZ4Kf" target="_blank"><img src="https://s2.loli.net/2022/08/06/T3LDCbgprJyZ4Kf.png" ></a> 



### execute方法

核心：

- 通过execute方法，查看到线程池的执行流程，以及一些避免并发的情况
- 为什么线程池会添加一个空任务的非核心线程到线程池

```java
public void execute(Runnable command) {
    // 为了程序的健壮性
    if (command == null)
        throw new NullPointerException();
    
    // 获取ctl属性值
    int c = ctl.get();
    //如果当前工作线程小于核心线程数
    if (workerCountOf(c) < corePoolSize) {
        //创建核心线程来执行command任务
        //addWorker方法的false表示创建非核心线程，true表示创建核心线程
        if (addWorker(command, true))
            return;
        //如果创建核心线程失败了，重新获取ctl属性值并继续进行下面的判断
        //因为并发可能有多个同时进来，假设只有一个核心线程可以运行了，其中一个command被addWorker了，其它的addWorker失败，需要重新获取ctl属性继续进行下面的判断
        c = ctl.get();
    }
    //走到这里说明创建核心线程失败
    //如果当前是running状态执行offer方法将其添加进工作队列（阻塞队列）
    if (isRunning(c) && workQueue.offer(command)) {
        //到这里说明把任务添加到工作队列成功
        //再次获取ctl属性
        int recheck = ctl.get();
        //判断线程池是否是RUNNING状态，如果不是RUNNING状态，需要将任务从工作队列中移除
        if (! isRunning(recheck) && remove(command))
            //不是running，也移除了添加进来的任务，执行拒绝策略交给拒绝策略来处理
            reject(command);
        // 是RUNNING或者，不是RUNNING但是remove任务失败，就会走到这里
        else if (workerCountOf(recheck) == 0)
			//如果当前工作线程数为0，并且是RUNNING状态，或者
            //当前工作线程数为0，不是RUNNING状态，但是remove失败了。那么工作队列就有残留任务
            //比如当前是shutdown状态，remove任务失败了，队列中有任务，但是没有工作线程。那么就没有办法从shutdown变为tidying状态，所以需要一个空任务非核心线程，处理工作队列中排队的任务。
            addWorker(null, false);
    }
    //创建非核心线程执行当前任务
    else if (!addWorker(command, false))
        //如果非核心线程也创建失败，那么就执行拒绝策略
        reject(command);
}
```



### addWorker方法

添加工作线程并启动，如果失败会有补救操作

```java
private boolean addWorker(Runnable firstTask, boolean core) {
    //对线程池状态的判断，对工作线程数量的判断
    
    //定义for循环标识，内层for循环可以直接跳出外面的两层for循环
    retry:
    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);

        // 如果当前线程池状态>=shutdown(只有RUNNING状态小于SHUTDOWN)那么就进行后面的判断
        if (rs >= SHUTDOWN &&
            //当前为SHUTDOWN状态，传入的任务为空，工作队列不为空
            //如果上述三个条件都满足，就是要处理工作队列的，放行，不返回false
            //如果上述三个条件有一个不满足，那么就进入这个if返回false
            	//比如
            	//1. 当前>=shutdown,且不是shutdown，那么就没有必要处理任务，return false
            	//2. 当前==shutdown,且传入任务不为null，没有必要处理，直接返回false（shutdown状态不接收任务）
            	//3. 当前==shutdown,且传入的任务为null，工作队列为空，直接返回false（工作队列没有工作要处理，可以直接返回false）
            ! (rs == SHUTDOWN && firstTask == null && ! workQueue.isEmpty()))
            return false;
		//上面的if中，什么情况才不会返回false
        //1. 当前状态是RUNNING
        //2. 或者rs == SHUTDOWN && firstTask == null && ! workQueue.isEmpty()为true
		//上面2是不是似曾相识，就是在execute方法的中间那部分的addWorker方法
        
        for (;;) {
            int wc = workerCountOf(c);
            //如果工作线程数大于最大线程数
            if (wc >= CAPACITY ||
                //如果当前是核心线程，和核心线程数比较，否则和定义的最大线程数比较
                wc >= (core ? corePoolSize : maximumPoolSize))
                //如果工作线程超载了，返回false
                return false;
            //到这里说明工作线程数不大于可用的最大线程数
            //以CAS方式对工作线程数+1，如果成功，跳出最外层的循环并进行下面的创建线程和执行
            if (compareAndIncrementWorkerCount(c))
                break retry;

            c = ctl.get();
            //可能有并发操作，所以需要重新判断线程池的状态，如果和之前的状态不同，重新执行外面的循环
            if (runStateOf(c) != rs)
                continue retry;
        }
    }
    
    //添加工作线程，并启动工作线程
    boolean workerStarted = false;
    boolean workerAdded = false;
    Worker w = null;
    try {
        w = new Worker(firstTask);
        //Worker对象有Thread属性
        final Thread t = w.thread;
        //这里肯定不为null(健壮性判断)
        if (t != null) {
            //加锁
            //为什么要加锁？
            //比如这边正在添加线程池，但是另外有个进程执行了shutdown，那么这个工作线程究竟是添加还是不添加。所以要加锁。
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                int rs = runStateOf(ctl.get());
				//这里判断是否是SHUTDOWN的原因是，如果这边刚好执行到加锁前的那几行代码，其它进程正好执行了shutdown方法
                if (rs < SHUTDOWN ||
                    (rs == SHUTDOWN && firstTask == null)) {
                    // 判断当前是否是run状态，肯定是false，因为是刚创建的线程（健壮性判断）
                    if (t.isAlive())
                        throw new IllegalThreadStateException();
                    //把线程添加进workers，workers是一个hashset对象
                    workers.add(w);
                    
                    //largestPoolSize是用来存储曾经出现过的最线程数量。
                    int s = workers.size();
                    if (s > largestPoolSize)
                        largestPoolSize = s;
                    //线程添加成功，修改状态为true
                    workerAdded = true;
                }
            } finally {
                mainLock.unlock();
            }
            //如果线程添加成功，就启动线程
            if (workerAdded) {
                t.start();
                workerStarted = true;
            }
        }
    } finally {
        //如果线程启动失败，需要做挽回操作
        if (! workerStarted)
            //挽回操作
            addWorkerFailed(w);
    }
    return workerStarted;
}
//线程启动失败的挽回操作
private void addWorkerFailed(Worker w) {
    //同样需要加锁
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        //如果线程不为null，说明已经添加进workers中，需要移除
        if (w != null)
            workers.remove(w);
        //和上面cas形式自增1一样，这是自减1
        decrementWorkerCount();
        //尝试切换状态，先不深入这个
        tryTerminate();
    } finally {
        mainLock.unlock();
    }
}
```



### Worker对象

中断线程不是立即让线程停止，只是将Thread的中断标识设置为true，具体的中断需要一定的契机

```java
private final class Worker
    extends AbstractQueuedSynchronizer	//线程中断
    implements Runnable					//存储需要执行的任务
{
    //工作线程的Thread对象
    final Thread thread;
    //需要执行的任务
    Runnable firstTask;

    //构造方法用来初始化
    Worker(Runnable firstTask) {
        //设置状态为-1，表示刚刚初始化的线程不允许被中断
        setState(-1);
        this.firstTask = firstTask;
        this.thread = getThreadFactory().newThread(this);
    }
	//调用t.start方法会执行run方法
    public void run() {
        runWorker(this);
    }

    // Lock methods
    //
    // The value 0 represents the unlocked state.
    // The value 1 represents the locked state.
	//下面的方法先不深入
    protected boolean isHeldExclusively() {
        return getState() != 0;
    }

    protected boolean tryAcquire(int unused) {
        if (compareAndSetState(0, 1)) {
            setExclusiveOwnerThread(Thread.currentThread());
            return true;
        }
        return false;
    }

    //设置允许中断
    protected boolean tryRelease(int unused) {
        setExclusiveOwnerThread(null);
        setState(0);
        return true;
    }

    public void lock()        { acquire(1); }
    public boolean tryLock()  { return tryAcquire(1); }
    public void unlock()      { release(1); }
    public boolean isLocked() { return isHeldExclusively(); }

    void interruptIfStarted() {
        Thread t;
        if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
            try {
                t.interrupt();
            } catch (SecurityException ignore) {
            }
        }
    }
}
```



### runWorker方法

执行任务流程，进行中断线程相关的lock操作

通过t.start()--->run()--->runWorker()

```java
final void runWorker(Worker w) {
    Thread wt = Thread.currentThread();
    Runnable task = w.firstTask;
    w.firstTask = null;
    //设置允许中断，unlock会调用release(1),release会调用tryRelease，这个调用的是Worker对象的tryRelease
    //将worker的state归位为0，代表可以被中断
    w.unlock();
    //标识（标识勾子函数是否出现异常）
    boolean completedAbruptly = true;
    
    try {
        //获取任务的第一种方式是通过execute、submit传入
        //获取任务的第二种方式是通过getTask方法传入（为了解决上文说的addWorker(null,false)）
        while (task != null || (task = getTask()) != null) {
            //Worker内部实现的锁，不是可重用锁
            //因为在中断时也需要对worker进行lock，如果中断不能获取锁，代表当前工作线程正在执行
            w.lock();
            //之前说过SHUTDOWN状态并不会中断正在执行的任务，STOP会中断正在执行的任务
            //判断当前状态来确定是否中断
            if ((runStateAtLeast(ctl.get(), STOP) ||
                 //查询中断标记位的状态并归位为false。并且当前至少要是STOP状态才可以中断
                 //为什么还要判断状态？因为可能会有并发操作
                 (Thread.interrupted() && runStateAtLeast(ctl.get(), STOP))) &&
                //查询当前线程中断标记位是否是false，如果是false就执行wt.interrupt();
                //因为&&只有前面为true才会执行后面部分，前面为true代表需要中断，那么这里就进行是否中断过判断
                !wt.isInterrupted())
                //将中断标记位设置为true
                wt.interrupt();
            try {
                //勾子函数，前置增强，空实现（需要自己实现线程池时实现）
                beforeExecute(wt, task);
                Throwable thrown = null;
                try {
                    task.run();
                } catch (RuntimeException x) {
                    thrown = x; throw x;
                } catch (Error x) {
                    thrown = x; throw x;
                } catch (Throwable x) {
                    thrown = x; throw new Error(x);
                } finally {
                    //勾子函数，后置增强，空实现（需要自己实现线程池时实现）
                    afterExecute(task, thrown);
                }
            } finally {
                task = null;
                //执行成功的任务数+1
                w.completedTasks++;
                //将state标记位置为0
                w.unlock();
            }
        }
        completedAbruptly = false;
    } finally {
        processWorkerExit(w, completedAbruptly);
    }
}
```

### getTask方法

```java
private Runnable getTask() {
    
    =========================判断当前线程状态============================
    //非核心线程可以干掉
    boolean timedOut = false;

    for (;;) {
        int c = ctl.get();
        int rs = runStateOf(c);
		//如果是>=stop需要移除当前工作线程
        //如果是shutdown且工作队列为空，需要移除当前工作线程
        if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
            decrementWorkerCount();
            //返回null，交给processWorkerExit移除当前工作线程
            return null;
        }
	=========================判断工作线程数量============================
        int wc = workerCountOf(c);
		//allowCoreThreadTimeOut是否允许核心线程超时（一般都是false）
        //当前工作线程数是否大于核心线程数
        boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;

        //工作线程是否大于最大线程（健壮性判断）
        //在allowCoreThreadTimeOut为false情况下，只有当前线程已经超时才会进行下面判断
        if ((wc > maximumPoolSize || (timed && timedOut))
            //工作线程数大于1（如果等于1就不行，干掉后就没有工作线程来执行工作队列中的任务）
            //工作队列为空
            //满足上面两个其一就可以干掉当前工作线程
            && (wc > 1 || workQueue.isEmpty())) {
            //基于cas的方式移除当前线程，可能出现并发操作，但是cas只会移除一个，另外一个continue重新执行
            if (compareAndDecrementWorkerCount(c))
                //返回null，交给processWorkerExit移除当前工作线程
                return null;
            continue;
        }
	=========================从工作队列获取任务============================
        try {
            //timed = allowCoreThreadTimeOut || wc > corePoolSize;
            Runnable r = timed ?
                //阻塞一定时间从工作队列拿任务（简单理解为如果是非核心线程走这个方法）
                workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
            	//一直阻塞（简单理解为如果是核心线程走这个take）
                workQueue.take();
            if (r != null)
                //如果拿到任务直接返回执行
                return r;
            //从队列获取任务时超时了
            timedOut = true;
        } catch (InterruptedException retry) {
            timedOut = false;
        }
    }
}
```

### processWorkerExit方法

```java
private void processWorkerExit(Worker w, boolean completedAbruptly) {
    //什么时候completedAbruptly为true，调用processWorkerExit前，runWorker中的while循环出异常了就为true（一般是勾子函数出异常）
    if (completedAbruptly)
        //执行方式不合法，手动减扣工作线程数
        decrementWorkerCount();

    //加锁
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        //记录当前线程池一共处理了多少任务
        completedTaskCount += w.completedTasks;
        //移除工作线程
        workers.remove(w);
    } finally {
        mainLock.unlock();
    }

    //尝试将线程池关闭
    tryTerminate();

    int c = ctl.get();
    //如果当前状态为RUNNING、SHUTDOWN就进入
    if (runStateLessThan(c, STOP)) {
        //如果是正常移除就进入if
        if (!completedAbruptly) {
            //allowCoreThreadTimeOut：
            	//如果为false，核心线程即使在空闲时间也保持活动状态
            	//如果为true，核心线程使用keepalivetime超时等待
            //核心线程数最小值
            int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
            //如果min为0，且工作队列不为空，设置工作线程最小值为1
            if (min == 0 && ! workQueue.isEmpty())
                min = 1;
            //如果工作线程数>=核心线程数最小值，正常返回
            if (workerCountOf(c) >= min)
                return;
        }
        //如果是非正常移除，就重新添加一个非核心工作线程
        //线程池工作队列不为空，且没有工作线程，再添加一个工作线程
        addWorker(null, false);
    }
}
```



> 小结

今天学习ThreadPoolExecutor的源码，但是还有几个方法没有学完，最主要的是学习它的执行流程，以及遇到各种状态所需要做出的操作。还学习了Worker类，这个类也是ThreadPoolExecutor的内部类。主要用于进行线程的具体执行。

