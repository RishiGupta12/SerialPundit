/*
 * This file is part of SerialPundit.
 * 
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.serialpundit.core.util;

import java.util.AbstractList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>An expandable blocking/non-blocking FIFO queue backed by a ring buffer. It implements two lock 
 * concurrent queue algorithm. Inserting and removing elements can happen in parallel but two threads 
 * trying to insert or remove at the same time will be given synchronized access to the queue.</p>
 * 
 * <p>Inserting null elements is not allowed.</p>
 * 
 * <p>Insertion methods  : offer(), offer(timeout), add(),  put() <br>
 *    Removal methods    : poll(), poll(timeout), take(), remove(), peek(), element() <br>
 *    Inspection methods : peek(), size(), isEmpty(), clear(), remainingCapacity()<br></p>
 * 
 * @author Rishi Gupta
 */
public final class RingArrayBlockingQueue<E> extends AbstractList<E> implements BlockingQueue<E> {

    private final int DEFAULT_CAPACITY = 1024;
    private final int DEFAULT_EXPANSION_BY = 1024;
    private final int DEFAULT_MAX_CAPACITY = 4096;

    private final Lock enqueueLock = new ReentrantLock();
    private final Lock dequeueLock = new ReentrantLock();
    private final AtomicInteger totalElementsInQueue = new AtomicInteger(0);

    private final int capacity;
    private final int maxCapacity;
    private final int expandBy;
    private E[] buffer;

    // These 4 are accessed in happen-before relationship across threads.
    private int tail = 0;
    private int head = 0;
    private int headUpdateStatus = 0;
    private int tailUpdateStatus = 0;

    // condition used with blocking await/signal
    private final Condition waitForElementToBeAvailableCond;

    /**
     * <p>Allocate and create queue with default initial capacity (DEFAULT_CAPACITY), default expansion 
     * factor (DEFAULT_EXPANSION_BY) and default maximum allowable size (DEFAULT_MAX_CAPACITY).</p>
     * 
     * @throws IllegalArgumentException if maxCapacity is zero or negative.
     */
    @SuppressWarnings("unchecked")
    public RingArrayBlockingQueue() {
        capacity = DEFAULT_CAPACITY;
        maxCapacity = DEFAULT_MAX_CAPACITY;
        expandBy = DEFAULT_EXPANSION_BY;
        buffer = (E[]) new Object[capacity];
        waitForElementToBeAvailableCond = dequeueLock.newCondition();
    }

    /**
     * <p>Allocate and create queue with default initial capacity (DEFAULT_CAPACITY), default expansion 
     * factor (DEFAULT_EXPANSION_BY) and given maximum allowable size (maxCapacity).</p>
     * 
     * @param maxCapacity maximum size of queue.
     * @throws IllegalArgumentException if maxCapacity is zero or negative.
     */
    @SuppressWarnings("unchecked")
    public RingArrayBlockingQueue(int maxCapacity) {
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("Argument maxCapacity can not be negative or zero !");
        }
        capacity = DEFAULT_CAPACITY;
        this.maxCapacity = maxCapacity;
        expandBy = DEFAULT_EXPANSION_BY;
        buffer = (E[]) new Object[capacity];
        waitForElementToBeAvailableCond = dequeueLock.newCondition();
    }

    /**
     * <p>Allocate and create queue according to the given initial capacity, expansion factor and 
     * maximum allowable size.</p>
     * 
     * @param capacity initial size of queue.
     * @param expandBy number that should be added to current size of queue to expand it.
     * @param maxCapacity maximum size of queue.
     * @throws IllegalArgumentException if capacity/maxCapacity/expandBy is zero or negative.
     */
    @SuppressWarnings("unchecked")
    public RingArrayBlockingQueue(int capacity, int expandBy, int maxCapacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity can not be negative or zero !");
        }
        if (maxCapacity <= 0) {
            throw new IllegalArgumentException("Argument maxCapacity can not be negative or zero !");
        }
        if (expandBy <= 0) {
            throw new IllegalArgumentException("Argument expandBy can not be negative or zero !");
        }
        this.capacity = capacity;
        this.maxCapacity = maxCapacity;
        this.expandBy = expandBy;
        buffer = (E[]) new Object[capacity];

        // when take() blocks, another thread will call signal() on condition associated with 
        // dequeueLock i.e. waitForElementToBeAvailableCond. Similar approach applies for 
        // enque as well.
        waitForElementToBeAvailableCond = dequeueLock.newCondition();
    }

    /*
     * Expand the array by expandBy value. The situation to expand the array arises only when rate of 
     * insertion was higher than rate of removal. If the tail rolled over, this method will re-arrange
     * all elements as it would have been in big linear 1-D array. After expansion next location is 
     * calculated as :
     * next location = number of element at the time this method was called + 1;
     */
    @SuppressWarnings("unchecked")
    private boolean expandQueue() {

        int x = 0;
        int y = 0;
        int z = 0;

        // If we have reached maximum allowable size, consumer must read the elements from queue to make 
        // room for new elements to be inserted into queue. There is no way out, so return false;
        if(buffer.length == maxCapacity) {
            return false;
        }

        enqueueLock.lock();
        dequeueLock.lock();

        // Size of new queue can never be greater than maxCapacity.
        int newLength = buffer.length + expandBy;
        if(newLength >= maxCapacity) {
            newLength = maxCapacity;
        }

        try {
            E[] tmp = (E[]) new Object[newLength];

            if(tail > head) {
                // tail kept increasing linearly and finally the queue was full.
                for(x = 0; x < buffer.length; x++) {
                    tmp[x] = buffer[x];
                }
            }else {
                // tail kept increasing and rolled over, but than found that queue is full.
                y = head;
                for(x = 0; x < (buffer.length - head); x++) {
                    tmp[x] = buffer[y];
                    y++;
                }
                for(z = 0; z <= tail; z++) {
                    tmp[x] = buffer[z];
                    x++;
                }
            }

            // new bigger queue
            buffer = tmp;

        } finally {
            dequeueLock.unlock();
            enqueueLock.unlock();
        }

        return true;
    }

    /*
     * <p>Inserts the specified element into this queue, waiting up to the specified wait time if necessary for 
     * space to become available.</p>
     * 
     * Called by put(), offer() and add().
     * 
     * @param e the element to add.
     * @param timeout -1 for do not wait, -2 for wait indefinitely, for rest values wait as per given timeout value.
     * @param unit a TimeUnit determining how to interpret the timeout parameter, null if timeout is -1 or -2.
     * @return true if element gets inserted into the queue, false otherwise.
     * @throws ClassCastException if the class of the specified element prevents it from being added to this queue.
     * @throws NullPointerException if the specified element is null.
     * @throws IllegalArgumentException if some property of the specified element prevents it from being added to 
     *         this queue.
     */
    private boolean insert(E e, long timeout, TimeUnit unit) throws InterruptedException {

        boolean elementAdded = false;
        int totalElementBeforeInsertion = 0;

        if (e == null) {
            throw new NullPointerException("Null elements may not be inserted in this queue !");
        }

        enqueueLock.lock();

        try {

            totalElementBeforeInsertion = totalElementsInQueue.get();

            if(buffer.length >= maxCapacity) {
                /* buffer can not be expanded further. */

                if(totalElementBeforeInsertion >= maxCapacity) {
                    // Queue is already full and can not be expanded.
                    if(timeout == -1) {
                        return false;
                    }
                    else if(timeout == -2) {
                        // handle spurious signal, wait until queue really has space.
                        while(totalElementsInQueue.get() >= buffer.length) {
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException ie) {
                                throw ie;
                            }
                        }

                        // check if updating tail before insertion is needed or not.
                        if(tailUpdateStatus == -1) {
                            tail = 0;
                            tailUpdateStatus = 0;
                        }else if(tailUpdateStatus == -2) {
                            tail++;
                            tailUpdateStatus = 0;
                        }else {
                        }

                        buffer[tail] = e;
                        totalElementsInQueue.incrementAndGet();
                        elementAdded = true;

                        // update tail an associated status as required.
                        if((totalElementBeforeInsertion + 1) >= buffer.length) {
                            if(tail == (buffer.length - 1)) {
                                tailUpdateStatus = -1;
                            }else {
                                tailUpdateStatus = -2;
                            }
                        }else {
                            if(tail == (buffer.length - 1)) {
                                tail = 0;
                                tailUpdateStatus = 0;
                            }else {
                                tail++;
                                tailUpdateStatus = 0;
                            }
                        }
                    }
                    else {
                        try {
                            Thread.sleep(unit.toMillis(timeout));

                            if (totalElementsInQueue.get() >= buffer.length) {
                                // timed out while waiting.
                                return false;
                            }else {
                                if(tailUpdateStatus == -1) {
                                    tail = 0;
                                    tailUpdateStatus = 0;
                                }else if(tailUpdateStatus == -2) {
                                    tail++;
                                    tailUpdateStatus = 0;
                                }else {
                                }
                                buffer[tail] = e;
                                totalElementsInQueue.incrementAndGet();
                                elementAdded = true;

                                if((totalElementBeforeInsertion + 1) >= buffer.length) {
                                    if(tail == (buffer.length - 1)) {
                                        tailUpdateStatus = -1;
                                    }else {
                                        tailUpdateStatus = -2;
                                    }
                                }else {
                                    if(tail == (buffer.length - 1)) {
                                        tail = 0;
                                        tailUpdateStatus = 0;
                                    }else {
                                        tail++;
                                        tailUpdateStatus = 0;
                                    }
                                }
                            }
                        } catch (InterruptedException ie) {
                            throw ie;
                        }
                    }
                }else {
                    if(tailUpdateStatus == -1) {
                        tail = 0;
                        tailUpdateStatus = 0;
                    }else if(tailUpdateStatus == -2) {
                        tail++;
                        tailUpdateStatus = 0;
                    }else {
                    }

                    buffer[tail] = e;
                    totalElementsInQueue.incrementAndGet();
                    elementAdded = true;

                    if((totalElementBeforeInsertion + 1) >= buffer.length) {
                        if(tail == (buffer.length - 1)) {
                            tailUpdateStatus = -1;
                        }else {
                            tailUpdateStatus = -2;
                        }
                    }else {
                        if(tail == (buffer.length - 1)) {
                            tail = 0;
                            tailUpdateStatus = 0;
                        }else {
                            tail++;
                            tailUpdateStatus = 0;
                        }
                    }
                }
            }else {
                /* buffer can be expanded further if required. */

                if(totalElementBeforeInsertion >= buffer.length) {
                    // queue is already full, expansion is required.
                    int tmp = buffer.length;

                    dequeueLock.lock();
                    try {
                        if(expandQueue() == true) {
                            buffer[tmp] = e;
                            tail = tmp;
                            totalElementsInQueue.incrementAndGet();
                            elementAdded = true;

                            if((totalElementBeforeInsertion + 1) >= buffer.length) {
                                if(tail == (buffer.length - 1)) {
                                    tailUpdateStatus = -1;
                                }
                            }else {
                                if(tail == (buffer.length - 1)) {
                                    tail = 0;
                                    tailUpdateStatus = 0;
                                }else {
                                    tail++;
                                    tailUpdateStatus = 0;
                                }
                            }
                        }else {
                            return false;
                        }
                    } finally {
                        dequeueLock.unlock();
                    }
                }else {
                    if(tailUpdateStatus == -1) {
                        tail = 0;
                        tailUpdateStatus = 0;
                    }else if(tailUpdateStatus == -2) {
                        tail++;
                        tailUpdateStatus = 0;
                    }else {
                    }

                    buffer[tail] = e;
                    totalElementsInQueue.incrementAndGet();
                    elementAdded = true;

                    if((totalElementBeforeInsertion + 1) >= buffer.length) {
                        if(tail == (buffer.length - 1)) {
                            tailUpdateStatus = -1;
                        }else {
                            tailUpdateStatus = -2;
                        }
                    }else {
                        if(tail == (buffer.length - 1)) {
                            tail = 0;
                            tailUpdateStatus = 0;
                        }else {
                            tail++;
                            tailUpdateStatus = 0;
                        }
                    }
                }
            }

            // If the element is inserted into queue, acquire the lock associated with condition and signal others.
            if(elementAdded == true) {
                dequeueLock.lock();
                try {
                    waitForElementToBeAvailableCond.signalAll();
                } finally {
                    dequeueLock.unlock();
                }
            }

        } finally {
            enqueueLock.unlock();
        }

        return true;
    }

    /*
     * <p>Retrieves and removes the head of this queue, waiting up to the specified wait time if necessary for an 
     * element to become available.</p>
     * 
     * poll() and take() calls this method.
     * 
     * @param timeout -1 for do not wait, -2 for wait indefinitely, for rest values wait as per given timeout value.
     * @param unit a TimeUnit determining how to interpret the timeout parameter.
     * @return the head of this queue, or null if the specified waiting time elapses before an element is available.
     * @throws InterruptedException if interrupted while waiting.
     */
    private E removeElement(long timeout, TimeUnit unit) throws InterruptedException {

        E element = null;
        int totalElementBeforeRemoval = 0;

        dequeueLock.lockInterruptibly();

        try {
            totalElementBeforeRemoval = totalElementsInQueue.get();

            if(totalElementBeforeRemoval == 0) {
                // Queue is absolutely empty.

                if(timeout == -1) {
                    return null;
                }
                else if(timeout == -2) {
                    // handle spurious signal, wait until queue really has at-least one element.
                    while(totalElementsInQueue.get() == 0) {
                        try {
                            waitForElementToBeAvailableCond.await();
                        }catch (InterruptedException ie) {
                            waitForElementToBeAvailableCond.signal(); // propagate to non-interrupted thread
                            throw ie;
                        }
                    }
                }
                else {
                    long nanosTimeout = unit.toNanos(timeout);
                    while(totalElementsInQueue.get() == 0) {
                        if(nanosTimeout <= 0) {
                            return null;
                        }
                        try {
                            // nanosTimeout is 0 or negative if time elapsed.
                            if (nanosTimeout > 0) {
                                nanosTimeout = waitForElementToBeAvailableCond.awaitNanos(nanosTimeout);
                            }
                        }catch (InterruptedException ie) {
                            waitForElementToBeAvailableCond.signal(); // propagate to non-interrupted thread
                            throw ie;
                        }
                    }
                }
            }else {
                // Queue has at-least 1 element ready to be removed.
            }

            // check if updating head before removal is needed or not.
            if(headUpdateStatus == -1) {
                head = 0;
                headUpdateStatus = 0;
            }else if(headUpdateStatus == -2) {
                head++;
                headUpdateStatus = 0;
            }else {
            }

            element = buffer[head];
            buffer[head] = null;
            totalElementsInQueue.decrementAndGet();

            if((totalElementBeforeRemoval - 1) <= 0) {
                if(head == (buffer.length - 1)) {
                    headUpdateStatus = -1;
                }else {
                    headUpdateStatus = -2;
                }
            }else {
                if(head == (buffer.length - 1)) {
                    head = 0;
                    headUpdateStatus = 0;
                }else {
                    head++;
                    headUpdateStatus = 0;
                }
            }

            if((totalElementBeforeRemoval - 1) > 0) {
                waitForElementToBeAvailableCond.signalAll();
            }

        }finally {
            dequeueLock.unlock();
        }

        return element;
    }

    /**
     * <p>Retrieves and removes the head of this queue, or returns null if this queue is empty.</p>
     * 
     * @return the head of this queue, or null if this queue is empty.
     */
    public E poll() {
        E element = null;
        try {
            element = removeElement(-1, null);
        } catch (InterruptedException e) {
        }
        return element;
    }

    /**
     * <p>Retrieves and removes the head of this queue, waiting up to the specified wait time if necessary for an 
     * element to become available.</p>
     * 
     * @param timeout how long to wait before giving up, in units of unit.
     * @param unit a TimeUnit determining how to interpret the timeout parameter.
     * @return the head of this queue, or null if the specified waiting time elapses before an element is available.
     * @throws InterruptedException if interrupted while waiting.
     */
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return removeElement(timeout, unit);
    }

    /**
     * <p>Retrieves and removes the head of this queue, waiting if necessary until an element becomes available.</p>
     * 
     * @return the head of this queue.
     * @throws InterruptedException if interrupted while waiting.
     */
    public E take() throws InterruptedException {
        return removeElement(-2, null);
    }

    /**
     * <p>Retrieves and removes the head of this queue. If the queue is empty, NoSuchElementException 
     * is thrown.</p>
     * 
     * @return the head of this queue.
     * @throws NoSuchElementException if queue is empty.
     */
    public E remove() {
        E element = null;
        try {
            element = removeElement(-1, null);
        } catch (InterruptedException e) {
        }

        if (element == null) {
            throw new NoSuchElementException();
        }
        return element;
    }

    /**
     * <p>Retrieves, but does not remove, the head of this queue, or returns null if this queue is empty.</p>
     * 
     * @return the head of this queue, or null if this queue is empty.
     */
    public E peek() {

        E element = null;

        dequeueLock.lock();
        try {
            if(totalElementsInQueue.get() > 0) {
                element = buffer[head];
            }
        }finally {
            dequeueLock.unlock();
        }

        return element;
    }

    /**
     * <p>Retrieves, but does not remove, the head of this queue. If the queue is empty, NoSuchElementException 
     * is thrown.</p>
     * 
     * @return the head of this queue.
     * @throws NoSuchElementException if this queue is empty.
     */
    public E element() {
        E element = peek();
        if (element == null) {
            throw new NoSuchElementException();
        }
        return element;
    }

    /**
     * <p>Use take() or poll() to get an element.</p>
     * 
     * @throws UnsupportedOperationException use other methods to retrieve elements from queue.
     */
    @Override
    public E get(int index) {
        throw new UnsupportedOperationException("Use take() or poll() to get an element !");
    }

    /**
     * <p>Inserts the specified element into this queue if it is possible to do so immediately without violating 
     * capacity restrictions, expanding the queue if required and possible, returning true if elements gets inserted 
     * into queue, false otherwise.</p>
     * 
     * @param e the element to add.
     * @return true if the element was added to this queue otherwise false.
     * @throws ClassCastException if the class of the specified element prevents it from being added to this queue.
     * @throws NullPointerException if the specified element is null.
     * @throws IllegalArgumentException if some property of this element prevents it from being added to this queue.
     */
    public boolean offer(E e) {
        boolean result = false;

        try {
            result = insert(e, -1, null);
        } catch (Exception exp) {
            if(exp instanceof InterruptedException) {
                // This will not happen as insert() will not wait when this version of offer() is invoked.
                return false;
            }else if(exp instanceof NullPointerException) {
                throw new NullPointerException();
            }else if(exp instanceof ClassCastException) {
                throw (ClassCastException) exp;
            }else if(exp instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) exp;
            }else {
                return false;
            }
        }

        return result;
    }

    /**
     * <p>Inserts the specified element into this queue, waiting up to the specified wait time if necessary for 
     * space to become available.</p>
     * 
     * @param e the element to add.
     * @param timeout how long to wait before giving up, in units of unit.
     * @param unit a TimeUnit determining how to interpret the timeout parameter.
     * @return true if successful, or false if the specified waiting time elapses before space is available.
     * @throws ClassCastException if the class of the specified element prevents it from being added to this queue.
     * @throws NullPointerException if the specified element is null.
     * @throws IllegalArgumentException if some property of this element prevents it from being added to this queue.
     */
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return insert(e, timeout, unit);
    }

    /**
     * <p>Inserts the specified element into this queue if it is possible to do so immediately without violating 
     * capacity restrictions, expanding the queue if required and possible, returning true if elements gets inserted 
     * into the queue otherwise throws IllegalStateException exception.</p>
     * 
     * @param e the element to add.
     * @return true if element get added into queue.
     * @throws IllegalStateException if the element cannot be added at this time due to capacity restrictions.
     * @throws ClassCastException if the class of the specified element prevents it from being added to this queue.
     * @throws NullPointerException if the specified element is null and this queue does not permit null elements.
     * @throws IllegalArgumentException if some property of this element prevents it from being added to this queue.
     */
    @Override
    public boolean add(E e) {

        boolean result = false;

        try {
            result = insert(e, -1, null);
        } catch (Exception exp) {
            exp.printStackTrace();
            if(exp instanceof InterruptedException) {
                throw new IllegalStateException();
            }else if(exp instanceof NullPointerException) {
                throw new NullPointerException();
            }else if(exp instanceof ClassCastException) {
                throw (ClassCastException) exp;
            }else if(exp instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) exp;
            }else {
                throw new IllegalStateException();
            }
        }

        return result;
    }

    /**
     * <p>Inserts the specified element into this queue, waiting if necessary for space to become available.</p>
     * 
     * @param e the element to add.
     * @throws InterruptedException if interrupted while waiting.
     * @throws ClassCastException if the class of the specified element prevents it from being added to this queue.
     * @throws NullPointerException if the specified element is null.
     * @throws IllegalArgumentException if some property of this element prevents it from being added to this queue.
     */
    public void put(E e) throws InterruptedException {

        try {
            insert(e, -2, null);
        } catch (Exception exp) {
            if(exp instanceof InterruptedException) {
                throw new IllegalStateException();
            }else if(exp instanceof NullPointerException) {
                throw new NullPointerException();
            }else if(exp instanceof ClassCastException) {
                throw (ClassCastException) exp;
            }else if(exp instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) exp;
            }else {
                throw new IllegalStateException();
            }
        }

    }

    /**
     * <p>Returns the number of additional elements that this queue can ideally (in the absence of memory or resource 
     * constraints) accept without blocking. Note that we cannot always tell if an attempt to insert an element will 
     * succeed by inspecting remainingCapacity because it may be the case that another thread is about to insert or 
     * remove an element.</p>
     * 
     * @return the remaining capacity of this queue.
     */
    public int remainingCapacity() {

        int remainingSize = 0;

        enqueueLock.lock();
        dequeueLock.lock();

        try {
            remainingSize = buffer.length - totalElementsInQueue.get();
        } finally {
            dequeueLock.unlock();
            enqueueLock.unlock();
        }

        return remainingSize;
    }

    /**
     * <p>Returns the number of elements in this collection. If this collection contains more than Integer.MAX_VALUE 
     * elements, returns Integer.MAX_VALUE.</p>
     * 
     * @return the number of elements in this collection.
     */
    @Override
    public int size() {
        return totalElementsInQueue.get();
    }

    /**
     * <p>Removes all available elements from this queue and adds them to the given collection. This operation may 
     * be more efficient than repeatedly polling this queue. A failure encountered while attempting to add elements 
     * to collection c may result in elements being in neither, either or both collections when the associated 
     * exception is thrown.</p>
     * 
     * <p>Attempts to drain a queue to itself result in IllegalArgumentException. Further, the behavior of this 
     * operation is undefined if the specified collection is modified while the operation is in progress.</p>
     * 
     * <p>This batched reading style may be useful in case where rate of insertion in queue is faster than rate at 
     * which elements are removed from queue.</p>
     * 
     * @param c the collection to transfer elements into.
     * @return the number of elements transferred.
     * @throws UnsupportedOperationException if addition of elements is not supported by the specified collection.
     * @throws ClassCastException if the class of an element of this queue prevents it from being added to the 
     *          specified collection.
     * @throws NullPointerException if the specified collection is null.
     * @throws IllegalArgumentException if the specified collection is this queue, or some property of an element 
     *          of this queue prevents it from being added to the specified collection.
     */
    public int drainTo(Collection<? super E> c) {

        int x = 0;
        int y = 0;
        int numElementsInQueueRightNow = 0;

        if(c == null) {
            throw new NullPointerException();
        }
        if(c == this) {
            throw new IllegalArgumentException();
        }

        // acquire both the locks to take snapshot of queue to return.
        dequeueLock.lock();
        enqueueLock.lock();

        try {

            numElementsInQueueRightNow = totalElementsInQueue.get();

            if(numElementsInQueueRightNow != 0) {
                if(headUpdateStatus == -1) {
                    x = 0;
                }else if(headUpdateStatus == -2) {
                    x = ++head;
                }else {
                    x = head;
                }
                while(y < numElementsInQueueRightNow) {
                    c.add(buffer[x]);
                    buffer[x] = null;
                    x++;
                    if(x == buffer.length) {
                        x = 0;
                    }
                    y++;
                }
            }

            totalElementsInQueue.set(0);
            head = 0;
            tail = 0;
            headUpdateStatus = 0;
            tailUpdateStatus = 0;
        } finally {
            enqueueLock.unlock();
            dequeueLock.unlock();
        }

        return numElementsInQueueRightNow;
    }

    /**
     * <p>Removes at most the given number of available elements from this queue and adds them to the given 
     * collection. A failure encountered while attempting to add elements to collection c may result in elements 
     * being in neither, either or both collections when the associated exception is thrown.</p>
     * 
     * <p>Attempts to drain a queue to itself result in IllegalArgumentException. Further, the behavior of this 
     * operation is undefined if the specified collection is modified while the operation is in progress.</p>
     * 
     * <p>This batched reading style may be useful in case where rate of insertion in queue is faster than rate at 
     * which elements are removed from queue.</p>
     * 
     * @param c the collection to transfer elements into.
     * @param maxElements the maximum number of elements to transfer.
     * @return the number of elements transferred.
     * @throws UnsupportedOperationException if addition of elements is not supported by the specified collection.
     * @throws ClassCastException if the class of an element of this queue prevents it from being added to the 
     *          specified collection.
     * @throws NullPointerException if the specified collection is null.
     * @throws IllegalArgumentException if the specified collection is this queue, or some property of an element 
     *          of this queue prevents it from being added to the specified collection.
     */
    public int drainTo(Collection<? super E> c, int maxElements) {

        int x = 0;
        int y = 0;
        int numOfElementsToDrain = 0;
        int numElementsInQueueRightNow = 0;

        if(c == null) {
            throw new NullPointerException();
        }
        if(c == this) {
            throw new IllegalArgumentException();
        }

        // acquire both the locks to take snapshot of queue to return.
        dequeueLock.lock();
        enqueueLock.lock();

        try {

            numElementsInQueueRightNow = totalElementsInQueue.get();

            if(numElementsInQueueRightNow > maxElements) {
                numOfElementsToDrain = maxElements;
            }else {
                numOfElementsToDrain = numElementsInQueueRightNow;
            }

            if(numElementsInQueueRightNow > 0) {
                if(headUpdateStatus == -1) {
                    x = 0;
                }else if(headUpdateStatus == -2) {
                    x = ++head;
                }else {
                    x = head;
                }
                while(y < numOfElementsToDrain) {
                    c.add(buffer[x]);
                    buffer[x] = null;
                    x++;
                    if(x == buffer.length) {
                        x = 0;
                    }
                    y++;
                }
            }

            totalElementsInQueue.set(0);
            head = 0;
            tail = 0;
            headUpdateStatus = 0;
            tailUpdateStatus = 0;
        } finally {
            enqueueLock.unlock();
            dequeueLock.unlock();
        }

        return numOfElementsToDrain;
    }

    /**
     * <p>Atomically removes all of the elements from this queue. The queue will be empty after this 
     * call returns.</p>
     */
    @Override
    public void clear() {

        int x = 0;
        int y = 0;
        int numElementsInQueueRightNow = 0;

        // acquire both the locks to take snapshot of queue to return.
        dequeueLock.lock();
        enqueueLock.lock();

        try {

            numElementsInQueueRightNow = totalElementsInQueue.get();

            if(numElementsInQueueRightNow > 0) {
                if(headUpdateStatus == -1) {
                    x = 0;
                }else if(headUpdateStatus == -2) {
                    x = ++head;
                }else {
                    x = head;
                }
                while(y < numElementsInQueueRightNow) {
                    buffer[x] = null;
                    x++;
                    if(x == buffer.length) {
                        x = 0;
                    }
                    y++;
                }
            }

            totalElementsInQueue.set(0);
            head = 0;
            tail = 0;
            headUpdateStatus = 0;
            tailUpdateStatus = 0;
        } finally {
            enqueueLock.unlock();
            dequeueLock.unlock();
        }
    }

    /**
     * <p>Returns true if queue is not empty otherwise false.</p>
     * 
     * @return true if queue is not empty otherwise false.
     */
    public boolean isEmpty() {
        if(totalElementsInQueue.get() > 0) {
            return false;
        }
        return true;
    }

    /**
     * <p>Provide an array containing all the elements in the same as they were inserted into this 
     * queue. The drainTo() method removes elements from the queue while this method does not remove 
     * elements from queue.</p>
     * 
     * @return an array containing all the elements in this queue.
     */
    public Object[] toArray() {

        int x = 0;
        int y = 0;
        int z = 0;
        int numElementsInQueueRightNow = 0;
        Object[] r = null;

        dequeueLock.lock();
        enqueueLock.lock();

        try {
            numElementsInQueueRightNow = totalElementsInQueue.get();

            if(numElementsInQueueRightNow > 0) {

                r = new Object[numElementsInQueueRightNow];

                if(headUpdateStatus == -1) {
                    x = 0;
                }else if(headUpdateStatus == -2) {
                    x = ++head;
                }else {
                    x = head;
                }

                while(y < numElementsInQueueRightNow) {
                    r[z] = buffer[x];
                    x++;
                    if(x == buffer.length) {
                        x = 0;
                    }
                    z++;
                    y++;
                }
            }else {
                return new Object[0];
            }

        } finally {
            enqueueLock.unlock();
            dequeueLock.unlock();
        }

        return r;
    }
}
