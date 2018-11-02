package arbutus;

import org.apache.log4j.Logger;

import arbutus.influxdb.IInfluxdbRepository;
import arbutus.influxdb.InfluxdbContext;
import arbutus.influxdb.InfluxdbRepository;
import arbutus.nmea.service.INMEAService;
import arbutus.nmea.service.NMEAService;
import arbutus.nmea.service.connectors.TCPReader;
import arbutus.rtmodel.Vessel;
import arbutus.rtmodel.VirtuinoServiceType;
import arbutus.service.ServiceManager;
import arbutus.timeservice.ITimeService;
import arbutus.timeservice.TimeService;
import arbutus.virtuino.service.VirtuinoService;
import arbutus.virtuino.connectors.VirtuinoConnector;
import arbutus.virtuino.connectors.serial.SerialVirtuinoConnector;
import arbutus.virtuino.connectors.serial.SerialVirtuinoContext;
import arbutus.virtuino.service.IVirtuinoService;


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
			
			srvMgr.register(INMEAService.class, new NMEAService(TCPReader.class));
			srvMgr.register(ITimeService.class, new TimeService());
			
			srvMgr.register(IInfluxdbRepository.class, new InfluxdbRepository(new InfluxdbContext()));
			srvMgr.register(IVirtuinoService.class, new VirtuinoService());
			
			IVirtuinoService virtuinoService = srvMgr.getService(IVirtuinoService.class);
			VirtuinoConnector enginemonitor = new SerialVirtuinoConnector(new SerialVirtuinoContext(VirtuinoServiceType.Engine.getVal()));
			virtuinoService.addVirtuinoConnector(VirtuinoServiceType.Engine.getVal(), enginemonitor);
			
			VirtuinoConnector pirmonitor = new SerialVirtuinoConnector(new SerialVirtuinoContext(VirtuinoServiceType.PIR.getVal()));
			virtuinoService.addVirtuinoConnector(VirtuinoServiceType.PIR.getVal(), pirmonitor);
			
			srvMgr.startServices();
			
			arbutus = new Vessel();
			log.info("NMEARecorder service started");
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
			log.info("NMEARecorder service stopped");
			
			System.exit(exitStatus);
		}
	}
}
