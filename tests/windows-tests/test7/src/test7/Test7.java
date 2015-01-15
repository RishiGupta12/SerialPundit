package test7;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

public class Test7 {
	public static void main(String[] args) {
		
		long handle = 0;
		long handle1 = 0;
		SerialComManager scm = new SerialComManager();
		
		try {
			handle = scm.openComPort("COM51", true, true, false);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			
			handle1 = scm.openComPort("COM52", true, true, false);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
			
			scm.clearPortIOBuffers(handle1, true, true);
			
			byte[] arr = new byte[10];
			for(int x=0; x<10; x++) {
				arr[x] = (byte) 202;
			}
			
			scm.writeBytes(handle, arr, 0);
			Thread.sleep(200);
			
			// before
			int[] before1 = scm.getByteCountInPortIOBuffer(handle1);
			System.out.println("BEFORE1 :: input : " + before1[0] + " output : " + before1[1]);
			
			// after
			scm.clearPortIOBuffers(handle1, true, true);
			int[] after1 = scm.getByteCountInPortIOBuffer(handle1);
			System.out.println("AFTER1 :: input : " + after1[0] + " output : " + after1[1]);
			
			scm.closeComPort(handle);
			scm.closeComPort(handle1);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}