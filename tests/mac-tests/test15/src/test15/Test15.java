package test15;

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

public class Test15 {
	public static void main(String[] args) {
		
		SerialComManager scm = new SerialComManager();
		
		// instantiate class which is will implement ISerialComEventListener interface
		EventListener eventListener = new EventListener();
		
		try {
			long handle = scm.openComPort("/dev/cu.usbserial-A602RDCH", true, true, false);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
			scm.registerLineEventListener(handle, eventListener);
			
			long handle1 = scm.openComPort("/dev/cu.usbserial-A70362A3", true, true, false);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
			
			// both event will be called
			Thread.sleep(1000);
			scm.setRTS(handle1, false);
			Thread.sleep(1000);
			scm.setDTR(handle1, false);
			
			// mask CTS, so only changes to CTS line will be reported.
			scm.setEventsMask(eventListener, SerialComManager.CTS);
			Thread.sleep(1000);
			scm.setRTS(handle1, true);
			Thread.sleep(1000);
			scm.setDTR(handle1, true);
			Thread.sleep(1000);
			scm.setRTS(handle1, false);
			Thread.sleep(1000);
			scm.setDTR(handle1, false);
			
			while(true);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
