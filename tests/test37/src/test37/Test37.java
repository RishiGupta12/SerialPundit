package test37;

import java.io.File;

import com.embeddedunveiled.serial.SerialComException;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;
import com.embeddedunveiled.serial.SerialComManager.FILETXPROTO;

public class Test37 {
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
		
		try {
			long handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B2400, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			
			scm.sendFile(handle, new File("/home/r/mtd"), FILETXPROTO.XMODEM);

			scm.closeComPort(handle);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
