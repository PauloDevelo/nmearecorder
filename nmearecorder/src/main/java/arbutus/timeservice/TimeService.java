package arbutus.timeservice;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import arbutus.nmea.sentences.GPRMC;
import arbutus.nmea.sentences.NMEASentence;
import arbutus.nmea.service.INMEAListener;
import arbutus.nmea.service.INMEAService;
import arbutus.service.IService;
import arbutus.service.ServiceManager;
import arbutus.service.ServiceState;
import arbutus.util.PropertiesFile;
import arbutus.util.Utils;

public class TimeService implements IService, ITimeService, INMEAListener {
	private static Logger log = Logger.getLogger(TimeService.class);
	
	private ServiceState state = ServiceState.STOPPED;
	private List<Long> referenceTimes = new ArrayList<Long>();
	
	private long referenceTime = 0;
	
	private TimeServiceContext context = null;

	private ScheduledExecutorService executor = null;
	
	public TimeService(TimeServiceContext context) {
		this.context = context;
	}
	
	public TimeService() {
		String fileSep = System.getProperty("file.separator");
		String propertiesPath = System.getProperty("user.dir") + fileSep + "properties" + fileSep + "timeservice.properties";
				 
		PropertiesFile properties = PropertiesFile.getPropertiesVM(propertiesPath);
		
		this.context = new TimeServiceContext(properties.getValueInt("synced", 4), 
				properties.getValueInt("synchronized", 30), 
				properties.getValueInt("resynchronized", 86400), 
				properties.getValue("exec"));
	}

	@Override
	public synchronized boolean isSynchonized() {
		return referenceTimes.size() >= context.getSynced();
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
	public Date getUTCDateTime(long nanoTime) throws SynchronizationException {
		if(!isSynchonized()) {
			throw new SynchronizationException();
		}
		else {
			return new Date(referenceTime + nanoTime / 1000000);
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
			referenceTimes.add(gprmc.getUtcDateTime().getTime() - gprmc.getReceptionNanoTime() / 1000000);
			
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

	private Boolean setSystemTime() throws IllegalStateException, IOException, InterruptedException {
		int count = 0;
		boolean success = false;
		while(success == false && count < 5) {
			count++;

			try {
				StringBuilder exec = getFormatedSyncCommand(this.getUTCDateTime());
				log.info("System date synchrnoisation: " + exec);
				success = Utils.execCommandSync(exec, 4);
				
				if(success) {
	            	log.info("System date synchronized with the GPS succesfully.");
	            }
	            else {
	            	log.warn("System date synchronization failed.");
	            }
			} 
			catch (SynchronizationException e) {
				log.warn("TimeService is not yet synchronized with the GPS. Impossible to synchonized the system.");
			}
			
			if(!success) {
				if(count < 5) {
					log.warn("It will try again in 5 seconds.");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						count = 5;
					}
				}
				else {
					log.warn("It will try again in " + context.getResync() + " seconds.");
				}
			}
		}
		
		return success;
	}
	
	private StringBuilder getFormatedSyncCommand(Date dateTime) throws InvalidPropertiesFormatException {
		StringBuilder exec = new StringBuilder();
		
		if(dateTime == null) {
			throw new IllegalArgumentException("The date cannot be null.");
		}
	
		int firstIndex = context.getExecStr().indexOf('"');
		int lastIndex = context.getExecStr().lastIndexOf('"');
		
		if(firstIndex == -1 || firstIndex == lastIndex) {
			throw new InvalidPropertiesFormatException("The property exec [" + context.getExecStr() + "] should contains a pair of \" within is the date format.");
		}
		
		SimpleDateFormat df = new SimpleDateFormat(context.getExecStr().substring(firstIndex + 1, lastIndex));
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		exec.append(context.getExecStr().subSequence(0, firstIndex + 1)).append(df.format(dateTime)).append(context.getExecStr().substring(lastIndex));

		return exec;
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

			Runnable setSystemTimeTask = () -> {
				try {
					this.setSystemTime();
				} catch (Exception e) {
					log.error("Impossible to synchronize the system time.", e);
					Thread t = Thread.currentThread();
					t.getUncaughtExceptionHandler().uncaughtException(t, e);
				} 
			}; 
			
			executor = Executors.newSingleThreadScheduledExecutor();
			executor.scheduleAtFixedRate(setSystemTimeTask, context.getSynchro(), context.getResync(), TimeUnit.SECONDS);

			state = ServiceState.STARTED;
			
			log.info("TimeService started");
		}
	}

	@Override
	public void stop() {
		if(state == ServiceState.STARTED) {
			INMEAService nmeaService = ServiceManager.getInstance().getService(INMEAService.class);
			nmeaService.unsubscribe(GPRMC.class, this);
			
			if(executor != null) {
				executor.shutdown();
				try {
				    if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
				    	executor.shutdownNow();
				    } 
				} catch (InterruptedException e) {
					executor.shutdownNow();
				}
				executor = null;
			}
			
			referenceTimes.clear();
			referenceTime = 0;
			
			state = ServiceState.STOPPED;
			
			log.info("TimeService stopped");
		}
	}
}
