package arbutus.timeservice;

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
import arbutus.util.OSValidator;
import arbutus.util.PropertiesFile;
import arbutus.util.Utils;

public class TimeService implements IService, ITimeService, INMEAListener {
	private static Logger log = Logger.getLogger(TimeService.class);
	private PropertiesFile properties = null;
	
	private ServiceState state = ServiceState.STOPPED;
	private List<Long> referenceTimes = new ArrayList<Long>();
	
	private long referenceTime = 0;
	
	final private int synced;
	final private int synchro;
	final private int resync;

	private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	
	public TimeService() {
		String fileSep = System.getProperty("file.separator");
		String propertiesPath = System.getProperty("user.dir") + fileSep + "properties" + fileSep + "timeservice.properties";
				 
		properties = PropertiesFile.getPropertiesVM(propertiesPath);
		
		synced = properties.getValueInt("synced", 4);
		synchro = properties.getValueInt("synchronized", 30);
		resync = properties.getValueInt("resynchronized", 86400);
	}

	@Override
	public synchronized boolean isSynchonized() {
		
		return referenceTimes.size() > synced;
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

	private void setSystemTime() {
		try {
			int count = 0;
			boolean success = false;
			while(success == false && count < 5) {
				count++;
				
				if(this.isSynchonized()) {
					
					StringBuilder exec = formatSyncCommand(this.getUTCDateTime());
					
					log.info("System date synchrnoisation: " + exec);
					success = Utils.execCommandSync(exec);
					
					if(success) {
		            	log.info("System date synchronized with the GPS succesfully.");
		            }
		            else {
		            	log.warn("System date synchronization failed.");
		            }
					
				}
				else {
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
						log.warn("It will try again in " + resync + " seconds.");
					}
				}
			}
		}
		catch(Exception ex) {
			log.error("An error occured when synchronizing the system date.", ex);
		}
	}
	
	private StringBuilder formatSyncCommand(Date dateTime) throws InvalidPropertiesFormatException {
		StringBuilder exec = new StringBuilder();
		
		if(dateTime == null) {
			throw new IllegalArgumentException("The date cannot be null.");
		}
		
		String execStr = properties.getValue("exec");
		int firstIndex = execStr.indexOf('"');
		int lastIndex = execStr.lastIndexOf('"');
		
		if(firstIndex == -1 || firstIndex == lastIndex) {
			throw new InvalidPropertiesFormatException("The property exec [" + execStr + "] should contains a pair of \" within is the date format.");
		}
		
		SimpleDateFormat df = new SimpleDateFormat(execStr.substring(firstIndex + 1, lastIndex));
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		exec.append(execStr.subSequence(0, firstIndex + 1)).append(df.format(dateTime)).append(execStr.substring(lastIndex));

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
			
			if(OSValidator.isUnix()) {
				Runnable setSystemTimeTask = () -> {
					this.setSystemTime();
				};
				executor.scheduleAtFixedRate(setSystemTimeTask, synchro, resync, TimeUnit.SECONDS);
			}
			
			state = ServiceState.STARTED;
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
			}
			
			state = ServiceState.STOPPED;
		}
	}
}
