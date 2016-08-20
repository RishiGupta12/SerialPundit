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

/** 
 * Entry point to this application.
 */
public final class STM32FlashLoaderApplication {

	private void begin(String[] args) {
		if(args.length == 0) {
			/* GUI mode */
			GUIFrontend gui = new GUIFrontend();
			gui.setUpGUIAndStart();
		}else {
			/* Command line mode */
			CommandLineParser cmdline = new CommandLineParser();
			cmdline.parseCmdLineAndMakeCmdExecute(args);
		}
	}

	public static void main(String[] args) {
		try {
			STM32FlashLoaderApplication floader = new STM32FlashLoaderApplication();
			floader.begin(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
