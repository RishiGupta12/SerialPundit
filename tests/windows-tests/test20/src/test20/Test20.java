package test20;

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
		System.out.println("Sender got from receiver : " + new String(data.getDataBytes()));
	}
}

class Data1 implements ISerialComDataListener{
	@Override
	public void onNewSerialDataAvailable(SerialComDataEvent data) {
		System.out.println("Receiver got from sender : " + new String(data.getDataBytes()));
	}
}

public class Test20 {
	public static void main(String[] args) {
		
		byte[] XON  = new byte[] {(byte) 0x24};   // ASCII value of $ is 0x24
		byte[] XOFF = new byte[] {(byte) 0x23};   // ASCII value of # is 0x23
		
		SerialComManager scm = new SerialComManager();
		Data1 receiver = new Data1();
		Data0 sender = new Data0();
		
		try {
			// open and configure port that will listen data
			long receiverHandle = scm.openComPort("COM51", true, true, true);
			scm.configureComPortData(receiverHandle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(receiverHandle, FLOWCONTROL.SOFTWARE, '$', '#', false, false);

			scm.registerDataListener(receiverHandle, receiver);
			
			// open and configure port which will send data
			long senderHandle = scm.openComPort("COM52", true, true, true);
			scm.configureComPortData(senderHandle, DATABITS.DB_8, STOPBITS.SB_1, PARITY.P_NONE, BAUDRATE.B115200, 0);
			scm.configureComPortControl(senderHandle, FLOWCONTROL.SOFTWARE, '$', '#', false, false);
			scm.registerDataListener(senderHandle, sender);
			
			// Step 1
			scm.writeString(senderHandle, "str1", 0);
			scm.writeString(receiverHandle, "str2", 0);
			Thread.sleep(1000);
			
			// Step 2
			scm.writeBytes(receiverHandle, XOFF, 0);
			Thread.sleep(200);
			
			// Step 3
			scm.writeString(senderHandle, "str3", 0);
			
			// Step 4
			Thread.sleep(2000);
			
			// Step 5
			scm.writeBytes(receiverHandle, XON, 0);
			Thread.sleep(1000);
			
			scm.unregisterDataListener(sender);
			scm.unregisterDataListener(receiver);
			scm.closeComPort(receiverHandle);
			scm.closeComPort(senderHandle);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}