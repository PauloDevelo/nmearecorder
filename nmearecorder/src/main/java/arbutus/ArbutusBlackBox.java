package arbutus;

import org.apache.log4j.Logger;

import arbutus.influxdb.IInfluxdbRepository;
import arbutus.influxdb.InfluxdbRepository;
import arbutus.nmea.service.INMEAService;
import arbutus.nmea.service.NMEAService;
import arbutus.rtmodel.Arbutus;
import arbutus.service.ServiceManager;
import arbutus.timeservice.ITimeService;
import arbutus.timeservice.SynchronizationException;
import arbutus.timeservice.TimeService;


public class ArbutusBlackBox {
	private static Logger log = Logger.getLogger(ArbutusBlackBox.class);
	
	private Arbutus arbutus = new Arbutus();
	
	private static boolean turnOff = false;
	
	public synchronized boolean isTurnedOff() {
		return turnOff;
	}
	
	public static synchronized void turnOff() {
		turnOff = true;
	}
	
	public static void main(String[] args) {
		ServiceManager srvMgr = ServiceManager.getInstance();
		
		srvMgr.register(INMEAService.class, new NMEAService());
		srvMgr.register(ITimeService.class, new TimeService());
		srvMgr.register(IInfluxdbRepository.class, new InfluxdbRepository());
		
		srvMgr.startServices();
		
		Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                log.debug("Shutdown NMEARecorder");
                ArbutusBlackBox.turnOff();
                
                
            }
        });
		
		ArbutusBlackBox blackBox = new ArbutusBlackBox();
		blackBox.waitMethod();
	}
	
	private synchronized void waitMethod() {
		ITimeService timeService = ServiceManager.getInstance().getService(ITimeService.class);
		while (!isTurnedOff()) {
			try {
				this.wait(2000);
			} catch (InterruptedException e) {
				log.error(e);
			}
		}
		
		arbutus.unsubscribe();
		
		System.exit(0);
        
		ServiceManager.getInstance().stopServices();
		log.debug("Services stopped");
	}
}
