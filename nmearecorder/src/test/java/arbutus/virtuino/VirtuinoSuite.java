package arbutus.virtuino;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import arbutus.virtuino.connectors.VirtuinoConnectorTest;
import arbutus.virtuino.connectors.serial.SerialVirtuinoConnectorTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	SerialVirtuinoConnectorTest.class,
	VirtuinoConnectorTest.class
})

public class VirtuinoSuite {

}
