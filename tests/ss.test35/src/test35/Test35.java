package test35;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

/* 
 * create two instances of scm from completely separate applications and open two different ports from them.
 * run this test in conjunction with ss.test35
 */
public class Test35 {
	public static void main(String[] args) {
		
		SerialComManager scm = new SerialComManager();
		
		String PORT = null;
		int osType = SerialComManager.getOSType();
		if(osType == SerialComManager.OS_LINUX) {
			PORT = "/dev/ttyUSB1";
		}else if(osType == SerialComManager.OS_WINDOWS) {
			PORT = "COM52";
		}else if(osType == SerialComManager.OS_MAC_OS_X) {
			PORT = "/dev/cu.usbserial-A602RDCH";
		}else if(osType == SerialComManager.OS_SOLARIS) {
			PORT = null;
		}else{
		}
		
		long handle = 0;
		try {	
			handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			scm.getLibraryVersions();
//			Thread.sleep(3000);
			scm.writeString(handle, "testing", 0);
			Thread.sleep(3000);
			System.out.println(" : " + scm.readString(handle));
			while(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}



