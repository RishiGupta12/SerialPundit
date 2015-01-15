package test6;

import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;
import com.embeddedunveiled.serial.ISerialComEventListener;
import com.embeddedunveiled.serial.SerialComLineEvent;

class EventListener implements ISerialComEventListener{
	@Override
	public void onNewSerialEvent(SerialComLineEvent lineEvent) {
		System.out.println("eventCTS : " + lineEvent.getCTS());
		System.out.println("eventDSR : " + lineEvent.getDSR());
	}
}

public class Test6 {
	public static void main(String[] args) {
		
		long handle = 0;
		SerialComManager scm = new SerialComManager();
		
		// instantiate class which is will implement ISerialComEventListener interface
		EventListener eventListener = new EventListener();
		
		try {
			handle = scm.openComPort("COM51", true, true, false);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			scm.registerLineEventListener(handle, eventListener);
			
			long handle1 = scm.openComPort("COM52", true, true, false);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
			
			Thread.sleep(2000);
			scm.setDTR(handle1, true);
			Thread.sleep(2000);
			scm.setRTS(handle1, true);
			Thread.sleep(2000);
			scm.setDTR(handle1, false);
			Thread.sleep(2000);
			scm.setRTS(handle1, false);
			Thread.sleep(2000);
			scm.setDTR(handle1, true);
			Thread.sleep(2000);
			scm.setRTS(handle1, true);
			Thread.sleep(2000);

			// unregister data listener
			scm.unregisterLineEventListener(eventListener);
			
			// close the port releasing handle
			scm.closeComPort(handle);
			scm.closeComPort(handle1);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
