package arbutus.virtuino.connectors.serial;

import arbutus.virtuino.connectors.VirtuinoContext;

public class SerialVirtuinoContext extends VirtuinoContext{
	/**
	 * Port name the connector shall connect to
	 */
	public final String portName;
	
	/**
	 * Timeout in millisecond for the character reading
	 */
	public final int charReadTimeoutMilliSec;
	
	/**
	 * Serial connection baud rate
	 */
	public final SerialBaud baudRate;
	
	/**
	 * Serial data bits
	 */
	public final SerialDataBits dataBits;
	
	/**
	 * Stop bits
	 */
	public final SerialStopBits stopBits;
	
	/**
	 * Parity
	 */
	public final SerialParity parity;
	
	public SerialVirtuinoContext(int scanRateInMilliSec, String portName, int charReadTimeoutMilliSec, SerialBaud baudRate, SerialDataBits dataBits, SerialStopBits stopBits, SerialParity parity) {
		super(scanRateInMilliSec);
		
		this.portName = portName;
		this.charReadTimeoutMilliSec = charReadTimeoutMilliSec;
		this.baudRate = baudRate;
		this.dataBits = dataBits;
		this.stopBits = stopBits;
		this.parity = parity;
	}

}
