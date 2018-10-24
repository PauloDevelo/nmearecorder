package arbutus.virtuino.connectors.serial;

import org.apache.log4j.Logger;

import arbutus.virtuino.connectors.VirtuinoConnector;
import arbutus.virtuino.connectors.VirtuinoConnectorException;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

public final class SerialVirtuinoConnector  extends VirtuinoConnector {
	private final static Logger log = Logger.getLogger(SerialVirtuinoConnector.class);
	
	private final SerialVirtuinoContext context;
	
	private SerialPort serialPort = null;

	private boolean isPortReady = false;

	public SerialVirtuinoConnector(SerialVirtuinoContext context) {
		super(context);
		this.context = context;
		
		this.serialPort = new SerialPort(this.context.portName);
	}
	
	@Override
	protected void stop() {
		if (this.serialPort != null && this.serialPort.isOpened()) {
			try {
				if(this.serialPort.closePort() == false) {
					log.error("Error when trying to close the port " + this.context.portName);
				}
			} catch (SerialPortException e) {
				log.error("Error when trying to close the port " + this.context.portName, e);
			}
		}
		
		this.isPortReady = false;
		this.serialPort = null;
	}

	@Override
	protected void checkAndReconnect() {	
		if(!this.serialPort.isOpened()) {
			this.isPortReady = false;
			try {
				if(this.serialPort.openPort() == true){
					if(this.serialPort.setParams(this.context.baudRate.getVal(), this.context.dataBits.getVal(), this.context.stopBits.getVal(), this.context.parity.getVal()) == false){
						log.warn("Impossible de paramétrer le port " + context.portName + ". We will try again later.");
						
						if(this.serialPort.closePort() == false) {
							log.error("Error when trying to close the port " + this.context.portName);
							this.serialPort = null;
						}
					}
					
					this.isPortReady = true;
				}
				else {
					log.warn("Impossible d'ouvrir le port " + context.portName + ". We will try again later.");
				}
			}
			catch (SerialPortException e) {
				log.error("Error when trying to open/configure or close the port " + this.context.portName, e);
				this.serialPort = null;
			}
		}
	}
	

	@Override
	protected boolean isConnectorReady() {
		return this.isPortReady;
	}

	@Override
	protected boolean writeString(String command) throws VirtuinoConnectorException {
		try {
			return this.serialPort.writeBytes(command.getBytes());
		} catch (SerialPortException e) {
			this.stop();
			throw new VirtuinoConnectorException("An SerialPortException has been thrown. We will attempt to reconnect.", e);
		}
	}

	@Override
	protected char readChar() throws VirtuinoConnectorException {
		try {
			byte[] arraybyte = this.serialPort.readBytes(1, this.context.charReadTimeoutMilliSec);
			return (char) arraybyte[0];
		} catch (SerialPortException e) {
			this.stop();
			throw new VirtuinoConnectorException("An SerialPortException has been thrown. We will attempt to reconnect.", e);
		}
		catch(SerialPortTimeoutException e) {
			throw new VirtuinoConnectorException("A timeout occurs when reading the port " + this.context.portName + ". You should increase the charReadTimeoutMilliSec property.");
		}
	}

}
