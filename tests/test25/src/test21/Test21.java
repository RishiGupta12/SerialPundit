package test21;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

public class Test21 {
	public static void main(String[] args) {
		
		SerialComManager scm = new SerialComManager();
		
		try {
			long handle = scm.openComPort("/dev/ttyUSB0", true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.BCUSTOM, 250000);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, '$', '$', false, false);

			long handle1 = scm.openComPort("/dev/ttyUSB1", true, true, true);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.BCUSTOM, 250000);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, '$', '$', false, false);
			
			scm.writeString(handle, "testing", 0);
			Thread.sleep(100);
			String data = scm.readString(handle1);
			System.out.println("data read is : " + data);
			
			scm.closeComPort(handle);
			scm.closeComPort(handle1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}