package test21;

import com.embeddedunveiled.serial.IPortMonitor;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

class portWatcher implements IPortMonitor{
	@Override
	public void onPortRemovedEvent() {	
		System.out.println("PORT REMOVED");
	}
}

public class Test21 {
	public static void main(String[] args) {
		
		long handle = 0;
		SerialComManager scm = new SerialComManager();
		portWatcher pw = new portWatcher();
		
		try {
			handle = scm.openComPort("/dev/ttyUSB0", true, true, false);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.SOFTWARE, '$', '#', false, false);
			scm.registerPortMonitorListener(handle, pw);
			
			Thread.sleep(500);
			scm.unregisterPortMonitorListener(handle);
			
			Thread.sleep(500);
			scm.registerPortMonitorListener(handle, pw);
			
			Thread.sleep(500);
			scm.unregisterPortMonitorListener(handle);
			
			Thread.sleep(500);
			scm.registerPortMonitorListener(handle, pw);
			
			while(true);
			
//			Thread.sleep(500);
//			scm.unregisterPortMonitorListener(handle);
//			
//			scm.closeComPort(handle);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
