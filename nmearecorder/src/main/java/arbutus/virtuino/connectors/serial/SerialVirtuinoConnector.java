package arbutus.virtuino.connectors.serial;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import arbutus.virtuino.connectors.VirtuinoCommandType;
import arbutus.virtuino.connectors.VirtuinoConnector;
import arbutus.virtuino.connectors.VirtuinoConnectorException;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;

public final class SerialVirtuinoConnector  extends VirtuinoConnector {
	private static final Logger log = Logger.getLogger(SerialVirtuinoConnector.class);

	private static final int MAX_CONSECUTIVE_TIMEOUT_BEFORE_RECONNECTION = 3;
	private int consecutiveTimeout = 0;
	
	private final SerialVirtuinoContext context;
	
	private final SerialPort serialPort;
	private boolean isReady = false;
	
	public SerialVirtuinoConnector(SerialVirtuinoContext context) {
		super(context);
		this.context = context;
		
		this.serialPort = new SerialPort(this.context.portName);
	}
	
	@Override
	protected void stop() {
		try {
			if(this.serialPort.closePort() == false) {
				log.error("Error when trying to close the port " + this.context.portName);
			}
		} catch (SerialPortException e) {
			log.error("Error when trying to close the port " + this.context.portName, e);
		}
		
		this.isReady = false;
	}

	@Override
	protected void checkAndReconnect() {	
		if(!this.isConnectorReady()) {
			if(this.serialPort.isOpened() || this.openPort()) {
				try {
					if(this.serialPort.setParams(this.context.baudRate.getVal(), this.context.dataBits.getVal(), this.context.stopBits.getVal(), this.context.parity.getVal()) == false){
						this.stop();
					}
					else {
						int nbTries = 0;
						while(!this.isReady && nbTries++ < 3) {
							try {
								this.serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
								
								float firmwareCode = this.writeFloat(VirtuinoCommandType.FirmwareCode, 0, 1);
								log.info("Connected to Virtuino firmware " + firmwareCode);
								
								this.isReady = true; 
							}
							catch(VirtuinoConnectorException getFirmwareEx) {
							}
						}
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
		return this.serialPort.isOpened() && this.isReady;
	}

	private boolean successWriting = false;
	@Override
	protected boolean writeString(String command) throws VirtuinoConnectorException {
		CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> {
			try {
				this.serialPort.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
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
			this.stop();
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
			this.consecutiveTimeout = 0;
			return (char) arraybyte[0];
		} catch (SerialPortException e) {
			this.stop();
			throw new VirtuinoConnectorException("An SerialPortException has been thrown. We will attempt to reconnect.", e);
		}
		catch(SerialPortTimeoutException e) {
			String errorMsg = "A timeout occurs when reading the port " + this.context.portName + ". You should increase the readWriteTimeoutMilliSec [" + this.context.readWriteTimeoutMilliSec + "ms] property.";
			if(++this.consecutiveTimeout == MAX_CONSECUTIVE_TIMEOUT_BEFORE_RECONNECTION) {
				errorMsg += "\nBecause " + consecutiveTimeout + " consecutive timeouts occured, we will attempt to reconnect.";
				this.stop();
			}
			
			throw new VirtuinoConnectorException(errorMsg);
		}
	}

}
