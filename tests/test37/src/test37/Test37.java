package test37;

import java.io.File;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;
import com.embeddedunveiled.serial.SerialComManager.FILETXPROTO;


// Test .txt, .doc, .pdf, .jpg, .mp3 file transfer
public class Test37 {
	public static void main(String[] args) {
		
		SerialComManager scm = new SerialComManager();
		
		String PORT = null;
		String PORT1 = null;
		int osType = SerialComManager.getOSType();
		if(osType == SerialComManager.OS_LINUX) {
			PORT = "/dev/ttyUSB0";
			PORT1 = "/dev/ttyUSB1";
		}else if(osType == SerialComManager.OS_WINDOWS) {
			PORT = "COM51";
			PORT1 = "COM52";
		}else if(osType == SerialComManager.OS_MAC_OS_X) {
			PORT = "/dev/cu.usbserial-A70362A3";
			PORT1 = "/dev/cu.usbserial-A602RDCH";
		}else if(osType == SerialComManager.OS_SOLARIS) {
			PORT = null;
			PORT1 = null;
		}else{
		}
		
		try {
			long handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			
			scm.sendFile(handle, new File("/home/r/ws-host-uart/development/ftptest/a.txt"), FILETXPROTO.XMODEM);
			
//			Thread.sleep(2);
//			
//			scm.sendFile(handle, new File("/home/r/ws-host-uart/development/ftptest/a.pdf"), FILETXPROTO.XMODEM);
//			
//			Thread.sleep(2);
//			
//			scm.sendFile(handle, new File("/home/r/ws-host-uart/development/ftptest/a.jpg"), FILETXPROTO.XMODEM);
//			
//			Thread.sleep(2);
//			
//			scm.sendFile(handle, new File("/home/r/ws-host-uart/development/ftptest/a.doc"), FILETXPROTO.XMODEM);

			scm.closeComPort(handle);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
