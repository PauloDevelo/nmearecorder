package arbutus.virtuino.connectors.serial;

import arbutus.util.PropertiesFile;
import arbutus.virtuino.connectors.VirtuinoContext;

public class SerialVirtuinoContext extends VirtuinoContext{
	/**
	 * Port name the connector shall connect to
	 */
	public final String portName;
	
	/**
	 * Timeout in millisecond for the character reading
	 */
	public final int readWriteTimeoutMilliSec;
	
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
		super();
		
		this.setScanRateInMilliSec(scanRateInMilliSec);
		
		this.portName = portName;
		this.readWriteTimeoutMilliSec = charReadTimeoutMilliSec;
		this.baudRate = baudRate;
		this.dataBits = dataBits;
		this.stopBits = stopBits;
		this.parity = parity;
	}

	public SerialVirtuinoContext(String propertiesFilename) {
		super();
		
		String fileSep = System.getProperty("file.separator");
		String propertiesPath = System.getProperty("user.dir") + fileSep + "properties" + fileSep + propertiesFilename;
		PropertiesFile properties = PropertiesFile.getPropertiesVM(propertiesPath);
		
		this.setScanRateInMilliSec(properties.getValueInt("scanRateInMilliSec", 1000));
		
		this.portName = properties.getValue("portName");
		this.readWriteTimeoutMilliSec = properties.getValueInt("readWriteTimeoutMilliSec", 500);
		this.baudRate = SerialBaud.valueOf(properties.getValueInt("baudRate", SerialBaud.BAUDRATE_4800.getVal()));
		this.dataBits = SerialDataBits.valueOf(properties.getValueInt("dataBits", SerialDataBits.DATABITS_8.getVal()));
		this.stopBits = SerialStopBits.valueOf(properties.getValueInt("stopBits", SerialStopBits.STOPBITS_1.getVal()));
		this.parity = SerialParity.valueOf(properties.getValueInt("parity", SerialParity.PARITY_NONE.getVal()));
	}

}
