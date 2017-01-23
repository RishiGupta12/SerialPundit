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

package stm32flashloader;

import java.util.concurrent.atomic.AtomicBoolean;

public final class CommandLineParser implements Runnable {

	private final Object forwardlock = new Object();
	private final Object backwardlock = new Object();
	private final AtomicBoolean exitApplication = new AtomicBoolean(false);
	private final String[] args;

	public CommandLineParser(String[] args) {
		this.args = args;
	}

	@Override
	public void run() {
		for(int i=0;i<args.length;i++) {
			System.out.println(args[i]);  

		}
		CMDExecutorUSART cmdexec = new CMDExecutorUSART(exitApplication, forwardlock, backwardlock);
		Thread t = new Thread(cmdexec);
		t.start();
	}
}
