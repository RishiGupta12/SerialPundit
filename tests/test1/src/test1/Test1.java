package test1;

import com.embeddedunveiled.serial.SerialComManager;

/*
 * connect bluetooth dongle, 3G dongle, USB-UART converter and all of them should be 
 * detected by this library apart from regular ports.
 */
public class Test1 {
	public static void main(String[] args) {
		SerialComManager scm = new SerialComManager();
		String[] ports = scm.listAvailableComPorts();
		for(String port: ports){
			System.out.println(port);
		}
	}
}
