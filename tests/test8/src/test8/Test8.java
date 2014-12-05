package test8;

import com.embeddedunveiled.serial.SerialComManager;

public class Test8 {
	public static void main(String[] args) {
		SerialComManager scm = new SerialComManager();
		String version = scm.getLibraryVersions();
		System.out.println(version);
	}
}
