package test21;

import com.embeddedunveiled.serial.IPortMonitor;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

// event 2 indicates port removal, 1 indicates additional of port
class portWatcher implements IPortMonitor {
	@Override
	public void onPortMonitorEvent(int event) {
		System.out.println("==" + event);
	}
}

public class Test21 {
	public static void main(String[] args) {
		
		SerialComManager scm = new SerialComManager();
		portWatcher pw = new portWatcher();
		
		try {
			long handle = scm.openComPort("COM52", true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, '$', '$', false, false);
			scm.registerPortMonitorListener(handle, pw);
			
			Thread.sleep(5000);
			
			scm.unregisterPortMonitorListener(handle);
			scm.closeComPort(handle);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}