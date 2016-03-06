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
