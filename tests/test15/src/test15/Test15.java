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
		
		String PORT = null;
		String PORT1 = null;
		int osType = SerialComManager.getOSType();
		if(osType == SerialComManager.OS_LINUX) {
			PORT = "/dev/ttyUSB0";
			PORT1 = "/dev/ttyUSB1";
		}else if(osType == SerialComManager.OS_WINDOWS) {
			PORT = "COM51";
			PORT = "COM52";
		}else if(osType == SerialComManager.OS_MAC_OS_X) {
			PORT = "/dev/cu.usbserial-A70362A3";
			PORT = "/dev/cu.usbserial-A602RDCH";
		}else if(osType == SerialComManager.OS_SOLARIS) {
			PORT = null;
			PORT1 = null;
		}else{
		}
		
		// instantiate class which is will implement ISerialComEventListener interface
		EventListener eventListener = new EventListener();
		
		try {
			long handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
			scm.registerLineEventListener(handle, eventListener);
			
			long handle1 = scm.openComPort(PORT1, true, true, true);
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
