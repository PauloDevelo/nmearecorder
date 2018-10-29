package arbutus.virtuino.connectors;

public class VirtuinoContext {
	/**
	 * Period of time the connector will check the Virtuino connection and read new values.
	 */
	private int scanRateInMilliSec = 1000;
	
	public VirtuinoContext(int scanRateInMilliSec) {
		this.scanRateInMilliSec = scanRateInMilliSec;
	}
	
	public VirtuinoContext() {
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
