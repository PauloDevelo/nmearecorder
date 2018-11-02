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

public class VirtuinoService implements IService, IVirtuinoService{
	private static Logger log = Logger.getLogger(VirtuinoService.class);
	
	private final HashMap<String, VirtuinoConnector> connectors = new HashMap<>();
	
	private ServiceState state = ServiceState.STOPPED;

	public VirtuinoService() {
	}

	@Override
	public ServiceState getState() {
		return state;
	}

	@Override
	public void start() throws Exception {
		if(this.state == ServiceState.STOPPED) {
			for(VirtuinoConnector connector : this.connectors.values()) {
				connector.startProcess();
			}
			
			this.state = ServiceState.STARTED;
			
			log.info("Virtuino service started");
		}
		else {
			throw new VirtuinoServiceException("Virtuino service is already started.");
		}
	}

	@Override
	public void stop() {
		if(this.state == ServiceState.STARTED) {
			for(String connectorKey : this.connectors.keySet()) {
				VirtuinoConnector connector = this.connectors.get(connectorKey);
				connector.stopProcess();
			}
			
			this.state = ServiceState.STOPPED;
			log.info("Virtuino service stopped");
		}
		else {
			log.warn("Virtuino service already stopped.");
		}
	}

	@Override
	public float getVirtualFloat(String connectorKey, int pin) {
		if(!this.connectors.containsKey(connectorKey)) {
			log.error("The connector " + connectorKey + " have not been added in the Virtuino service.");
			return Float.NaN;
		}
		
		try {
			return this.connectors.get(connectorKey).getSyncVirtualFloat(pin);
		} catch (VirtuinoConnectorException e) {
			log.error("Error when getting a value in virtuino service " + connectorKey + ".", e);
			return Float.NaN;
		}
	}

	@Override
	public void subscribe(String connectorKey, VirtuinoCommandType type, int pin, BiConsumer<Long, Float> consumer) throws VirtuinoServiceException{
		if(!this.connectors.containsKey(connectorKey)) {
			throw new VirtuinoServiceException("The connector " + connectorKey + " have not been added in the Virtuino service.");
		}
		
		this.connectors.get(connectorKey).subscribe(new VirtuinoItem(type, pin), consumer);
	}

	@Override
	public void unsubscribe(String connectorKey, VirtuinoCommandType type, int pin, BiConsumer<Long, Float> consumer) throws VirtuinoServiceException {
		if(!this.connectors.containsKey(connectorKey)) {
			throw new VirtuinoServiceException("The connector " + connectorKey + " have not been added in the Virtuino service.");
		}
		
		this.connectors.get(connectorKey).unsubscribe(new VirtuinoItem(type, pin), consumer);	
	}

	@Override
	public void addVirtuinoConnector(String connectorKey, VirtuinoConnector connector) throws VirtuinoServiceException {
		if(this.state == ServiceState.STARTED) {
			throw new VirtuinoServiceException("It is not possible to add the connector " + connectorKey + " because the service is started.");
		}
		else {
			if(!this.connectors.containsKey(connectorKey)) {
				this.connectors.put(connectorKey, connector);
			}
			else {
				throw new VirtuinoServiceException("A connector " + connectorKey + " have already been added.");
			}
		}
	}
}
