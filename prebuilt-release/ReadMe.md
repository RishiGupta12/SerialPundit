-- Latest development release is scm-1.0.4-dev.jar (linux/windows/mac os x, please USE this instead of 1.0.3 as there are large updates included in 1.0.4).

-- Latest stable release is scm-1.0.3.jar (linux/windows/mac os x).

##Platforms supported

| Operating System   | Architecture  | Versions  | Comments(requirements) |
| :------------:     |:-------------:| :--------:| :--------:|
| Linux      | amd64 | 2.6 kernel or later | eglibc 2.15, libpthread.so.0, libudev.so.0 |
| Linux      | x86   | 2.6 kernel or later | eglibc 2.15, libpthread.so.0, libudev.so.0 |
| Windows    | amd86 | Windows 7 or later | |
| Windows    | x86   | Windows 7 or later | |
| Mac OS X   | amd64 | 10.4 or later | |
| Mac OS X   | x86   | 10.4 kernel or later | |

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

