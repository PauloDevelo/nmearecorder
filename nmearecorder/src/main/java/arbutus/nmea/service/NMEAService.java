package arbutus.nmea.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.log4j.Logger;

import arbutus.nmea.sentences.GPRMC;
import arbutus.nmea.sentences.GPVTG;
import arbutus.nmea.sentences.HCHDG;
import arbutus.nmea.sentences.NMEASentence;
import arbutus.nmea.sentences.SDDPT;
import arbutus.nmea.sentences.VWMTW;
import arbutus.nmea.sentences.VWVHW;
import arbutus.nmea.sentences.WIMWV;
import arbutus.service.IService;
import arbutus.service.ServiceState;
import arbutus.util.TCPReader;

public class NMEAService implements IService, INMEAService {
	private static Logger log = Logger.getLogger(NMEAService.class);
	
	private TCPReader tcpReader = null;
	private Thread threadTcpReader = null;
	
	private HashMap<Class<? extends NMEASentence>, List<INMEAListener>> _subscribers = new HashMap<>(); 
	
	private void onReceiveNMEASentence(Long receptionNanoTime, StringBuilder nmeaSentence) {
		if(nmeaSentence == null)
		{
			log.error("nmea sentence is null.");
			return;
		}
		
		String sticker = nmeaSentence.substring(0, 6);

		switch(sticker)
		{
			case WIMWV.sticker:
				processMessage(WIMWV.class, new WIMWV(receptionNanoTime, nmeaSentence));
				return;
			case VWMTW.sticker:
				processMessage(VWMTW.class, new VWMTW(receptionNanoTime, nmeaSentence));
				return;
			case VWVHW.sticker:
				processMessage(VWVHW.class, new VWVHW(receptionNanoTime, nmeaSentence));
				return;
			case HCHDG.sticker:
				processMessage(HCHDG.class, new HCHDG(receptionNanoTime, nmeaSentence));
				return;
			case GPVTG.sticker:
				processMessage(GPVTG.class, new GPVTG(receptionNanoTime, nmeaSentence));
				return;
			case SDDPT.sticker:
				processMessage(SDDPT.class, new SDDPT(receptionNanoTime, nmeaSentence));
				return;
			case GPRMC.sticker:
				processMessage(GPRMC.class, new GPRMC(receptionNanoTime, nmeaSentence));
				return;
			default:
				return;
		}
	}
		
	private <T extends NMEASentence> void processMessage(Class<T> messageType, T message)	{
		List<INMEAListener> listeners = _subscribers.get(messageType);
		if(listeners != null) {
			@SuppressWarnings("rawtypes")
			CompletableFuture[] cfs = new CompletableFuture[listeners.size()];
			int i = 0;
			for(INMEAListener listener : listeners) {
				cfs[i++] = CompletableFuture.completedFuture(message).thenAcceptAsync(s -> listener.onNewNMEASentence(message));
			}
			
			CompletableFuture.allOf(cfs).whenComplete((v, th) -> {
			});
		}
	}
	
	@Override
	public <T extends NMEASentence> void  subscribe(Class<T> key, INMEAListener listener) {
		if(key == null || listener == null) {
			log.error("The key has to be not null. The listener cannot be null.");
			return;
		}
		
		List<INMEAListener> listeners = _subscribers.get(key);
		if (listeners == null) {
			listeners = new ArrayList<>();
			_subscribers.put(key, listeners);
		}
		
		listeners.add(listener);
	}

	@Override
	//public <T extends NMEASentence> void unsubscribe(Class<T> key, INMEAListener<T> listener) {
	public <T extends NMEASentence> void unsubscribe(Class<T> key, INMEAListener listener) {
		if(key == null || listener == null) {
			log.error("The key has to be not null. The listener cannot be null.");
			return;
		}
		
		List<INMEAListener> listeners = _subscribers.get(key);
		if(listeners == null) {
			log.error("We cannot find the key " + key.getName());
			return;
		}
		
		if(!listeners.remove(listener)) {
			log.error("The listener cannot be find in the listeners of " + key.getName());
		}
	}

	@Override
	public ServiceState getState() {
		if(threadTcpReader != null && threadTcpReader.isAlive())
			return ServiceState.STARTED;
		else
			return ServiceState.STOPPED;
	}

	@Override
	public void start() {
		tcpReader = new TCPReader(this::onReceiveNMEASentence);
		threadTcpReader = new Thread(tcpReader);
		
		threadTcpReader.start();
	}

	@Override
	public void stop() {
		if (threadTcpReader != null && threadTcpReader.isAlive()) {
			tcpReader.setInterrupted(true);
		}
	}

}
