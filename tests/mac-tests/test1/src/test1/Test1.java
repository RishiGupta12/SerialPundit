package test1;

import com.embeddedunveiled.serial.SerialComManager;

public class Test1 {
	public static void main(String[] args) {
		SerialComManager scm = new SerialComManager();
		String[] ports = scm.listAvailableComPorts();
		for(String port: ports){
			System.out.println(port);
		}
	}
}