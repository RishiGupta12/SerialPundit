package test28;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

/* 
 * create two instances of scm and open two different ports from them.
 */
public class Test28 {
	public static void main(String[] args) {
		
		SerialComManager scm = new SerialComManager();
		SerialComManager scm1 = new SerialComManager();
		
		String PORT = null;
		String PORT1 = null;
		int osType = SerialComManager.getOSType();
		if(osType == SerialComManager.OS_LINUX) {
			PORT = "/dev/ttyUSB0";
			PORT1 = "/dev/ttyUSB1";
		}else if(osType == SerialComManager.OS_WINDOWS) {
			PORT = "COM51";
		}else if(osType == SerialComManager.OS_MAC_OS_X) {
			PORT = "/dev/cu.usbserial-A70362A3";
		}else if(osType == SerialComManager.OS_SOLARIS) {
			PORT = null;
		}else{
		}
		
		long handle1 = 0;
		long handle = 0;
		try {	
			handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {	
			handle1 = scm1.openComPort(PORT1, true, true, true);
			scm1.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
			scm1.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			scm.writeString(handle, "testing4", 0);
			scm1.writeString(handle1, "testing3", 0);
			System.out.println("data read is : " + scm.readString(handle));
			System.out.println("data1 read is : " + scm1.readString(handle1));
			System.out.println("close status : " + scm.closeComPort(handle));
			System.out.println("close status : " + scm1.closeComPort(handle1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}



