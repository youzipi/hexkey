package org.youzipi.util.concurrent;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author wuqiantai
 */
public class MemoryLimiter {

    private final Instrumentation instrumentation;

    private long memoryLimit;

    private final LongAdder memoryUsed = new LongAdder();

    private final ReentrantLock acquireLock = new ReentrantLock();

    private final Condition notLimited = acquireLock.newCondition();

    private final ReentrantLock releaseLock = new ReentrantLock();

    private final Condition notEmpty = releaseLock.newCondition();


    public MemoryLimiter(final Instrumentation inst) {
        this(Integer.MAX_VALUE, inst);
    }

    public MemoryLimiter(final long memoryLimit, final Instrumentation instrumentation) {
        if (memoryLimit <= 0) {
            throw new IllegalArgumentException();
        }
        this.memoryLimit = memoryLimit;
        this.instrumentation = instrumentation;
    }

    public <E> void acquireInterruptibly(E e) throws InterruptedException {
        if (e == null) {
            throw new NullPointerException();
        }
        acquireLock.lockInterruptibly();
        try {
            long memoryAcquire = instrumentation.getObjectSize(e);
            while ((memoryUsed.sum() + memoryAcquire) >= memoryLimit) {
                notLimited.await();
            }
            memoryUsed.add(memoryAcquire);
            if (memoryUsed.sum() < memoryLimit) {
                notLimited.signal();
            }

        } finally {
            acquireLock.unlock();
        }

        if (memoryUsed.sum() > 0) {
            signalNotEmpty();
        }
    }

    public <E> boolean acquire(E e, long timeout, TimeUnit unit) {
        return false;
    }

    public <E> boolean acquire(E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        if (memoryUsed.sum() >= memoryLimit) {
            return false;
        }

        acquireLock.lock();
        try {
            final long memoryUsedCurr = memoryUsed.sum();
            final long memoryAcquire = instrumentation.getObjectSize(e);
            if ((memoryUsedCurr + memoryAcquire) >= memoryLimit) {
                return false;
            } else {
                memoryUsed.add(memoryAcquire);
                if (memoryUsed.sum() < memoryLimit) {
                    // 为什么在 memoryUsed 增加后，检查，通知 notLimited ?
                    notLimited.signal();
                }
            }

        } finally {
            acquireLock.unlock();
        }

        //
        if (memoryUsed.sum() > 0) {
            signalNotEmpty();
        }

        return true;

    }

    private void signalNotEmpty() {
        releaseLock.lock();
        try {
            notEmpty.signal();
        } finally {
            releaseLock.unlock();
        }
    }

    public <E> void releaseInterruptibly(E e) {

    }

    public <E> void release(E e) {

    }

    public void reset() {

    }

    public void setMemoryLimit(long memoryLimit) {

    }

    public long getMemoryLimit() {
        return 0;
    }

    public long getCurrentMemory() {
        return 0;
    }

    public long getCurrentRemainMemory() {
        return 0;
    }
}
