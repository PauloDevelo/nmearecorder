package arbutus.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;


public class ServiceManager {
	private final static Logger log = Logger.getLogger(ServiceManager.class);
	private static ServiceManager _instance = null;
	
	public static ServiceManager getInstance() {
		if(_instance == null) {
			_instance = new ServiceManager();
		}
		
		return _instance;
	}
	
	private final HashMap<Object, Object> _services = new HashMap<>();
	private final List<Object> _orderedServices = new ArrayList<>();
	
	private ServiceManager() {
		
	}
	
	public <K, T extends K> boolean register(Class<K> key, T serviceInstance) {
		if(!(serviceInstance instanceof IService)) {
			log.error("The instance of your service should implement " + IService.class.getName());
			return false;
		}
		
		if(_services.containsKey(key)) {
			log.error("The service is already registered.");
			return false;
		}
		else {
			_services.put(key, serviceInstance);
			_orderedServices.add(key);
			return true;
		}
	}
	
	public <K> K unregister(Class<K> key) {
		_orderedServices.remove(key);
		return key.cast(_services.remove(key));
	}
	
	public <K> K getService(Class<K> myInterface) {
		return myInterface.cast(_services.get(myInterface));
	}
	
	public void startServices() throws Exception{
		for(Object key : _orderedServices) {
			Object service = _services.get(key);
			
			try {
				IService.class.cast(service).start();
			}
			catch(Exception ex) {
				log.fatal("A fatal error occured when starting the service " + service.getClass().getName(), ex);
				stopServices();
				
				throw ex;
			}
		}
	}
	
	public void stopServices() {
		for(int i = _orderedServices.size() - 1; i >= 0; i--) {
			Object service = _services.get(_orderedServices.get(i));
			
			try {
				IService.class.cast(service).stop();
			}
			catch(Exception ex) {
				log.error("An exception was thrown when stopping the service " + service.getClass().getName() + ".", ex);
			}
		}
	}
}
