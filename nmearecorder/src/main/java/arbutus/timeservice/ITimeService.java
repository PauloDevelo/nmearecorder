package arbutus.timeservice;

import java.util.Date;

public interface ITimeService {
	
	boolean isSynchonized();
	
	Date getUTCDateTime() throws SynchronizationException;
	
	Date getUTCDateTime(long nanoTime) throws SynchronizationException;
	
	public long getAccuracyNano() throws SynchronizationException;

}
