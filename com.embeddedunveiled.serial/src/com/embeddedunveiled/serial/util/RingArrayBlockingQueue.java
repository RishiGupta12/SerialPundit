/*
 * Author : Rishi Gupta
 * 
 * This file is part of 'serial communication manager' library.
 * Copyright (C) <2014-2016>  <Rishi Gupta>
 *
 * This 'serial communication manager' is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The 'serial communication manager' is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR 
 * A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with 'serial communication manager'.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.embeddedunveiled.serial.util;

import java.util.AbstractList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * The two lock concurrent queue algorithm : 
 * 
 * The concurrency; several tasks executing simultaneously and may be potentially interacting with each other and 
 * involves context switching, can be addressed by this algorithm. Items can be simultaneously added and removed, 
 * however only one item can be added at a time, and only one item can be removed at a time.
 * 
 * 
 */

/**
 * <p>An expandable blocking/non-blocking FIFO queue backed by a ring buffer. It implements two lock 
 * concurrent queue algorithm.</p>
 * 
 * <p>Insertion methods  : offer(), offer(timeout), add(), put()<br>
 *    Removal methods    :         <br>
 *    Inspection methods :         <br></p>
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
	private int tail = 0;
	private int head = 0;

	// condition for waiting removal from queue
	private final Condition waitForElementToBeAvailable;

	// condition for waiting insertion into queue.
	private final Condition waitForSpaceToBeAvailable;

	/**
	 * 
	 */
	public RingArrayBlockingQueue() {
		capacity = DEFAULT_CAPACITY;
		maxCapacity = DEFAULT_MAX_CAPACITY;
		expandBy = DEFAULT_EXPANSION_BY;
		buffer = (E[]) new Object[capacity];
		waitForElementToBeAvailable = enqueueLock.newCondition();
		waitForSpaceToBeAvailable = dequeueLock.newCondition();
	}

	/**
	 * 
	 * @param maxCapacity
	 * @throws IllegalArgumentException if maxCapacity is zero or negative.
	 */
	public RingArrayBlockingQueue(int maxCapacity) {
		if (maxCapacity <= 0) {
			throw new IllegalArgumentException("Argument maxCapacity can not be negative or zero !");
		}
		capacity = DEFAULT_CAPACITY;
		this.maxCapacity = maxCapacity;
		expandBy = DEFAULT_EXPANSION_BY;
		buffer = (E[]) new Object[capacity];
		waitForElementToBeAvailable = enqueueLock.newCondition();
		waitForSpaceToBeAvailable = dequeueLock.newCondition();
	}

	/**
	 * 
	 * @param capacity
	 * @param expandBy
	 * @param maxCapacity
	 * @throws IllegalArgumentException if capacity/maxCapacity/expandBy is zero or negative.
	 */
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
		waitForElementToBeAvailable = enqueueLock.newCondition();
		waitForSpaceToBeAvailable = dequeueLock.newCondition();
	}

	/**
	 * <p>Retrieves and removes the head of this queue. If the queue is empty, NoSuchElementException 
	 * is thrown.</p>
	 * 
	 * @return the head of this queue.
	 * @throws NoSuchElementException if queue is empty.
	 */
	@Override
	public E remove() {
		E element = poll();
		if (element == null)
			throw new NoSuchElementException();
		return element;
	}

	/**
	 * <p>Retrieves and removes the head of this queue, or returns null if this queue is empty.</p>
	 * 
	 * @return the head of this queue, or null if this queue is empty.
	 */
	@Override
	public E poll() {

		E element = null;

		if (totalElementsInQueue.get() == 0) {
			return null;
		}

		dequeueLock.lock();

		try {
			if(totalElementsInQueue.get() > 0) {

			}
		} finally {
			dequeueLock.unlock();
		}

		return element;
	}

	/**
	 * <p>Retrieves, but does not remove, the head of this queue. If the queue is empty, NoSuchElementException 
	 * is thrown.</p>
	 * 
	 * @return the head of this queue.
	 * @throws NoSuchElementException if queue is empty.
	 */
	@Override
	public E element() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * <p>Retrieves, but does not remove, the head of this queue, or returns null if this queue is empty.</p>
	 * 
	 * @return the head of this queue, or null if this queue is empty.
	 */
	@Override
	public E peek() {
		// TODO Auto-generated method stub
		return null;
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
	@Override
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
	@Override
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
	@Override
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
	 * <p>Retrieves and removes the head of this queue, waiting if necessary until an element becomes available.</p>
	 * 
	 * @return the head of this queue.
	 * @throws InterruptedException if interrupted while waiting.
	 */
	@Override
	public E take() throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
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
	@Override
	public E poll(long timeout, TimeUnit unit) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * <p>Returns the number of additional elements that this queue can ideally (in the absence of memory or resource 
	 * constraints) accept without blocking, or Integer.MAX_VALUE if there is no intrinsic limit. Note that we cannot 
	 * always tell if an attempt to insert an element will succeed by inspecting remainingCapacity because it may be 
	 * the case that another thread is about to insert or remove an element.</p>
	 * 
	 * @return the remaining capacity of this queue.
	 */
	@Override
	public int remainingCapacity() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * <p>Removes all available elements from this queue and adds them to the given collection. This operation may 
	 * be more efficient than repeatedly polling this queue. A failure encountered while attempting to add elements 
	 * to collection c may result in elements being in neither, either or both collections when the associated 
	 * exception is thrown. Attempts to drain a queue to itself result in IllegalArgumentException. Further, the 
	 * behaviour of this operation is undefined if the specified collection is modified while the operation is in 
	 * progress.</p>
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
	@Override
	public int drainTo(Collection<? super E> c) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * <p>Removes at most the given number of available elements from this queue and adds them to the given 
	 * collection. A failure encountered while attempting to add elements to collection c may result in elements 
	 * being in neither, either or both collections when the associated exception is thrown. Attempts to drain a 
	 * queue to itself result in IllegalArgumentException. Further, the behaviour of this operation is undefined 
	 * if the specified collection is modified while the operation is in progress.</p>
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
	@Override
	public int drainTo(Collection<? super E> c, int maxElements) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * <p>Use take() or poll() to get element.</p>
	 * @return always null.
	 */
	@Override
	public E get(int index) {
		return null;
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

	/*
	 * Expand the array by expandBy value.
	 */
	private boolean expandQueue() {

		// If we have reached maximum allowable size, consumer must read the elements from queue to make 
		// room for new elements to be inserted into queue.
		if(buffer.length == maxCapacity) {
			return false;
		}

		enqueueLock.lock();
		dequeueLock.lock();

		try {
			E[] buf = (E[]) new Object[buffer.length + expandBy];

		} finally {
			enqueueLock.unlock();
			dequeueLock.unlock();
		}

		return false;
	}

	/*
	 * <p>Inserts the specified element into this queue, waiting up to the specified wait time if necessary for 
	 * space to become available.</p>
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

		if (e == null) {
			throw new NullPointerException();
		}

		enqueueLock.lock();

		try {
			int totalElementBeforeInsertion = totalElementsInQueue.get();

			if(totalElementBeforeInsertion >= maxCapacity) {
				// Queue is already full and can not be expanded.
				if(timeout == -1) {
					return false;
				}else if(timeout == -2) {
					// handle spurious signal, wait until queue really has space.
					while(totalElementsInQueue.get() == buffer.length) {
						try {
							waitForSpaceToBeAvailable.await();
						}catch (InterruptedException e1) {
							// do nothing, loop back.
						}
					}
				}else {
					long nanosTimeout = unit.toNanos(timeout);
					while(totalElementsInQueue.get() == buffer.length) {
						try {
							// nanosTimeout is 0 or negative if time elapsed.
							if (nanosTimeout > 0) {
								nanosTimeout = waitForSpaceToBeAvailable.awaitNanos(nanosTimeout);
							}
						}catch (InterruptedException e1) {
							// do nothing, loop back.
						}
					}
				}
			}
			else if((totalElementBeforeInsertion == buffer.length) && (buffer.length < maxCapacity)) {
				// Queue is already full and need to be expanded.
				int tmp = buffer.length;
				dequeueLock.lock();
				try {
					if(expandQueue() == true) {
						buffer[tmp] = e;
						totalElementsInQueue.getAndIncrement();
						tail = tmp;
					}else {
						return false;
					}
				} finally {
					dequeueLock.unlock();
				}
			}
			else {
				// Queue is neither full nor requires expansion, freely insert element.
				buffer[tail] = e;
				totalElementsInQueue.getAndIncrement();

				if(totalElementBeforeInsertion == (buffer.length - 1)) {
					// ((1), head is just next to tail), do nothing, after inserting this queue, it has become full.
				}else if(tail == (buffer.length - 1)) {
					// (2), roll over to the beginning of queue.
					tail = 0;
				}else {
					// (3), covers all remaining cases, just increment.
					tail++;
				}
			}

			waitForElementToBeAvailable.signalAll();

		} finally {
			enqueueLock.unlock();
		}

		return true;
	}
}



























