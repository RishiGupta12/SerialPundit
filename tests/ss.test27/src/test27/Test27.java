package test27;

import com.embeddedunveiled.serial.ISerialComDataListener;
import com.embeddedunveiled.serial.SerialComDataEvent;
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

class DataListener implements ISerialComDataListener{
	@Override
	public void onNewSerialDataAvailable(SerialComDataEvent data) {
		System.out.println("Read from serial port : " + new String(data.getDataBytes()) + "\n");
	}
}

/* register/unregister data/event listener with data/event many times */
public class Test27 {
	public static void main(String[] args) {
		
		SerialComManager scm = new SerialComManager();
		EventListener eventListener = new EventListener();
		DataListener dataListener = new DataListener();
		
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
		
		try {
			long handle = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(handle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
			scm.configureComPortControl(handle, FLOWCONTROL.NONE, 'x', 'x', false, false);
			
			long handle1 = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(handle1, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
			scm.configureComPortControl(handle1, FLOWCONTROL.NONE, 'x', 'x', false, false);
			int x = 0;
			
			for(x=0; x<5000; x++) {
				System.out.println("register  : " + scm.registerLineEventListener(handle, eventListener));
				scm.setDTR(handle1, true);
				scm.setRTS(handle1, true);
				scm.setRTS(handle1, false);
				scm.setDTR(handle1, false);
				Thread.sleep(10);
				System.out.println("unregister : " + scm.unregisterLineEventListener(eventListener));
			}
			System.out.println("test event pass \n");
			
			for(x=0; x<5000; x++) {
				System.out.println("register  : " + scm.registerDataListener(handle, dataListener));
				scm.writeString(handle1, "T", 0);
				Thread.sleep(50);
				System.out.println("unregister : " + scm.unregisterDataListener(dataListener));
			}
			System.out.println("test data pass \n");

			scm.closeComPort(handle);
			scm.closeComPort(handle1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
