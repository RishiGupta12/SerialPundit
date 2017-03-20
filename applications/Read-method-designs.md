## Read methods designs
Give brief ideas about how to read data using serialpundit depending upon the nature of data (fixed length, delimited, timeout or burst etc). These ideas can also be used with non-blocking or blocking variant of read method according to use case.

- **Fixed length data**; length of data packet (number of bytes) is fixed and is known before hand.
```java
  int x = 0;
  int index = 0;
  int totalNumberOfBytesReadTillNow = 0;
  byte[] data = null;
  byte[] dataBuffer = new byte[15];
  while(totalNumberOfBytesReadTillNow < 15) {
      data = scm.readBytes(handle);
      if(data != null) {
          for(x=0; x < data.length; x++) {
              dataBuffer[index] = data[x];
              index++;
          }
          totalNumberOfBytesReadTillNow = totalNumberOfBytesReadTillNow + data.length;
      }
  }
```
Or
```java
  int x = 0;
  int offset = 0;
  int totalNumberOfBytesReadTillNow = 0;
  byte[] dataBuffer = new byte[128];
  while(totalNumberOfBytesReadTillNow < 15) {
      x = 0;
      x = scm.readBytes(handle1, dataBuffer, offset, 15, -1);
      if(x > 0) {
          totalNumberOfBytesReadTillNow = totalNumberOfBytesReadTillNow + x;
          offset = offset + x;
      }
  }
```
- **Delimited data**; data packet (fixed/variable length) will be delimited by well known character like CR, LF, Special char etc.
```java
  boolean exit = false;
  int x = 0;
  int index = 0;
  byte[] data = null;
  byte[] dataBuffer = new byte[100];
  while(exit == false) {
      data = scm.readBytes(handle);
      if(data != null) {
          for(x=0; x < data.length; x++) {
              if (data[x] != CR) {
                  dataBuffer[index] = data[x];
                  index++;
              }else {
                  exit = true;
                  break;
              }
          }
      }
  }
```
- **Timedout data**; consecutive data packets will always have a minimum time gap between them.
```java
  // Tune read method behaviour (500 milliseconds wait timeout value)
  scm.fineTuneReadBehaviour(handle, 0, 5, 100, 5, 200);

  // The readBytes will return only after 500 millisecond if there was no data to read
  data = scm.readBytes(handle);
  if(data != null) {
      System.out.println("Data read : " + new String(dataRead));
  }else {
      System.out.println("Timed out without reading data");
  }
```
- **Burst data**; data packets will arrive asynchronously in burst fashion.
```java
```

