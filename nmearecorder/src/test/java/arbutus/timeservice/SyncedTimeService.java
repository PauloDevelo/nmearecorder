package arbutus.timeservice;

import java.util.Date;
import arbutus.service.ServiceState;

public class SyncedTimeService implements TimeServiceInterface{
	
	private final long timeInNano;
	private final Date now;
	private final boolean allowStart;

	public SyncedTimeService(long nanoTime, Date date, boolean allowStart) {
		this.timeInNano = nanoTime;
		this.now = date;
		this.allowStart = allowStart;
	}
	
	public SyncedTimeService(boolean allowStart) {
		this(-1, null, allowStart);
	}

	@Override
	public boolean isSynchonized() {
		return true;
	}

	@Override
	public Date getUTCDateTime(long nanoTime) throws SynchronizationException {
		if(this.now == null) {
			return new Date(System.currentTimeMillis());
		}
		else if(this.timeInNano == nanoTime) {
			return this.now;
		}
		else {
			throw new SynchronizationException();
		}
	}

	@Override
	public Date getUTCDateTime() throws SynchronizationException {
		return new Date(System.currentTimeMillis());
	}

	@Override
	public long getAccuracyNano() throws SynchronizationException {
		return 1000;
	}

	@Override
	public ServiceState getState() {
		return null;
	}

	@Override
	public void start() throws Exception {
		if(!this.allowStart) {
			throw new Exception("start function is not allowed");
		}
	}

	@Override
	public void stop() {
	}
}
