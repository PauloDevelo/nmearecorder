package arbutus.virtuino.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import arbutus.rtmodel.EngineMeasurementType;
import arbutus.rtmodel.VirtuinoServiceType;
import arbutus.service.ServiceState;
import arbutus.test.ToolBox;
import arbutus.virtuino.connectors.VirtuinoConnector;
import arbutus.virtuino.connectors.VirtuinoConnectorException;
import arbutus.virtuino.connectors.VirtuinoContext;
import arbutus.virtuino.connectors.serial.SerialBaud;
import arbutus.virtuino.connectors.serial.SerialDataBits;
import arbutus.virtuino.connectors.serial.SerialParity;
import arbutus.virtuino.connectors.serial.SerialStopBits;
import arbutus.virtuino.connectors.serial.SerialVirtuinoConnector;
import arbutus.virtuino.connectors.serial.SerialVirtuinoContext;

public class VirtuinoServiceTest {
	
	@Test
	public void getVirtualFloat_When_The_Connector_Returns_An_Exception__Should_Return_Nan() throws Exception {
		// Arrange
		VirtuinoService virtuinoService = new VirtuinoService();
		
		SerialVirtuinoContext engineContext = new SerialVirtuinoContext("myConnector", 3000, "COMX", 1000, SerialBaud.BAUDRATE_4800, SerialDataBits.DATABITS_8, SerialStopBits.STOPBITS_1, SerialParity.PARITY_NONE);
		VirtuinoConnector engineConn = new SerialVirtuinoConnector(engineContext);
		
		virtuinoService.addVirtuinoConnector(VirtuinoServiceType.Engine.getVal(), engineConn);
		
		virtuinoService.start();
		
		// Act
		float value = virtuinoService.getVirtualFloat(VirtuinoServiceType.Engine.getVal(), EngineMeasurementType.AGE.getPin());
		
		// Assert
		assertThat("Because the connector is not able to get a value", Float.isNaN(value), is(true));
	}
	
	@Test
	public void getVirtualFloat_When_The_Connector_Is_Not_Added__Should_Return_Nan() throws Exception {
		// Arrange
		VirtuinoService virtuinoService = new VirtuinoService();
		
		SerialVirtuinoContext engineContext = new SerialVirtuinoContext("myConnector", 3000, "COMX", 1000, SerialBaud.BAUDRATE_4800, SerialDataBits.DATABITS_8, SerialStopBits.STOPBITS_1, SerialParity.PARITY_NONE);
		VirtuinoConnector engineConn = new SerialVirtuinoConnector(engineContext);
		
		virtuinoService.addVirtuinoConnector(VirtuinoServiceType.Engine.getVal(), engineConn);
		
		virtuinoService.start();
		
		// Act
		float value = virtuinoService.getVirtualFloat(VirtuinoServiceType.PIR.getVal(), EngineMeasurementType.AGE.getPin());
		
		// Assert
		assertThat("Because the connector is not added", Float.isNaN(value), is(true));
	}
	
	@Test(expected=VirtuinoServiceException.class)
	public void Start_Twice_Should_Throw_An_Exception() throws Exception {
		// Arrange
		VirtuinoService virtuinoService = new VirtuinoService();
		
		SerialVirtuinoContext engineContext = new SerialVirtuinoContext("myConnector", 3000, "COMX", 1000, SerialBaud.BAUDRATE_4800, SerialDataBits.DATABITS_8, SerialStopBits.STOPBITS_1, SerialParity.PARITY_NONE);
		VirtuinoConnector engineConn = new SerialVirtuinoConnector(engineContext);
		
		virtuinoService.addVirtuinoConnector(VirtuinoServiceType.Engine.getVal(), engineConn);
		
		virtuinoService.start();
		
		// Act
		virtuinoService.start();
		
		// Assert
		fail("Because we were expecting an exception when we start the service twice");
	}
	
	@Test
	public void Stop_Although_The_Service_Is_Not_Started__Should_Stay_Stopped() throws VirtuinoServiceException {
		// Arrange
		VirtuinoService virtuinoService = new VirtuinoService();
		
		SerialVirtuinoContext engineContext = new SerialVirtuinoContext("myConnector", 3000, "COMX", 1000, SerialBaud.BAUDRATE_4800, SerialDataBits.DATABITS_8, SerialStopBits.STOPBITS_1, SerialParity.PARITY_NONE);
		VirtuinoConnector engineConn = new SerialVirtuinoConnector(engineContext);
		
		virtuinoService.addVirtuinoConnector(VirtuinoServiceType.Engine.getVal(), engineConn);
		
		// Act
		virtuinoService.stop();
		
		// Assert
		assertThat("Because the service was not started.", virtuinoService.getState(), is(ServiceState.STOPPED));
	}
	
	@Test(timeout=2000)
	public void Stop_Although_The_Connector_Was_Dead_Should_Stop() throws Exception {
		// Arrange
		VirtuinoService virtuinoService = new VirtuinoService();
		
		SerialVirtuinoContext engineContext = new SerialVirtuinoContext("myConnector", 100, "COMX", 1000, SerialBaud.BAUDRATE_4800, SerialDataBits.DATABITS_8, SerialStopBits.STOPBITS_1, SerialParity.PARITY_NONE);
		VirtuinoConnector engineConn = new SerialVirtuinoConnector(engineContext);
		
		virtuinoService.addVirtuinoConnector(VirtuinoServiceType.Engine.getVal(), engineConn);
		
		virtuinoService.start();
		while(!engineConn.isProcessAlive())
			ToolBox.waitms(100);
		
		engineConn.stopProcess();
		
		while(engineConn.isProcessAlive())
			ToolBox.waitms(100);
		ToolBox.waitms(100);
		
		// Act
		virtuinoService.stop();
		
		// Assert
		assertThat("Because the service was stopped.", virtuinoService.getState(), is(ServiceState.STOPPED));
	}
	
	public class VirtuinoConnectorNotRespondingToInterrupt extends VirtuinoConnector{
		private boolean wasInterrupted = false;
		
		public VirtuinoConnectorNotRespondingToInterrupt(VirtuinoContext context) {
			super(context);
		}
		
		public synchronized boolean wasInterrupted() {
			return wasInterrupted;
		}

		@Override
		protected void checkAndReconnect() {
			try {
				TimeUnit.SECONDS.wait(120);
			} catch (InterruptedException e) {
				wasInterrupted = true;
			}
		}

		@Override
		protected void stop() {
		}

		@Override
		protected boolean isConnectorReady() {
			return true;
		}

		@Override
		protected boolean writeString(String command) throws VirtuinoConnectorException {
			return true;
		}

		@Override
		protected char readChar() throws VirtuinoConnectorException {
			return '!';
		}
	}

}
