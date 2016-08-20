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

package ringbuffer;

import java.util.ArrayList;

import com.serialpundit.core.util.RingArrayBlockingQueue;

class Test1 implements Runnable {

	final RingArrayBlockingQueue<Integer> q;

	public Test1(RingArrayBlockingQueue<Integer> q) {
		this.q = q;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		q.add(10);
		q.add(11);
	}
}

class Test2 implements Runnable {
	int x = 0;
	final RingArrayBlockingQueue<Integer> q;

	public Test2(RingArrayBlockingQueue<Integer> q) {
		this.q = q;
	}

	@Override
	public void run() {
		while(true) {
			try {
				q.add(x);
				x++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

public final class RingBuffer {

	public static void main(String[] args) {

		RingArrayBlockingQueue<Integer> q = new RingArrayBlockingQueue<Integer>(2, 1, 4);

		for(int x=0; x<5; x++) {
			q.offer(x);
		}

		System.out.println("ff : " + q.remainingCapacity() + " " + q.size());
		q.clear();
		System.out.println("gg : " + q.remainingCapacity() + " " + q.size());

		for(int x=0; x<5; x++) {
			q.offer(x);
		}

		Thread t = new Thread(new Test1(q));
		t.start();

		try {
			for(int x=0; x<4; x++) {
				System.out.println("q.take() : " + q.take());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for(int x=0; x<5; x++) {
			q.offer(x);
		}

		try {
			q.take();
			q.take();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("donwe");
		ArrayList al = new ArrayList();
		int num = q.drainTo(al);

		System.out.println("o : " + num);
		for(int x=0; x<al.size(); x++) {
			System.out.println("" + al.get(x));
		}

		for(int x=0; x<5; x++) {
			q.add(x);
		}

		ArrayList al1 = new ArrayList();
		int num1 = q.drainTo(al1, 3);

		System.out.println("o1 : " + num1);
		for(int x=0; x<al1.size(); x++) {
			System.out.println("" + al1.get(x));
		}

		for(int x=0; x<5; x++) {
			q.add(x);
		}

		System.out.println("\n");
		Object[] i = (Object[]) q.toArray();
		for(int x=0; x < i.length; x++) {
			System.out.println("" + i[x]);
		}

		System.out.println("\n");
		Thread t1 = new Thread(new Test2(q));
		t1.start();
		while(true) {
			try {
				System.out.println("Take : " + q.take());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		//        System.out.println("done !");
	}
}
