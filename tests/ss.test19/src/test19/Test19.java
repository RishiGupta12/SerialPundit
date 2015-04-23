package test19;

import com.embeddedunveiled.serial.SerialComManager;

public class Test19 {
	public static void main(String[] args) {
		
		SerialComManager scm = new SerialComManager();
		
		String PORT = null;
		int osType = SerialComManager.getOSType();
		if(osType == SerialComManager.OS_LINUX) {
			PORT = "/dev/ttyUSB0";
		}else if(osType == SerialComManager.OS_WINDOWS) {
			PORT = "COM51";
		}else if(osType == SerialComManager.OS_MAC_OS_X) {
			PORT = "/dev/cu.usbserial-A70362A3";
		}else if(osType == SerialComManager.OS_SOLARIS) {
			PORT = null;
		}else{
		}
		
		int x = 0;
		long handle = 0;
		for(x=0; x<5000; x++) {
			try {
				handle = scm.openComPort(PORT, true, true, true);
				System.out.println("open status :" + handle + " at " + "x== " + x);
				System.out.println("close status :" + scm.closeComPort(handle));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}
}