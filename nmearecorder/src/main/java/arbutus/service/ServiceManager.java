package arbutus.service;

import java.util.HashMap;

import org.apache.log4j.Logger;


public class ServiceManager {
	private static Logger log = Logger.getLogger(ServiceManager.class);
	private static ServiceManager _instance = null;
	
	public static ServiceManager getInstance() {
		if(_instance == null) {
			_instance = new ServiceManager();
		}
		
		return _instance;
	}
	
	private HashMap<Object, Object> _services = new HashMap<>();
	
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
			return true;
		}
	}
	
	public <K> K unregister(Class<K> key) {
		return key.cast(_services.remove(key));
	}
	
	public <K> K getService(Class<K> myInterface) {
		return myInterface.cast(_services.get(myInterface));
	}
	
	public void startServices() {
		for(Object service : _services.values()) {
			IService.class.cast(service).start();
		}
	}
	
	public void stopServices() {
		for(Object service : _services.values()) {
			IService.class.cast(service).stop();
		}
	}
}
