package arbutus.influxdb;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import arbutus.service.ServiceManager;
import arbutus.timeservice.ITimeService;
import arbutus.timeservice.SynchronizationException;

public class InfluxTask<T> extends TimerTask{
	private static Logger log = Logger.getLogger(InfluxTask.class);
	
	private ITimeService timeService = null;
	private IInfluxdbRepository repository = null;
	
	private Class<T> type = null;
	private T instance = null;
	private String measurementName = null;
	
	public InfluxTask(String measurementName, Class<T> type, T instance) {
		this.type = type;
		this.instance = instance;
		this.measurementName = measurementName;
		
		ServiceManager srvMgr = ServiceManager.getInstance();
		
		timeService = srvMgr.getService(ITimeService.class);
		repository = srvMgr.getService(IInfluxdbRepository.class);
	}
	
	@Override
	public void run() {
		if(timeService.isSynchonized()) {
			HashMap<String, Float> influxDbFields = new HashMap<>();
			
			for(Field field : type.getDeclaredFields())
			{
				addInfluxField(influxDbFields, field);
			}
			
			try {
				repository.addPoint(measurementName, timeService.getUTCDateTime(), influxDbFields);
			}
			catch(SynchronizationException ex) {
				log.error(ex);
			}
		}
	}
	
	private void addInfluxField(HashMap<String, Float> influxFields, Field f) {
		try {
			f.setAccessible(true);
			
			InfluxField influxAnnotation = f.getAnnotation(InfluxField.class);
			if(influxAnnotation != null) {
				
				try {
					Method getter = type.getDeclaredMethod(getGetterName(f));
					Float value = Float.class.cast(getter.invoke(instance));
					
					if( value != null && !Float.isNaN(value)) {
						influxFields.put(influxAnnotation.name(), value);
					}
				} catch (NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
					log.error("Error when adding a field for influx", e);
				} 
			}
		}
		catch(IllegalAccessException ex){
			log.error("Error in addInfluxField", ex);
		}
	}

	private String getGetterName(Field f) {
		StringBuilder getterName = new StringBuilder("get");
		getterName.append(Character.toUpperCase(f.getName().charAt(0)));
		getterName.append(f.getName().substring(1));
		
		return getterName.toString();
	}
}