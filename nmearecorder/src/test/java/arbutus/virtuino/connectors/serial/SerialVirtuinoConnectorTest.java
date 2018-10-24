package arbutus.virtuino.connectors.serial;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import arbutus.test.ToolBox;
import arbutus.virtuino.connectors.VirtuinoConnector;
import arbutus.virtuino.connectors.VirtuinoConnectorException;
import arbutus.virtuino.connectors.serial.SerialBaud;
import arbutus.virtuino.connectors.serial.SerialDataBits;
import arbutus.virtuino.connectors.serial.SerialParity;
import arbutus.virtuino.connectors.serial.SerialStopBits;
import arbutus.virtuino.connectors.serial.SerialVirtuinoConnector;
import arbutus.virtuino.connectors.serial.SerialVirtuinoContext;

public class SerialVirtuinoConnectorTest {
	
	private static VirtuinoConnector connector = null;
	private static Thread connectorThread = null;
	
	@BeforeClass
	public static void setUp() throws VirtuinoConnectorException {
		connector = new SerialVirtuinoConnector(new SerialVirtuinoContext(3000, "COM5", 1000, SerialBaud.BAUDRATE_4800, SerialDataBits.DATABITS_8, SerialStopBits.STOPBITS_1, SerialParity.PARITY_NONE));
		connectorThread = new Thread(connector);
		
		connectorThread.start();
		
		// The connector won't be running right after the start, so let's wait a maximum of 10sec ...
		int nbSec = 0;
		while(!connector.isReady() && nbSec++ < 20)
			ToolBox.wait(1);
		
		if(!connector.isReady()) {
			throw new VirtuinoConnectorException("Connector did not get reay within 20 sec...");
		}
	}
	
	@AfterClass
	public static void tearDown() throws VirtuinoConnectorException {
		connector.interrupt();
		
		int nbSec = 0;
		while(nbSec++ < 10 && connectorThread.isAlive()) {
			ToolBox.wait(1);
		}
		
		if(connectorThread.isAlive()) {
			throw new VirtuinoConnectorException("The thread of the connector is still alive.");
		}
	}
	
	@Test(timeout=120000)
	@Ignore
	public void Run_With_Engine_OFF__It_Shoud_Not_Be_Ready_After_Few_Seconds() {
		// Arrange
		
		// Act
		
		// Assert
		assertFalse("Because the connector should not be ready when the engine is OFF.", connector.isReady());
		assertTrue("Because the thread of connector should running even if the connector is not ready.", SerialVirtuinoConnectorTest.connectorThread.isAlive());
	}
	
//	#define AGE_ENGINE_INDEX	0
//	#define RPM_INDEX			1
//	#define CONSO_INDEX			2
//	#define QTE_GAZ_INDEX		3
//	#define TEMP_INDEX			4
//	#define VOLTAGE_INDEX		5
//	#define TEMP_COOLANT_INDEX	6
	
	@Test(timeout=120000)
	public void GetEngineInfo_With_Engine_ON__It_Shoud_Return_A_No_Zero_Value() throws VirtuinoConnectorException {
		// Arrange
		
		// Act
		float age = connector.getSyncVirtualFloat(0);
		float rpm = connector.getSyncVirtualFloat(1);
		float conso = connector.getSyncVirtualFloat(2);
		float qteDiesel = connector.getSyncVirtualFloat(3);
		float temp = connector.getSyncVirtualFloat(4);
		float voltage = connector.getSyncVirtualFloat(5);
		float tempCoolant = connector.getSyncVirtualFloat(6);
		
		// Assert
		assertThat("Because the engine is not new, the age is not equal to zero.", age, is(greaterThanOrEqualTo(2000f)));
		assertTrue("Because the engine is on, the rpm is not equal to zero.", rpm > 0f);
		assertTrue("Because the engine is on, the consumption of diesel is not equal to zero.", conso > 0f);
		assertTrue("Because we still have some diesel in our tank, the quantity of diesel is not equal to zero.", qteDiesel > 0);
		assertTrue("Because the engine is hot, the temp is not equal to zero.", temp > 0);
		assertTrue("Because the batterie is charged, the voltage is not equal to zero.", voltage > 0);
		assertTrue("Because the engine is hot, the tempCoolant is not equal to zero.", tempCoolant > 0);
	}
	
	@Test(timeout=120000)
	public void GetFirmwareCode_With_Engine_ON__It_Shoud_Return_1_5_Value() throws VirtuinoConnectorException {
		// Arrange
		
		// Act
		float firmwareCode = connector.getSyncFirmwareCode();
		
		// Assert
		
		assertEquals("Because the firmware of the engine monitor is currently 1.5", 1.5f, firmwareCode, 0.0001);
	}
}
