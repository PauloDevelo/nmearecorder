package arbutus.virtuino.service;

import java.util.HashMap;
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

public class VirtuinoService implements IService, IVirtuinoService{
	private static Logger log = Logger.getLogger(VirtuinoService.class);
	
	private final HashMap<VirtuinoServiceType, VirtuinoConnector> connectors = new HashMap<>();
	
	private ServiceState state = ServiceState.STOPPED;

	public VirtuinoService() {
		VirtuinoConnector enginemonitor = new SerialVirtuinoConnector(new SerialVirtuinoContext(VirtuinoServiceType.Engine.getVal()));
		this.connectors.put(VirtuinoServiceType.Engine, enginemonitor);
		
		VirtuinoConnector pirmonitor = new SerialVirtuinoConnector(new SerialVirtuinoContext(VirtuinoServiceType.PIR.getVal()));
		this.connectors.put(VirtuinoServiceType.PIR, pirmonitor);
	}

	@Override
	public ServiceState getState() {
		return state;
	}

	@Override
	public void start() throws Exception {
		for(VirtuinoConnector connector : this.connectors.values()) {
			connector.setThreadConnector(new Thread(connector));
			connector.getThreadConnector().start();
		}
		
		state = ServiceState.STARTED;
		
		log.info("Virtuino service started");
	}

	@Override
	public void stop() {
		for(VirtuinoServiceType connectorType : this.connectors.keySet()) {
			VirtuinoConnector connector = this.connectors.get(connectorType);
			if (connector.getThreadConnector() != null && connector.getThreadConnector().isAlive()) {
				connector.interrupt();
				
				try {
					int nbMilli = 0;
					while(nbMilli < connector.getContext().getScanRateInMilliSec() && connector.getThreadConnector().isAlive()) {
						TimeUnit.MILLISECONDS.sleep(100);
						nbMilli += 100;
					}
					
					if(connector.getThreadConnector().isAlive()) {
						connector.getThreadConnector().interrupt();
						TimeUnit.MILLISECONDS.sleep(100);
					}
				} catch (InterruptedException e) {
					connector.getThreadConnector().interrupt();
				}
			}
			
			if(!connector.getThreadConnector().isAlive()) {
				connector.setThreadConnector(null);
			}
			else {
				log.error("The Virtuino connector " + connectorType.toString() + " could not stopped");
			}
		}
		
		this.state = ServiceState.STOPPED;
		log.info("Virtuino service stopped");
	}

	@Override
	public float getVirtualFloat(VirtuinoServiceType connectorId, int pin) {
		try {
			return this.connectors.get(connectorId).getSyncVirtualFloat(pin);
		} catch (VirtuinoConnectorException e) {
			log.error("Error when getting a value in virtuino service " + connectorId.toString() + ".", e);
			return Float.NaN;
		}
	}

	@Override
	public void subscribe(VirtuinoServiceType connectorId, VirtuinoCommandType type, int pin, BiConsumer<Long, Float> consumer) {
		this.connectors.get(connectorId).subscribe(new VirtuinoItem(type, pin), consumer);
	}

	@Override
	public void unsubscribe(VirtuinoServiceType connectorId, VirtuinoCommandType type, int pin, BiConsumer<Long, Float> consumer) {
		this.connectors.get(connectorId).unsubscribe(new VirtuinoItem(type, pin), consumer);	
	}
}
