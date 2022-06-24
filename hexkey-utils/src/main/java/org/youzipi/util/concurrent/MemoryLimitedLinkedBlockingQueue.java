package org.youzipi.util.concurrent;

import java.lang.instrument.Instrumentation;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * put
 * offer
 * <p>
 * take
 * poll
 * <p>
 * remove
 * <p>
 * clear
 *
 * @author wuqiantai
 * @see <a href="https://github.com/apache/incubator-shenyu/blob/master/shenyu-common/src/main/java/org/apache/shenyu/common/concurrent/MemoryLimitedLinkedBlockingQueue.java/>
 */
public class MemoryLimitedLinkedBlockingQueue<E> extends LinkedBlockingQueue<E> {

    private final MemoryLimiter memoryLimiter;

    public MemoryLimitedLinkedBlockingQueue(final Instrumentation inst) {
        this(Integer.MAX_VALUE, inst);
    }

    public MemoryLimitedLinkedBlockingQueue(
            final long memoryLimit,
            final Instrumentation inst
    ) {
        super(Integer.MAX_VALUE);
        this.memoryLimiter = new MemoryLimiter(memoryLimit, inst);
    }

    public MemoryLimitedLinkedBlockingQueue(
            final Collection<? extends E> c,
            final long memoryLimit,
            final Instrumentation inst
    ) {
        super(c);
        this.memoryLimiter = new MemoryLimiter(memoryLimit, inst);
    }

    /**
     * ========================================================================================
     * observability
     * stat
     * ========================================================================================
     */

    /**
     * set the memory limit.
     *
     * @param memoryLimit the memory limit
     */
    public void setMemoryLimit(final long memoryLimit) {
        memoryLimiter.setMemoryLimit(memoryLimit);
    }

    /**
     * get the memory limit.
     *
     * @return the memory limit
     */
    public long getMemoryLimit() {
        return memoryLimiter.getMemoryLimit();
    }

    /**
     * get the current memory.
     *
     * @return the current memory
     */
    public long getCurrentMemory() {
        return memoryLimiter.getCurrentMemory();
    }

    /**
     * get the current remain memory.
     *
     * @return the current remain memory
     */
    public long getCurrentRemainMemory() {
        return memoryLimiter.getCurrentRemainMemory();
    }

    /**
     * ========================================================================================
     * linkedBlockingQueue operation
     * ========================================================================================
     */

    @Override
    public void put(E e) throws InterruptedException {
        memoryLimiter.acquireInterruptibly(e);
        super.put(e);
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return memoryLimiter.acquire(e, timeout, unit) && super.offer(e, timeout, unit);
    }

    @Override
    public boolean offer(E e) {
        return memoryLimiter.acquire(e) && super.offer(e);
    }

    @Override
    public E take() throws InterruptedException {
        final E e = super.take();
        memoryLimiter.releaseInterruptibly(e);
        return e;
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        final E e = super.poll(timeout, unit);
        memoryLimiter.releaseInterruptibly(e);
        return e;
    }

    @Override
    public E poll() {
        final E e = super.poll();
        memoryLimiter.release(e);
        return e;
    }

    @Override
    public boolean remove(Object o) {
        final boolean isSuccess = super.remove(o);
        if (isSuccess) {
            memoryLimiter.release(o);
        }
        return isSuccess;
    }

    @Override
    public void clear() {
        super.clear();
        memoryLimiter.reset();
    }
}
