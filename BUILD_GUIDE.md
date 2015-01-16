##Part 1

Part 1 covers building and exporting java and native library when generating JNI-style header file is not required.

####Common steps
---

1. Get the project by either cloning repository or downloading it.

   Clone repository :
   ```sh
   $ git clone https://github.com/RishiGupta12/serial-com-manager.git
   ```
   Download project :
   
   Click 'Download Zip' button on this page https://github.com/RishiGupta12/serial-com-manager

2. Install eclipse IDE for java developers from here https://eclipse.org/downloads.
   Select 32 bit or 64 bit as per your system and OS.

####Building java library (.jar file)
---

1. Start eclipse selecting workspace folder as per your choice.

2. Import this java project into workspace.
   File->Import->General->Existing Projects Into Workspace->Select root directory:
   Then browse to the location where this project exist and select com.embeddedunveiled.serial folder.
   
3. Build the project.
   Select project folder and then from menu, Project->Build All.
   This will create class files in bin folder.
   
4. Export jar file that will be imported by applications using this library.
   Select project folder and then right click on it.
   Export->Java->JAR File, then click 'Next>' button.
   Select the export destination which is the place where this jar will be placed on file system.
   Click Browse and give name for example scm and click Finish button.

####Building native library (.so files) for linux
---

1. Install CDT plugin from here https://eclipse.org/cdt/downloads.php
   Help->Install New Software
   
   Then in "Work with" field, pull down the drop-down menu and select "Kepler - http://download.eclipse.org/releases/kepler" (or juno for Eclipse 4.2; or helios for Eclipse 3.7).
   
   In "Name" box, expand "Programming Language" node and Check "C/C++ Development Tools" -> "Next"-> "Next".
   Accept the license agreement and then click "Finish" button.
   
   Let the installation finish and it will ask to restart eclipse. Restart eclipse so CDT become active.
   
   No toolchain specific configuration is need. CDT searches the PATH to discover the C/C++ compilers.
   Linux GCC toolchain has been used to build library.

2. Import this C project into workspace.
   File->Import->General->Existing Projects Into Workspace->Select root directory:
   Then browse to the location where this project exist and select unix-like folder.
   
3. Locate jni.h header file(s) on your system.
   Select project and then right click on it.
   Properties->C/C+ Build->Settings->Tool Settings->GCC C Compiler->Includes
   Now select Includes paths and locate include folder where jdk is installed on your system.
   Similarly, locate and select jni.h file under Include files.
   
4. Specify building shared library by passing -fPIC flag.
   Select project and then right click on it.
   Properties->C/C+ Build->Settings->Tool Settings->Miscellaneous and then check (-fPIC) flag.
   
5. The java library at runtime loads 32 or 64 bit native library as per system, JRE and OS in use.
   The name of native library is formulated as a combination of OS, library version and bit(32/64).
   For example "linux_1.0.0_x86_64.so" for 64 bit and "linux_1.0.0_x86.so" for 32 bit
   
   Select project, right click and then select Properties. Under C/C++ option select Settings and
   then Build Artifact. 
  - Enter Artifact name as described above without extension.
  - Enter Artifact extension as "so".
   
6. Define building 32 bit or 64 bit version library to compiler.
   Select project and then right click on it.
   Properties->C/C+ Build->Settings->Tool Settings->GCC C Compiler->Miscellaneous->Other flags
   Write -m32 for 32 bit library and -m64 for 64 bit library.
   Click Apply button.
   
   Note that for 32 bit libraries must be installed on your system if your system is 64 bit.
   $ sudo apt-get install gcc-multilib
   $ sudo apt-get install ia32-libs
   
7. Define linking against 32 bit or 64 bit version library to linker.
   Select project and then right click on it.
   Properties->C/C+ Build->Settings->Tool Settings->GCC C Linker->Miscellaneous->Linker flags
   Write -m32 for 32 bit library and -m64 for 64 bit library.
   Click Apply button.

8. Set scalability parameter for eclipse IDE.
   Window->Preferences->C/C++->Editor->Scalability->"Enable scalability mode when ..." set high value for example 60000.

   Project-> C/C++ Index->Freshen All Files and Rebuild
   
9. Generate shared library file.
   Select project and then Project->Build All
   This will create '.so' file for example "linux_1.0.0_x86_64.so" in Debug folder.
   
   Copy this file into *'libs'* folder of com.embeddedunveiled.serial eclipse java project.
   
####Building native library (.dll files) for windows
---

1. Install Visual Studio Express 2013 with Update 4 for *Windows Desktop* from here
   http://www.visualstudio.com/downloads/download-visual-studio-vs
   
   We used online installer as offline iso file does not contain many packages required.
   
2. Start visual studio and then click on Open Project option.
   Navigate to the file windows_serial.sln file (basically VS project) and select it.
  
   Press ok button if security dialog pop up. If you cancel it, then reload project.
   http://msdn.microsoft.com/en-us/library/tt479x1t%28v=vs.90%29.aspx
  

2. Locate jni.h header file(s) on your system. 
   Right click on windows_serial project and then select Properties.
   Configuration Properties->C/C++->General and click on Additional Include Directories.
   
   Click on the drop down arrow and then select <edit>. Locate the path to include directory for example as showwn below:
   
   C:\Program Files\Java\jdk1.8.0_25\include
   C:\Program Files\Java\jdk1.8.0_25\include\win32
   
3. Define 32 bit or 64 bit build to visual studio.

   Right click on windows_serial project and then select Properties.
   Select Configuration Properties and then from drop down menu select Win32 for 32 bit build or x64 for 64 bit build.
   
   This can also be done via Configuration manager : 
   http://msdn.microsoft.com/en-us/library/kwybya3w(v=vs.90).aspx
   
   
4. The java library at runtime loads 32 or 64 bit native library as per system, JRE and OS in use. The name of native library is formulated as a combination of OS, library version and bit(32/64). For example "windows_1.0.0_x86_64.dll" for 64 bit and "windows_1.0.0_x86.so" for 32 bit.

   Note this step should be done together when performing step 3 i.e. setting Active platform.
   
   Right click on windows_serial project and then select Properties.
   Configuration Properties->General and select Target Name option.
   
   From drop down <edit> and enter windows_1.0.0_x86_64 for 64 bit and windows_1.0.0_x86 for 32 bit.
   
##Part 2

Part 2 cover steps required to build and export java and native library if one or more *source file has been modified* in such a way that it becomes necessary to generate new JNI-STYLE header file.

####Generate header file
---
