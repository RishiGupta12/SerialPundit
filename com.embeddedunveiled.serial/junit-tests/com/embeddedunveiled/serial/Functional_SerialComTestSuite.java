package com.embeddedunveiled.serial;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/* Functional testing of various APIs. */
@RunWith(Suite.class)
@SuiteClasses({ 
	SerialComManagerTests.class,
	SerialComByteStreamTests.class,
	SerialComUtilTests.class,
	SerialComCRCUtilTests.class
})

public class Functional_SerialComTestSuite {
}
