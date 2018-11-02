package arbutus.virtuino.connectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.function.BiConsumer;

import org.junit.Test;

import arbutus.test.ToolBox;
import arbutus.virtuino.connectors.VirtuinoCommandType;
import arbutus.virtuino.connectors.VirtuinoConnector;
import arbutus.virtuino.connectors.VirtuinoConnectorException;
import arbutus.virtuino.connectors.VirtuinoContext;
import arbutus.virtuino.connectors.VirtuinoItem;

public class VirtuinoConnectorTest {
	
	@Test(timeout=1000)
	public void Constructor_The_Connector_Should_NotBe_Ready() {
		// Arrange
		
		// Act
		VirtuinoConnector connector = new VirtuinoConnectorStub(new VirtuinoContext("myVirtuinoConnector", 3000));
		
		// Assert
		assertFalse("Because the connector should be ready only after the run function has been called.", connector.isReady());
	}
	
	@Test(timeout=2000)
	public void Run_The_Connector_Should_Eventually_Be_Ready() throws Exception {
		// Arrange
		VirtuinoConnectorStub connector = new VirtuinoConnectorStub(new VirtuinoContext("myVirtuinoConnector", 100));
		
		// Act
		connector.startProcess();
		
		while(!connector.isReady())
			ToolBox.waitms(100);
		
		// Assert
		assertTrue("Because the connector should be ready after the run function has been called.", connector.isReady());
		assertTrue("Because the connector should be ready because the function checkAndReconnect was called at least once.", connector.getNbCallCheckAndReconnect() >= 1);
		
		// tear down
		connector.stopProcess();
		if(connector.isProcessAlive())
			throw new Exception("the connector thread is still alive");
	}
	
	@Test(timeout=2000)
	public void Stop_After_interrupt_the_connector_should_stop() throws Exception {
		// Arrange
		VirtuinoConnectorStub connector = new VirtuinoConnectorStub(new VirtuinoContext("myVirtuinoConnector", 100));
		connector.startProcess();
		
		while(!connector.isReady())
			ToolBox.waitms(100);
		
		// Act
		connector.stopProcess();
		if(connector.isProcessAlive())
			throw new Exception("the connector thread is still alive");
		
		// Assert
		assertTrue("Because the function stop was called at least once.", connector.getNbCallStop() >= 1);
	}
	
	@Test(timeout=2000)
	public void Stop_After_throwing_an_interruption_the_connector_should_stop() throws Exception {
		// Arrange
		VirtuinoConnectorStub connector = new VirtuinoConnectorStub(new VirtuinoContext("myVirtuinoConnector", 100));
		connector.startProcess();
		
		while(!connector.isReady())
			ToolBox.waitms(100);
		
		// Act
		connector.stopProcess();
		if(connector.isProcessAlive())
			throw new Exception("the connector thread is still alive");
		
		// Assert
		assertTrue("Because the function stop was called at least once.", connector.getNbCallStop() >= 1);
	}
	
	@Test(timeout=2000)
	public void GetFirmwareCode_With_An_Unexpected_Answer1_ShouldThrow_An_Exception() throws Exception {
		// Arrange
		VirtuinoConnectorStub connector = new VirtuinoConnectorStub(new VirtuinoContext("myVirtuinoConnector", 100));
		connector.startProcess();
		
		while(!connector.isReady())
			ToolBox.waitms(100);
		
		// Act
		try {
			connector.getSyncFirmwareCode();
			
		//Assert
			fail("Because an exception should have been thrown");
		}
		catch(VirtuinoConnectorException ex) {
			assertEquals("!C00 1.0$", connector.getLastCommandWritten());
		}
		
		// tear down
		connector.stopProcess();
		if(connector.isProcessAlive())
			throw new Exception("the connector thread is still alive");
	}
	
	@Test(timeout=2000)
	public void GetFirmwareCode_With_An_Unexpected_Answer2_ShouldThrow_An_Exception() throws Exception {
		// Arrange
		VirtuinoConnectorStub connector = new VirtuinoConnectorStub(new VirtuinoContext("myVirtuinoConnector", 100));
		connector.setAnswer("!C00=aa$");
		
		connector.startProcess();
		
		while(!connector.isReady())
			ToolBox.waitms(100);
		
		// Act
		try {
			connector.getSyncFirmwareCode();
			
		//Assert
			fail("Because an exception should have been thrown");
		}
		catch(VirtuinoConnectorException ex) {
			assertEquals("!C00 1.0$", connector.getLastCommandWritten());
		}
		
		// tear down
		connector.stopProcess();
		if(connector.isProcessAlive())
			throw new Exception("the connector thread is still alive");
	}
	
	@Test(timeout=2000)
	public void GetFirmwareCode_With_An_Unexpected_Answer3_ShouldThrow_An_Exception() throws Exception {
		// Arrange
		VirtuinoConnectorStub connector = new VirtuinoConnectorStub(new VirtuinoContext("myVirtuinoConnector", 100));
		connector.setAnswer("!$");
		
		connector.startProcess();
		
		while(!connector.isReady())
			ToolBox.waitms(100);
		
		// Act
		try {
			connector.getSyncFirmwareCode();
			
		//Assert
			fail("Because an exception should have been thrown");
		}
		catch(VirtuinoConnectorException ex) {
			assertEquals("!C00 1.0$", connector.getLastCommandWritten());
		}
		
		// tear down
		connector.stopProcess();
		if(connector.isProcessAlive())
			throw new Exception("the connector thread is still alive");
	}
	
	@Test(timeout=2000)
	public void GetFirmwareCode_With_An_Expected_Answer_Should_Not_Throw_An_Exception() throws Exception {
		// Arrange
		VirtuinoConnectorStub connector = new VirtuinoConnectorStub(new VirtuinoContext("myVirtuinoConnector", 100));
		connector.setAnswer("!C00=1.5$");
		
		connector.startProcess();
		
		while(!connector.isReady())
			ToolBox.waitms(100);
		
		// Act
		float firmwareCode = connector.getSyncFirmwareCode();
			
		//Assert
		assertEquals("!C00 1.0$", connector.getLastCommandWritten());
		assertEquals(1.5, firmwareCode, 0.001);
		
		// tear down
		connector.stopProcess();
		if(connector.isProcessAlive())
			throw new Exception("the connector thread is still alive");
	}
	
	@Test(timeout=1000)
	public void Unsubscribe_An_Unknown_Subscriber_Should_Not_Crash() {
		// Arrange
		this.counter = 0;
		this.lastValue = 0;
		VirtuinoConnectorStub connector = new VirtuinoConnectorStub(new VirtuinoContext("myVirtuinoConnector", 100));
		
		// Act
		connector.unsubscribe(new VirtuinoItem(VirtuinoCommandType.VirtualFloat, 0), this::consume);
		
		// Assert
		assertEquals(0, this.counter);
	}
	
	@Test(timeout=2000)
	public void Subscribe_A_Subscriber_Should_Notify_Of_New_Value() throws Exception {
		// Arrange
		this.counter = 0;
		this.lastValue = 0;
		VirtuinoConnectorStub connector = new VirtuinoConnectorStub(new VirtuinoContext("myVirtuinoConnector", 100));
		connector.subscribe(new VirtuinoItem(VirtuinoCommandType.VirtualFloat, 0), this::consume);
		connector.setAnswer("!V00=3.14$");
		
		// Act
		connector.startProcess();
		while(!connector.isConnectorReady()) {
			ToolBox.waitms(100);
		}
		
		// Assert
		assertEquals(1, this.counter);
		assertEquals(3.14, this.lastValue, 0.001);
		
		// tear down
		connector.stopProcess();
		if(connector.isProcessAlive())
			throw new Exception("the connector thread is still alive");
	}
	
	@Test(timeout=2000)
	public void Unsubscribe_A_Subscriber_Should_Not_Be_Notified_After_A_New_Value() throws Exception {
		// Arrange
		this.counter = 0;
		this.lastValue = 0;
		
		BiConsumer<Long, Float> consumer = this::consume;
		
		VirtuinoConnectorStub connector = new VirtuinoConnectorStub(new VirtuinoContext("myVirtuinoConnector", 100));
		connector.subscribe(new VirtuinoItem(VirtuinoCommandType.VirtualFloat, 0), consumer);
		connector.setAnswer("!V00=3.14$");
		
		// Act
		connector.startProcess();
		while(!connector.isConnectorReady()) {
			ToolBox.waitms(10);
		}
		connector.setAnswer("!V00=3.14$");
		
		ToolBox.waitms(200);
		
		// Assert
		assertEquals(2, this.counter);
		assertEquals(3.14, this.lastValue, 0.001);
		
		// Act
		connector.unsubscribe(new VirtuinoItem(VirtuinoCommandType.VirtualFloat, 0), consumer);
		connector.setAnswer("!V00=3.14$");
		
		int lastCounter = this.counter;
		
		ToolBox.waitms(300);
		
		// Assert
		assertEquals(lastCounter, this.counter);
		assertEquals(3.14, this.lastValue, 0.001);
		
		// tear down
		connector.stopProcess();
		if(connector.isProcessAlive())
			throw new Exception("the connector thread is still alive");
		
	}
	
	private int counter = 0;
	private float lastValue;
	private synchronized void consume(Long nantime, Float value) {
		this.counter++;
		this.lastValue = value;
	}
	
	protected class VirtuinoConnectorStub extends VirtuinoConnector{
		
		private boolean isReady = false;
		
		public VirtuinoConnectorStub(VirtuinoContext context) {
			super(context);
		}
		
		public void setReady(boolean isReady) {
			this.isReady = isReady;
		}
		
		public int getNbCallCheckAndReconnect() {
			return this.nbCallCheckAndReconnect;
		}
		
		public int getNbCallStop() {
			return this.nbCallStop;
		}
		
		public String getLastCommandWritten() {
			return this.lastCommandWritten;
		}

		public void setAnswer(String answer) {
			this.answer = answer;
			this.indexChar = 0;
		}
		
		private int nbCallCheckAndReconnect = 0;
		@Override
		protected void checkAndReconnect() {
			this.nbCallCheckAndReconnect++;
			this.isReady = true;
		}

		private int nbCallStop = 0;
		@Override
		protected void stop() {
			this.nbCallStop++;
		}

		@Override
		protected boolean isConnectorReady() {
			return this.isReady;
		}

		private String lastCommandWritten = null;
		@Override
		protected boolean writeString(String command) throws VirtuinoConnectorException {
			this.lastCommandWritten = command;
			return true;
		}

		
		private int indexChar = 0;
		private String answer = null;
		
		@Override
		protected char readChar() throws VirtuinoConnectorException {
			if(answer == null || indexChar == answer.length()) {
				throw new VirtuinoConnectorException("Out of range exception");
			}
			
			return answer.charAt(indexChar++);
		}
	}
}
