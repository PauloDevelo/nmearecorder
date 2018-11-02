package arbutus.virtuino;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import arbutus.virtuino.connectors.VirtuinoConnectorTest;
import arbutus.virtuino.connectors.serial.SerialVirtuinoConnectorTest;
import arbutus.virtuino.service.VirtuinoServiceTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	SerialVirtuinoConnectorTest.class,
	VirtuinoConnectorTest.class,
	VirtuinoServiceTest.class
})

public class VirtuinoSuite {

}
