package test21;

import com.embeddedunveiled.serial.IPortMonitor;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

// event 2 indicates port removal, 1 indicates additional of port
class portWatcher implements IPortMonitor{
	@Override
	public void onPortMonitorEvent(int event) {
		System.out.println("==" + event);
	}
}

public class Test21 {
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
		
		portWatcher pw = new portWatcher();
		
		try {
			long handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, '$', '$', false, false);
			scm.registerPortMonitorListener(handle, pw);
			
			Thread.sleep(50000);
			
			scm.unregisterPortMonitorListener(handle);
			scm.closeComPort(handle);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}