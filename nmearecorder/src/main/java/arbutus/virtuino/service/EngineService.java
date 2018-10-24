package arbutus.virtuino.service;

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.apache.log4j.Logger;

import arbutus.service.IService;
import arbutus.service.ServiceState;
import arbutus.virtuino.connectors.VirtuinoCommandType;
import arbutus.virtuino.connectors.VirtuinoConnector;
import arbutus.virtuino.connectors.VirtuinoConnectorException;
import arbutus.virtuino.connectors.VirtuinoItem;
import arbutus.virtuino.connectors.serial.SerialVirtuinoConnector;
import arbutus.virtuino.connectors.serial.SerialVirtuinoContext;

public class EngineService implements IService, IEngineService{
	private static Logger log = Logger.getLogger(EngineService.class);
	
	private final VirtuinoConnector connector;
	private Thread threadConnector;

	public EngineService() {
		this.connector = new SerialVirtuinoConnector(new SerialVirtuinoContext());
	}

	@Override
	public ServiceState getState() {
		if(threadConnector != null && threadConnector.isAlive())
			return ServiceState.STARTED;
		else
			return ServiceState.STOPPED;
	}

	@Override
	public void start() throws Exception {
		this.threadConnector = new Thread(this.connector);
		this.threadConnector.start();
		
		log.info("Engine service started");
	}

	@Override
	public void stop() {
		if (this.threadConnector != null && this.threadConnector.isAlive()) {
			this.connector.interrupt();
			
			try {
				int nbMilli = 0;
				while(nbMilli < this.connector.getContext().scanRateInMilliSec && this.threadConnector.isAlive()) {
					TimeUnit.MILLISECONDS.sleep(100);
					nbMilli += 100;
				}
				
				if(this.threadConnector.isAlive()) {
					this.threadConnector.interrupt();
					TimeUnit.MILLISECONDS.sleep(100);
				}
			} catch (InterruptedException e) {
				this.threadConnector.interrupt();
			}
		}
		
		if(!this.threadConnector.isAlive()) {
			this.threadConnector = null;
			log.info("Engine service stopped");
		}
		else {
			log.error("Engine service could not stopped");
		}
	}

	@Override
	public float getValue(EngineMeasurement measurement) {
		try {
			return this.connector.getSyncVirtualFloat(measurement.getPin());
		} catch (VirtuinoConnectorException e) {
			log.error("Error when getting a value in engine service.", e);
			return Float.NaN;
		}
	}

	@Override
	public void subscribe(EngineMeasurement measurement, BiConsumer<Long, Float> consumer) {
		this.connector.subscribe(new VirtuinoItem(VirtuinoCommandType.VirtualFloat, measurement.getPin()), consumer);
	}

	@Override
	public void unsubscribe(EngineMeasurement measurement, BiConsumer<Long, Float> consumer) {
		this.connector.unsubscribe(new VirtuinoItem(VirtuinoCommandType.VirtualFloat, measurement.getPin()), consumer);	
	}
}
