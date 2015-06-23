import com.embeddedunveiled.serial.SerialComManager;
public class FindPorts {
 public static void main(String[] args) {
 	try {
		System.out.println("Executing FindPorts application");
		SerialComManager scm = new SerialComManager();
		String[] ports = scm.listAvailableComPorts();
		for(String port: ports){
			System.out.println(port);
		}
	}catch (Exception e) {
		e.printStackTrace();
	}
 }
} 
