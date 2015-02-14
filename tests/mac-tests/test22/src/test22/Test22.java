package test22;

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

class EventListener extends Test22 implements ISerialComEventListener {
	@Override
	public void onNewSerialEvent(SerialComLineEvent lineEvent) {
		System.out.println("eventCTS : " + lineEvent.getCTS());
		senddata = false;
	}
}

public class Test22 {
	
	static boolean senddata = true;
	
	public static void main(String[] args) {
		
		SerialComManager scm = new SerialComManager();
		Data1 DTE1 = new Data1();
		Data0 DCE1 = new Data0();
		EventListener eventListener = new EventListener();
		
		try {
			// DTE terminal
			long DTE = scm.openComPort("/dev/ttyUSB0", true, true, false);
			scm.configureComPortData(DTE, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
			scm.configureComPortControl(DTE, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
			scm.registerDataListener(DTE, DTE1);
			scm.setRTS(DTE, true);

			Thread.sleep(100000);
			
			// DCE terminal
			long DCE = scm.openComPort("/dev/ttyUSB1", true, true, false);
			scm.configureComPortData(DCE, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B9600, 0);
			scm.configureComPortControl(DCE, FLOWCONTROL.HARDWARE, 'x', 'x', false, false);
			scm.registerDataListener(DCE, DCE1);
			scm.registerLineEventListener(DCE, eventListener);
			
			scm.setDTR(DTE, true);
			scm.setDTR(DCE, true);
			Thread.sleep(100);
			scm.setRTS(DTE, true);
			scm.setRTS(DCE, true);
			Thread.sleep(100000);
			
			// Step 1
			scm.writeString(DTE, "str1", 0);
			Thread.sleep(100);
			scm.writeString(DTE, "str1", 0);
			Thread.sleep(100);
			scm.writeString(DCE, "str2", 0);
			Thread.sleep(100);
			scm.writeString(DCE, "str2", 0);
			Thread.sleep(100);
			
			// Step 2 dte says to dce dont send data i m full
			scm.setRTS(DTE, false);
			Thread.sleep(1000); // give delay so that senddata gets updated
			
			// Step 3 dce will receive event CTS and will sto sending data.
			if(senddata == true) {
				scm.writeString(DCE, "str3", 0);
			}else {
				System.out.println("seems like DTE is full");
			}
			
			Thread.sleep(1000);
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