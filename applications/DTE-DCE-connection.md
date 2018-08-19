## DTE-DCE Connection
This application note gives a brief overview about how handshaking signals are used in serial port communication. Originally serial port handshaking was described from communication between DTE and DCE view. With the advancement of silicon technologies and more use cases, connecting a DTE to other DTE Instead of DCE has also become common.

<image width="625" heigth="170" src="https://github.com/RishiGupta12/SerialPundit/blob/master/images/dtedce.png">

* The DTE used to assert RTS signal which tells DCE that DTE wants to send some data. The DCE in response asserts CTS signal indicating that it is ready to receive data. The RTS and CTS signals therefore were used to control flow of data.
* DTE used to assert DTR signal to indicate DCE that it is present and ready for communication. The DCE used to assert DSR signal to indicate DTE that it is present and ready for communication. The DTR and DSR signals therefore were used to indicate presence.
* All signal names were viewed from DTE perspective when determining input and output flow.
* Traditionally, DTE end used a male type connector (DB9) and DCE used a female type connector (DB9).

Above mentioned way of working gets modified from time to time especially when DCE was no longer a slow device as compared to DTE, UART baudrate increased significantly and more applications where serial port can be used were identified.

* Now DTR signal can also be used to indicate to other end DCE/DTE that DCE/DTE can send data to DTE making DTR signal part of data flow control.
* In custom implementations of hardware and driver, sometimes RTS/CTS are used as GPIO to perform specific operation for ex; turning on and off a LED.
* Two ends may use same type of connector for ex; when two DTE are connected to each other (null modem) both will use DB9 male type connector.
* When connected in null modem fashion, each DTE asserts its RTS signal which is connected to CTS signal at other end. The end which wants to transmit data just check its CTS line to know whether it is allowed to transmit or not.

The operating system and drivers offer various APIs and configuration which allows us to control the behaviour DTE/DCE should exhibit. Whether we want to have legacy or custom behaviour, it is possible to implement it.

