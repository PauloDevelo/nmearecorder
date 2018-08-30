package arbutus.timeservice;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import arbutus.nmea.sentences.GPRMC;
import arbutus.nmea.sentences.NMEASentence;
import arbutus.nmea.service.INMEAListener;
import arbutus.nmea.service.INMEAService;
import arbutus.service.IService;
import arbutus.service.ServiceManager;
import arbutus.service.ServiceState;

public class TimeService implements IService, ITimeService, INMEAListener {
	
	private ServiceState state = ServiceState.STOPPED;
	private List<Long> referenceTimes = new ArrayList<Long>();
	
	private long referenceTime = 0;
	
	public TimeService() {
	}

	@Override
	public synchronized boolean isSynchonized() {
		
		return referenceTimes.size() > 4;
	}

	@Override
	public synchronized Date getUTCDateTime() throws SynchronizationException {
		if(!isSynchonized()) {
			throw new SynchronizationException();
		}
		else {
			return new Date(referenceTime + System.nanoTime() / 1000000);
		}
	}
	
	@Override
	public synchronized long getAccuracyNano() throws SynchronizationException {
		if(!isSynchonized()) {
			throw new SynchronizationException();
		}
		else {
			return 1000000000 / referenceTimes.size();
		}
	}

	@Override
	public synchronized void onNewNMEASentence(NMEASentence sentence) {
		GPRMC gprmc = GPRMC.class.cast(sentence);
		if(gprmc.getUtcDateTime() != null) {
			referenceTimes.add(gprmc.getUtcDateTime().getTime() - System.nanoTime() / 1000000);
			
			if(referenceTimes.size() > 100) {
				referenceTimes.remove(0);
			}
			
			referenceTime = 0;
			for(Long refTime : referenceTimes) {
				referenceTime += refTime;
			}
			referenceTimes.stream().map(refTime -> referenceTime += refTime);
			referenceTime /= referenceTimes.size();
		}
	}

	@Override
	public ServiceState getState() {
		return state;
	}

	@Override
	public void start() {
		if(state == ServiceState.STOPPED) {
			INMEAService nmeaService = ServiceManager.getInstance().getService(INMEAService.class);
			nmeaService.subscribe(GPRMC.class, this);
			state = ServiceState.STARTED;
		}
	}

	@Override
	public void stop() {
		if(state == ServiceState.STARTED) {
			INMEAService nmeaService = ServiceManager.getInstance().getService(INMEAService.class);
			nmeaService.unsubscribe(GPRMC.class, this);
			state = ServiceState.STOPPED;
		}
	}
}
