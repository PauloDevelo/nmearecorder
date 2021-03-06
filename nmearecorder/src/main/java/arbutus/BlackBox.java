package arbutus;

import org.apache.log4j.Logger;

import arbutus.influxdb.IInfluxdbRepository;
import arbutus.influxdb.InfluxdbRepository;
import arbutus.nmea.service.INMEAService;
import arbutus.nmea.service.NMEAService;
import arbutus.rtmodel.Vessel;
import arbutus.service.ServiceManager;
import arbutus.timeservice.ITimeService;
import arbutus.timeservice.TimeService;


public class BlackBox {
	private static Logger log = Logger.getLogger(BlackBox.class);
	
	private Vessel arbutus = null;
	
	public static void main(String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread()
        {
			private Thread currentThread = Thread.currentThread();
			
            @Override
            public void run()
            {
                log.debug("Shutdown NMEARecorder");
                currentThread.interrupt();
            }
        });
		
		BlackBox blackBox = new BlackBox();
		blackBox.run();
	}
	
	private synchronized void run() {
		int exitStatus = 0;
		
		try {
			ServiceManager srvMgr = ServiceManager.getInstance();
			
			srvMgr.register(INMEAService.class, new NMEAService());
			srvMgr.register(ITimeService.class, new TimeService());
			srvMgr.register(IInfluxdbRepository.class, new InfluxdbRepository());
			
			srvMgr.startServices();
			
			arbutus = new Vessel();
			
			while (!Thread.interrupted()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					log.debug("Interruption received");
				}
			}
		}
		catch(Exception ex) {
			log.fatal("unexpected error caught at the last minute", ex);
			exitStatus = 1;
		}
		finally {
			if(arbutus != null)
				arbutus.unsubscribe();

			ServiceManager.getInstance().stopServices();
			log.debug("Services stopped");
			
			System.exit(exitStatus);
		}
	}
}
