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
	
	private T instance = null;
	private String measurementName = null;
	
	private HashMap<InfluxField, Method> mapInfluxFieldGetter = new HashMap<InfluxField, Method>();
	
	public InfluxTask(String measurementName, Class<T> type, T instance) {
		this.instance = instance;
		this.measurementName = measurementName;
		
		ServiceManager srvMgr = ServiceManager.getInstance();
		
		timeService = srvMgr.getService(ITimeService.class);
		repository = srvMgr.getService(IInfluxdbRepository.class);
		
		for(Field field : type.getDeclaredFields())
		{
			try {
				field.setAccessible(true);
				
				InfluxField influxAnnotation = field.getAnnotation(InfluxField.class);
				if(influxAnnotation != null) {
					Method getter = type.getDeclaredMethod(getGetterName(field));
					mapInfluxFieldGetter.put(influxAnnotation, getter);
				}
			} catch (NoSuchMethodException | SecurityException e) {
				log.error("Error when adding a field for influx", e);
			} 
		}
	}
	
	@Override
	public void run() {
		if(timeService.isSynchonized()) {
			HashMap<String, Float> mapInfluxFieldValue = new HashMap<>();
			
			for(InfluxField influxField : mapInfluxFieldGetter.keySet())
			{
				try {
					Float value = Float.class.cast(mapInfluxFieldGetter.get(influxField).invoke(instance));
					
					if( value != null && !Float.isNaN(value)) {
						mapInfluxFieldValue.put(influxField.name(), value);
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					log.error("Error when accessing a field for influx", e);
				} 
			}
			
			try {
				repository.addPoint(measurementName, timeService.getUTCDateTime(), mapInfluxFieldValue);
			}
			catch(SynchronizationException ex) {
				log.error(ex);
			}
		}
	}

	private String getGetterName(Field f) {
		StringBuilder getterName = new StringBuilder("get");
		getterName.append(Character.toUpperCase(f.getName().charAt(0)));
		getterName.append(f.getName().substring(1));
		
		return getterName.toString();
	}
}