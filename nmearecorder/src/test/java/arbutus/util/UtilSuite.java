package arbutus.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import arbutus.nmea.service.connectors.TCPReaderTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	TCPReaderTest.class,
	UtilsTest.class
})

public class UtilSuite {

}
