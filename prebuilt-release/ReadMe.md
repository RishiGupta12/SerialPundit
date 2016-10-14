##Latest releases
Last updated on 14-Oct-2016

| Jar file       | Functions/Features                    | Comments     |
| :------------: |:-------------:                        | :--------:   |
| sp-core.jar    | Platform identification, utility etc. | Must include |
| sp-usb.jar     | USB hotplug, dynamic devnode etc.     | Can be used for both tty and HID |
| sp-tty.jar     | Serial port communication APIs        |              |
| sp-hid.jar     | HID communication APIs                |              |

##Platforms supported

| Operating System   | Architecture  |  Versions    | Comments |
| :------------:     |:-------------:| :--------:   | :--------:|
| Linux              | amd64 | 3.0 kernel or later  | eglibc 2.15 or later, libpthread.so.0, libudev.so.0 |
| Linux              | x86   | 3.0 kernel or later  | Intel Edision, eglibc 2.15, libpthread.so.0, libudev.so.0 |
| Windows            | amd86 | Windows 7 or later   |         |
| Windows            | x86   | Windows 7 or later   |         |
| Mac OS X           | amd64 | 10.4 or later        |         |
| Mac OS X           | x86   | 10.4 kernel or later |         |
| Embedded Linux     | ARMv7 | 3.0 kernel or later  | libudev.so.1, libpthread.so.0, libc.so.6, hard/soft float ABI, Raspberry Pi, BeagleBone, Wandboard, Cubieboard etc. |
| Embedded Linux     | ARMv6 | 3.0 kernel or later  | libudev.so.1, libpthread.so.0, libc.so.6, hard/soft float ABI, Raspberry Pi, BeagleBone etc. |

##Integration information

#####Maven
```
<dependency>
	<groupId>com.embeddedunveiled</groupId>
	<artifactId>scm</artifactId>
	<version>1.0.3</version>
</dependency>
```

#####IVY
```
<dependency org="com.embeddedunveiled" name="scm" rev="1.0.3"/>
```

#####Grape
```
@Grapes(
	@Grab(group='com.embeddedunveiled', module='scm', version='1.0.3')
)
```

#####Gradle
```
'com.embeddedunveiled:scm:1.0.3'
```

#####Buildr
```
'com.embeddedunveiled:scm:jar:1.0.3'
```

#####SBT
```
libraryDependencies += "com.embeddedunveiled" % "scm" % "1.0.3"
```

#####Leiningen
```
[com.embeddedunveiled/scm "1.0.3"]
```

##Signature verification

For Linux, change directory to where sp-tty.jar file is placed on your system and check as follows :
```
$ gpg --verify sp-tty.jar.asc sp-tty.jar
gpg: Signature made Friday 29 May 2015 11:28:11 AM IST using RSA key ID 2B942F12
gpg: Good signature from "rishigupta (serialpundit) <xxxx@gmail.com>"
```

