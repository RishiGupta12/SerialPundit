## Concurrently collecting data from source

- **Use case 1:** 100 temperature sensors are placed at strategic locations in an industrial plant and central control room need to read temperature from each of them periodically.

- **Use case 2:** 100 trucks need to be tracked and their location need to logged into database as needed by a logistics company.

Very fast concurrent reading strategy may be needed when state-of-art hardware with baudrates in mega-bits per second range is used or software null modem emulator or TCP/IP com redirectors are used or very fast logging may be application requirement. 

This document gives ideas that may be used in application's implementation to address use cases like above.

#### Polled read using executors and thread poll

Polling every serial port using single thread will reduce overall throughput. Using thread for every serial port may cause resource starvation or degraded performance. So we use the Executor framework which decouples task submission from task execution. The design is divided into following parts:

- *Module 1* : Establishes internet connection with remote vehicle/sensor. Opens and configure the serial port corresponding to it. This will give serial port handle (comPortHandle) for every vehicle to be tracked.
  
  For example assume the VTS board (vehicle tracking system) contains a GPS receiver. Firmware in microcontroller collects NMEA data from receiver, parses it and creates a final buffer that will be passed to application server. It has a 3G enabled SIM through which a COM-to-TCP/IP redirector sends data to application server (our application).

- *Module 2* : It gets the serial port handle from module 1 and schedule the task (reading) at fixed rate (here 500 milliseconds) for every serial port i.e. vehicle/sensor.
  ```Java
  Runnable task = new Task(scm, comPortHandle);
  scheduledExecutor.scheduleAtFixedRate(task, 0, 500, TimeUnit.MILLISECONDS);
  ```

- *Module 3* : Read data from given serial port and writes it to database.
  ```Java
  final class Task implements Runnable {
	
	private final long comPortHandle;
	private final SerialComManager scm;
	
	public Task(SerialComManager scm, long comPortHandle) {
		this.comPortHandle = comPortHandle;
		this.scm = scm;
	}
	
	@Override
	public void run() {
		try {
			byte[] dataRead = scm.readBytes(comPortHandle);
			if(dataRead != null) {
				// Logic to insert into database table goes here.
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
  }
  ```

