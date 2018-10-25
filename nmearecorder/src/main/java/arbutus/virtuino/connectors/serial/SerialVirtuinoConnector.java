package arbutus.virtuino.connectors.serial;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import arbutus.virtuino.connectors.VirtuinoConnector;
import arbutus.virtuino.connectors.VirtuinoConnectorException;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

public final class SerialVirtuinoConnector  extends VirtuinoConnector {
	private final static Logger log = Logger.getLogger(SerialVirtuinoConnector.class);
	
	private final SerialVirtuinoContext context;
	
	private final SerialPort serialPort;

	public SerialVirtuinoConnector(SerialVirtuinoContext context) {
		super(context);
		this.context = context;
		
		this.serialPort = new SerialPort(this.context.portName);
	}
	
	@Override
	protected void stop() {
		if (this.isConnectorReady()) {
			try {
				if(this.serialPort.closePort() == false) {
					log.error("Error when trying to close the port " + this.context.portName);
				}
			} catch (SerialPortException e) {
				log.error("Error when trying to close the port " + this.context.portName, e);
			}
		}
	}

	@Override
	protected void checkAndReconnect() {	
		if(!this.isConnectorReady()) {
			if(this.openPort()) {
				try {
					if(this.serialPort.setParams(this.context.baudRate.getVal(), this.context.dataBits.getVal(), this.context.stopBits.getVal(), this.context.parity.getVal()) == false){
						log.warn("Impossible de paramétrer le port " + context.portName + ". We will try again later.");
						
						this.stop();
					}
				} catch (SerialPortException e) {
					log.error("Error when trying to configure the port " + this.context.portName, e);
					this.stop();
				}
			}
		}
	}
	
	private boolean openPort() {
		try {
			if(this.serialPort.openPort() == true){
				return true;
			}
			else {
				return false;
			}
		}
		catch (SerialPortException e) {
			return false;
		}
	}

	@Override
	protected boolean isConnectorReady() {
		return this.serialPort.isOpened();
	}

	private boolean successWriting = false;
	@Override
	protected boolean writeString(String command) throws VirtuinoConnectorException {
		CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> {
			try {
				this.serialPort.writeBytes(command.getBytes());
				this.successWriting = true;
			} catch (SerialPortException e) {
				stop();
				this.successWriting = false;
			}
		});
		
		long timeout = System.currentTimeMillis() + this.context.readWriteTimeoutMilliSec;
		while(!cf.isDone() && System.currentTimeMillis() < timeout) {
			try {
				TimeUnit.MILLISECONDS.sleep(20);
			} catch (InterruptedException e) {
				break;
			}
		}
		
		if(!cf.isDone()) {
			cf.cancel(true);
			stop();
			return false;
		}
		else {
			return successWriting;
		}
	}

	@Override
	protected char readChar() throws VirtuinoConnectorException {
		try {
			byte[] arraybyte = this.serialPort.readBytes(1, this.context.readWriteTimeoutMilliSec);
			return (char) arraybyte[0];
		} catch (SerialPortException e) {
			this.stop();
			throw new VirtuinoConnectorException("An SerialPortException has been thrown. We will attempt to reconnect.", e);
		}
		catch(SerialPortTimeoutException e) {
			throw new VirtuinoConnectorException("A timeout occurs when reading the port " + this.context.portName + ". You should increase the readWriteTimeoutMilliSec property.");
		}
	}

}
