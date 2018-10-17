package arbutus.influxdb.measurement;

import java.io.InvalidClassException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import arbutus.influxdb.IInfluxdbRepository;
import arbutus.service.ServiceManager;

public abstract class InfluxMeasurement<T> extends Measurement{
	private final static Logger log = Logger.getLogger(InfluxMeasurement.class);
	
	private final IInfluxdbRepository repository;
	
	private final T instance;
	private final String measurementName;
	
	private final HashMap<InfluxFieldAnnotation, Method> mapInfluxFieldGetter = new HashMap<InfluxFieldAnnotation, Method>();
	
	public InfluxMeasurement(Class<T> type) throws InvalidClassException, ClassCastException{
		ServiceManager srvMgr = ServiceManager.getInstance();
		
		this.repository = srvMgr.getService(IInfluxdbRepository.class);
		
		this.instance = type.cast(this);
		
		InfluxMeasurementAnnotation influxMeasurement = type.getAnnotation(InfluxMeasurementAnnotation.class);
		if(influxMeasurement == null) {
			throw new java.io.InvalidClassException(type.getName() + " should contain an annotation of " + InfluxMeasurementAnnotation.class.getSimpleName() + " type.");
		}
		else {
			this.measurementName = influxMeasurement.name();
		}
		
		this.initMeasurement(type);
	}
	
	protected final void write() {
		Date dataDateTime = this.getDataUTCDateTime();
		if(dataDateTime != null) {
			HashMap<String, Float> mapInfluxFieldValue = new HashMap<>();
			
			for(InfluxFieldAnnotation influxField : this.mapInfluxFieldGetter.keySet())
			{
				try {
					Float value = Float.class.cast(this.mapInfluxFieldGetter.get(influxField).invoke(instance));
					
					if( value != null && !Float.isNaN(value)) {
						mapInfluxFieldValue.put(influxField.name(), value);
					}
					
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					log.error("Error when accessing a field for influx", e);
				} 
			}
			
			if(!mapInfluxFieldValue.isEmpty()) {
				this.repository.addPoint(this.measurementName, this.getDataUTCDateTime(), mapInfluxFieldValue);
			}
		}
	}
	
	/**
	 * Function which has to be called in the constructor of the sub-class in order to initiate parameters for influxDB.
	 * @param type
	 * @param instance
	 */
	private final void initMeasurement(Class<T> type) {
		for(Field field : type.getDeclaredFields())
		{
			try {
				field.setAccessible(true);
				
				InfluxFieldAnnotation influxAnnotation = field.getAnnotation(InfluxFieldAnnotation.class);
				if(influxAnnotation != null) {
					Method getter = type.getDeclaredMethod(getGetterName(field));
					this.mapInfluxFieldGetter.put(influxAnnotation, getter);
				}
			} catch (NoSuchMethodException | SecurityException e) {
				log.error("Error when adding a field for influxDB for the type " + type.getName() + ": the public getter " + getGetterName(field) + " does not exist or is private.", e);
			} 
		}
	}

	private final String getGetterName(Field f) {
		StringBuilder getterName = new StringBuilder("get");
		getterName.append(Character.toUpperCase(f.getName().charAt(0)));
		getterName.append(f.getName().substring(1));
		
		return getterName.toString();
	}
}
