package arbutus.virtuino.connectors;

public class VirtuinoContext {
	/**
	 * Period of time the connector will check the Virtuino connection and read new values.
	 */
	private int scanRateInMilliSec = 1000;
	
	public final String connectorKey;
	
	public VirtuinoContext(String connectorKey, int scanRateInMilliSec) {
		this.scanRateInMilliSec = scanRateInMilliSec;
		this.connectorKey = connectorKey;
	}
	
	public VirtuinoContext(String connectorkey) {
		this.connectorKey = connectorkey;
	}

	/**
	 * @return the scanRateInMilliSec
	 */
	public int getScanRateInMilliSec() {
		return scanRateInMilliSec;
	}

	/**
	 * @param scanRateInMilliSec the scanRateInMilliSec to set
	 */
	protected void setScanRateInMilliSec(int scanRateInMilliSec) {
		this.scanRateInMilliSec = scanRateInMilliSec;
	}
}
