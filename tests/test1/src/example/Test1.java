package example;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;

public class Test1 {
	public static void main(String[] args) {
	
		long handle = 0;
		
		// get serial communication manager instance
		SerialComManager sc = new SerialComManager();
		
		try {
			// try opening serial port for read and write without exclusive ownership
			handle = sc.openComPort("/dev/ttyUSB1", true, true, false);
			
			// configure data communication related parameters
			sc.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			
			// configure line control related parameters
			sc.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			
			// try to send data out of serial port
			if(sc.writeString(handle, "test string \n", 0) == true) {
				System.out.println("write success \n");
			}
		
			// try to read data from serial port
			String data = sc.readString(handle);
			System.out.println("data read is :" + data);

			// close serial port
			sc.closeComPort(handle);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}