package arbutus.timeservice;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
			String execStr = properties.getValue("exec");
			int firstIndex = execStr.indexOf('"');
			int lastIndex = execStr.lastIndexOf('"');
			
			SimpleDateFormat df = new SimpleDateFormat(execStr.substring(firstIndex + 1, lastIndex));
			df.setTimeZone(TimeZone.getTimeZone("UTC"));
			
			StringBuilder exec = new StringBuilder();
			exec.append(execStr.subSequence(0, firstIndex + 1)).append(df.format(this.getUTCDateTime())).append(execStr.substring(lastIndex));		
			
			
			log.info("exec " + exec.toString());
			
			//Process subProcess = Runtime.getRuntime().exec(exec.toString());
			Process subProcess = new ProcessBuilder().command("bash", "-c", exec.toString()).start();
			subProcess.waitFor();
			
			String s;
            BufferedReader br = new BufferedReader(new InputStreamReader(subProcess.getInputStream()));
            while ((s = br.readLine()) != null)
                log.info(s);

            log.info("exit: " + subProcess.exitValue());
            subProcess.destroy();
		} catch (Exception e) {
			log.error("Error when synchronizing the system time", e);
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
			
			Runnable setSystemTimeTask = () -> {
				this.setSystemTime();
			};
			executor.scheduleAtFixedRate(setSystemTimeTask, synchro, resync, TimeUnit.SECONDS);
			
			state = ServiceState.STARTED;
		}
	}

	@Override
	public void stop() {
		if(state == ServiceState.STARTED) {
			INMEAService nmeaService = ServiceManager.getInstance().getService(INMEAService.class);
			nmeaService.unsubscribe(GPRMC.class, this);
			
			executor.shutdown();
			try {
			    if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
			    	executor.shutdownNow();
			    } 
			} catch (InterruptedException e) {
				executor.shutdownNow();
			}
			
			state = ServiceState.STOPPED;
		}
	}
}
