package arbutus.virtuino.connectors.serial;

import java.util.function.BiConsumer;

import arbutus.test.ToolBox;
import arbutus.virtuino.connectors.VirtuinoCommandType;
import arbutus.virtuino.connectors.VirtuinoConnector;
import arbutus.virtuino.connectors.VirtuinoConnectorException;
import arbutus.virtuino.connectors.VirtuinoItem;

public class SerialVirtuinoConnectorForLinuxTest {
	private static VirtuinoConnector connector = null;
	private static Thread connectorThread = null;

	public static void main(String[] args) {
		try {
			SerialVirtuinoConnectorForLinuxTest tester = new SerialVirtuinoConnectorForLinuxTest();
			
			//connector = new SerialVirtuinoConnector(new SerialVirtuinoContext(3000, "COM10", 1000, SerialBaud.BAUDRATE_38400, SerialDataBits.DATABITS_8, SerialStopBits.STOPBITS_1, SerialParity.PARITY_NONE));
			connector = new SerialVirtuinoConnector(new SerialVirtuinoContext(3000, "/dev/rfcomm0", 3000, SerialBaud.BAUDRATE_38400, SerialDataBits.DATABITS_8, SerialStopBits.STOPBITS_1, SerialParity.PARITY_NONE));
			//connector = new SerialVirtuinoConnector(new SerialVirtuinoContext(3000, "/dev/rfcomm1", 15000, SerialBaud.BAUDRATE_4800, SerialDataBits.DATABITS_8, SerialStopBits.STOPBITS_1, SerialParity.PARITY_NONE));
			connectorThread = new Thread(connector);
			
			connectorThread.start();
			
			// The connector won't be running right after the start, so let's wait a maximum of 10sec ...
			int nbTenthSec = 0;
			while(!connector.isReady() && nbTenthSec++ < 200)
				ToolBox.waitms(100);
			
			if(!connector.isReady()) {
				throw new VirtuinoConnectorException("Connector did not get ready within 20 sec...");
			}
			
			try {
				System.out.println("Test the readSyncDigitalPin function");
				System.out.println("Pin12 = " + connector.readSyncDigitalPin(12));
				
				System.out.println("Test the subscription on the digital pin 12");
				
				BiConsumer<Long, Float> biConsumer = tester::onDigitalValue;
				connector.subscribe(new VirtuinoItem(VirtuinoCommandType.DigitalRead, 12), biConsumer);
				
				System.out.println("180 sec for testing");
				ToolBox.wait(180);
				
				connector.unsubscribe(new VirtuinoItem(VirtuinoCommandType.DigitalRead, 12), biConsumer);
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
			
			connector.interrupt();
			
			nbTenthSec = 0;
			while(nbTenthSec++ < 100 && connectorThread.isAlive()) {
				ToolBox.waitms(100);
			}
			
			if(connectorThread.isAlive()) {
				throw new VirtuinoConnectorException("The thread of the connector is still alive.");
			}
		
		} catch (VirtuinoConnectorException e1) {
			e1.printStackTrace();
		}
	}
	
	public void onDigitalValue(Long nano, Float value) {
		long sec = nano / 1000000000L;
		System.out.println("At " + sec + ", a new value arrived: "+ value);
	}
}
