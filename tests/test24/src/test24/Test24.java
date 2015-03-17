package test24;

import com.embeddedunveiled.serial.ISerialComEventListener;
import com.embeddedunveiled.serial.SerialComLineEvent;
import com.embeddedunveiled.serial.SerialComManager;
import com.embeddedunveiled.serial.SerialComManager.BAUDRATE;
import com.embeddedunveiled.serial.SerialComManager.DATABITS;
import com.embeddedunveiled.serial.SerialComManager.FLOWCONTROL;
import com.embeddedunveiled.serial.SerialComManager.PARITY;
import com.embeddedunveiled.serial.SerialComManager.STOPBITS;
import com.embeddedunveiled.serial.ISerialComDataListener;
import com.embeddedunveiled.serial.SerialComDataEvent;

class Data0 implements ISerialComDataListener{
	@Override
	public void onNewSerialDataAvailable(SerialComDataEvent data) {
		System.out.println("DCE GOT FROM DTE : " + new String(data.getDataBytes()));
	}
}

class Data1 implements ISerialComDataListener{
	@Override
	public void onNewSerialDataAvailable(SerialComDataEvent data) {
		System.out.println("DTE GOT FROM DCE : " + new String(data.getDataBytes()));
	}
}

class EventListener implements ISerialComEventListener {
	@Override
	public void onNewSerialEvent(SerialComLineEvent lineEvent) {
		System.out.println("eventCTS : " + lineEvent.getCTS());
	}
}

public class Test24 {
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
		
		Data1 DTE1 = new Data1();
		Data0 DCE1 = new Data0();
		EventListener eventListener = new EventListener();
		
		try {
			// DTE terminal
			long DTE = scm.openComPort(PORT, true, true, true);
			scm.configureComPortData(DTE, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
			scm.configureComPortControl(DTE, FLOWCONTROL.NONE, 'x', 'x', false, false);
			scm.registerDataListener(DTE, DTE1);
			
			// DCE terminal
			long DCE = scm.openComPort(PORT1, true, true, true);
			scm.configureComPortData(DCE, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
			scm.configureComPortControl(DCE, FLOWCONTROL.NONE, 'x', 'x', false, false);
			scm.registerDataListener(DCE, DCE1);
			scm.registerLineEventListener(DCE, eventListener);
			
			// Step 1
			scm.writeString(DTE, "str1", 0);
			Thread.sleep(100);
			scm.writeString(DTE, "str1", 0);
			Thread.sleep(100);
			scm.writeString(DCE, "str2", 0);
			Thread.sleep(100);
			scm.writeString(DCE, "str2", 0);
			Thread.sleep(100);
			
			Thread.sleep(2000);
			scm.unregisterDataListener(DTE1);
			scm.unregisterDataListener(DCE1);
			scm.unregisterLineEventListener(eventListener);
			scm.closeComPort(DTE);
			scm.closeComPort(DCE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}