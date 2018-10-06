package arbutus.nmea.service;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import arbutus.nmea.service.connectors.TCPReaderTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	NMEAServiceTest.class,
	TCPReaderTest.class
})
public class NMEAServiceSuite {

}
