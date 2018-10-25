package arbutus.virtuino.connectors.serial;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import arbutus.test.ToolBox;
import arbutus.virtuino.connectors.VirtuinoConnector;
import arbutus.virtuino.connectors.VirtuinoConnectorException;

public class MultipleSerialVirtuinoConnectorTest {
	
	private static VirtuinoConnector engineConnector = null;
	private static Thread engineConnectorThread = null;
	
	private static VirtuinoConnector arduinoConnector = null;
	private static Thread arduinoConnectorThread = null;
	
	@BeforeClass
	public static void setUp() throws VirtuinoConnectorException {
//		engineConnector = new SerialVirtuinoConnector(new SerialVirtuinoContext(3000, "COM5", 1000, SerialBaud.BAUDRATE_4800, SerialDataBits.DATABITS_8, SerialStopBits.STOPBITS_1, SerialParity.PARITY_NONE));
//		engineConnectorThread = start(engineConnector);
//		
		arduinoConnector = new SerialVirtuinoConnector(new SerialVirtuinoContext(3000, "COM10", 1000, SerialBaud.BAUDRATE_38400, SerialDataBits.DATABITS_8, SerialStopBits.STOPBITS_1, SerialParity.PARITY_NONE));
		arduinoConnectorThread = start(arduinoConnector);
	}
	
	public static Thread start(VirtuinoConnector connector) throws VirtuinoConnectorException {
		Thread connectorThread = new Thread(connector);
		
		connectorThread.start();
		
		// The arduino Connector won't be running right after the start, so let's wait a maximum of 20sec ...
		int nbSec = 0;
		while(!connector.isReady() && nbSec++ < 20)
			ToolBox.wait(1);
		
		if(!connector.isReady()) {
			throw new VirtuinoConnectorException("The connector did not get ready within 20 sec...");
		}
		
		return connectorThread;
	}
	
	@AfterClass
	public static void tearDown() throws VirtuinoConnectorException {
		stop(arduinoConnector, arduinoConnectorThread);
		//stop(engineConnector, engineConnectorThread);
	}
	
	public static void stop(VirtuinoConnector connector, Thread connectorThread) throws VirtuinoConnectorException {
		connector.interrupt();
		
		int nbSec = 0;
		while(nbSec++ < 10 && connectorThread.isAlive()) {
			ToolBox.wait(1);
		}
		
		if(connectorThread.isAlive()) {
			throw new VirtuinoConnectorException("The thread is still alive.");
		}
	}
	
	@Test
	@Ignore
	public void GetSyncVirtualFloat_From_2_Connectors_Should_Returns_The_Correct_Value() throws VirtuinoConnectorException {
		// Arrange
		
		// Act
		float age = engineConnector.getSyncVirtualFloat(0);
		float rpm = engineConnector.getSyncVirtualFloat(1);
		float conso = engineConnector.getSyncVirtualFloat(2);
		float qteDiesel = engineConnector.getSyncVirtualFloat(3);
		float temp = engineConnector.getSyncVirtualFloat(4);
		float voltage = engineConnector.getSyncVirtualFloat(5);
		float tempCoolant = engineConnector.getSyncVirtualFloat(6);
		float engineFirmwareVersion = engineConnector.getSyncFirmwareCode();
		
		ToolBox.wait(3);
		
		float arduinoFirmwareVersion = arduinoConnector.getSyncFirmwareCode();
		float arduinoCounter = arduinoConnector.getSyncVirtualFloat(0);
		
		// Assert
		assertEquals("Because we wait few seconds the firmwareversion should be 1.5", 1.5, engineFirmwareVersion, 0.001);
		assertThat("Because the engine is not new, the age is not equal to zero.", age, is(greaterThanOrEqualTo(2000f)));
		assertTrue("Because the engine is on, the rpm is not equal to zero.", rpm > 0f);
		assertTrue("Because the engine is on, the consumption of diesel is not equal to zero.", conso > 0f);
		assertTrue("Because we still have some diesel in our tank, the quantity of diesel is not equal to zero.", qteDiesel > 0);
		assertTrue("Because the engine is hot, the temp is not equal to zero.", temp > 0);
		assertTrue("Because the batterie is charged, the voltage is not equal to zero.", voltage > 0);
		assertTrue("Because the engine is hot, the tempCoolant is not equal to zero.", tempCoolant > 0);
		
		assertEquals("Because we wait few seconds the firmwareversion should be 1.5", 1.5, arduinoFirmwareVersion, 0.001);
		assertTrue("Because we wait few seconds the arduino counter should return a counter diff of 0", arduinoCounter > 0);
	}
	
	@Test
	@Ignore
	public void ReadDigitalPin_ShouldDisplayON_OFF_Because_We_areMoving_This_Test_Allow_To_Test_Disconnection_Reconnection() {
		while(true) {
			try {
				System.out.println(arduinoConnector.readSyncDigitalPin(12));
			}
			catch(VirtuinoConnectorException ex) {
				
			}
		}
		//assertTrue(true);
	}

}
