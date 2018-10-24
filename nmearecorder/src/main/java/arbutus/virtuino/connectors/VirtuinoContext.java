package arbutus.virtuino.connectors;

public class VirtuinoContext {
	/**
	 * Period of time the connector will check the Virtuino connection and read new values.
	 */
	public final int scanRateInMilliSec;
	
	public VirtuinoContext(int scanRateInMilliSec) {
		this.scanRateInMilliSec = scanRateInMilliSec;
	}
	
}
