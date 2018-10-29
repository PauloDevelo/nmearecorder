package arbutus.virtuino.connectors.serial;

import arbutus.test.ToolBox;
import arbutus.virtuino.connectors.VirtuinoConnector;
import arbutus.virtuino.connectors.VirtuinoConnectorException;

public class SerialVirtuinoConnectorForLinuxTest {
	private static VirtuinoConnector connector = null;
	private static Thread connectorThread = null;

	public static void main(String[] args) {
		try {
			//connector = new SerialVirtuinoConnector(new SerialVirtuinoContext(3000, "/dev/rfcomm0", 10000, SerialBaud.BAUDRATE_38400, SerialDataBits.DATABITS_8, SerialStopBits.STOPBITS_1, SerialParity.PARITY_NONE));
			connector = new SerialVirtuinoConnector(new SerialVirtuinoContext(3000, "/dev/rfcomm1", 15000, SerialBaud.BAUDRATE_4800, SerialDataBits.DATABITS_8, SerialStopBits.STOPBITS_1, SerialParity.PARITY_NONE));
			connectorThread = new Thread(connector);
			
			connectorThread.start();
			
			// The connector won't be running right after the start, so let's wait a maximum of 10sec ...
			int nbSec = 0;
			while(!connector.isReady() && nbSec++ < 20)
				ToolBox.wait(1);
			
			if(!connector.isReady()) {
				throw new VirtuinoConnectorException("Connector did not get ready within 20 sec...");
			}
			
			try {
//				System.out.println("Pin12 = " + connector.readSyncDigitalPin(12));
				System.out.println("Age = " + connector.getSyncVirtualFloat(0));
				System.out.println("Rpm = " + connector.getSyncVirtualFloat(1));
				System.out.println("Cnso = " + connector.getSyncVirtualFloat(2));
				System.out.println("Diesiel Qty = " + connector.getSyncVirtualFloat(3));
				System.out.println("Temp exhaust = " + connector.getSyncVirtualFloat(4));
				System.out.println("Voltage = " + connector.getSyncVirtualFloat(5));
				System.out.println("Coolant temp = " + connector.getSyncVirtualFloat(6));
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
			
			connector.interrupt();
			
			nbSec = 0;
			while(nbSec++ < 10 && connectorThread.isAlive()) {
				ToolBox.wait(1);
			}
			
			if(connectorThread.isAlive()) {
				throw new VirtuinoConnectorException("The thread of the connector is still alive.");
			}
		
		} catch (VirtuinoConnectorException e1) {
			e1.printStackTrace();
		}
	}
}
