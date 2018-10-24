package arbutus.rtmodel;

import java.io.InvalidClassException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import arbutus.influxdb.measurement.InfluxFieldAnnotation;
import arbutus.influxdb.measurement.InfluxMeasurement;
import arbutus.influxdb.measurement.InfluxMeasurementAnnotation;
import arbutus.service.ServiceManager;
import arbutus.timeservice.SynchronizationException;
import arbutus.virtuino.service.IEngineService;

@InfluxMeasurementAnnotation(name="Engine")
public class EngineMeasurement extends InfluxMeasurement<EngineMeasurement> {
	private final static Logger log = Logger.getLogger(EngineMeasurement.class);

	
	//#define AGE_ENGINE_INDEX	0
	//#define RPM_INDEX			1
	//#define CONSO_INDEX			2
	//#define QTE_GAZ_INDEX		3
	//#define TEMP_INDEX			4
	//#define VOLTAGE_INDEX		5
	//#define TEMP_COOLANT_INDEX	6
	@InfluxFieldAnnotation(name="ageInHour")
	private float age = Float.NaN;
	
	@InfluxFieldAnnotation(name="rpm")
	private float rpm = Float.NaN;
	
	@InfluxFieldAnnotation(name="consumption")
	private float consumption = Float.NaN;
	
	@InfluxFieldAnnotation(name="dieselQty")
	private float dieselQty = Float.NaN;
	
	@InfluxFieldAnnotation(name="exhaustTemp")
	private float exhaustTemp = Float.NaN;
	
	@InfluxFieldAnnotation(name="batVoltage")
	private float batVoltage = Float.NaN;
	
	@InfluxFieldAnnotation(name="coolantTemp")
	private float coolantTemp = Float.NaN;
	
	private ArrayList<arbutus.virtuino.service.EngineMeasurement> listExpectedValue = new ArrayList<>();

	public EngineMeasurement() throws InvalidClassException, ClassCastException {
		super(EngineMeasurement.class);
		
		this.initExpectedValue();
		
		IEngineService engineService = ServiceManager.getInstance().getService(IEngineService.class);
		for(arbutus.virtuino.service.EngineMeasurement measurement : arbutus.virtuino.service.EngineMeasurement.values()){
			switch(measurement) {
			case AGE:
				engineService.subscribe(measurement, this::setAge);
				break;
			case BAT_VOLTAGE:
				engineService.subscribe(measurement, this::setBatVoltage);
				break;
			case CONSO:
				engineService.subscribe(measurement, this::setConsumption);
				break;
			case DIESEL_VOL:
				engineService.subscribe(measurement, this::setDieselQty);
				break;
			case RPM:
				engineService.subscribe(measurement, this::setRpm);
				break;
			case TEMP_COOLANT:
				engineService.subscribe(measurement, this::setCoolantTemp);
				break;
			case TEMP_EXHAUST:
				engineService.subscribe(measurement, this::setExhaustTemp);
				break;
			default:
				log.warn("The engine measurement " + measurement + " is not in the EngineMeasurement");
				break;
			}
		}
	}

	private void initExpectedValue() {
		this.listExpectedValue.clear();
		
		for(arbutus.virtuino.service.EngineMeasurement measurement : arbutus.virtuino.service.EngineMeasurement.values()) {
			this.listExpectedValue.add(measurement);
		}
	}

	/**
	 * @return the age
	 */
	public synchronized float getAge() {
		return age;
	}

	/**
	 * @param age the age to set
	 */
	public synchronized void setAge(Long nano, Float age) {
		this.age = age;
		this.onNewValue(nano, arbutus.virtuino.service.EngineMeasurement.AGE);
	}

	/**
	 * @return the rpm
	 */
	public synchronized float getRpm() {
		return rpm;
	}

	/**
	 * @param rpm the rpm to set
	 */
	public synchronized void setRpm(Long nano, Float rpm) {
		this.rpm = rpm;
		this.onNewValue(nano, arbutus.virtuino.service.EngineMeasurement.RPM);
	}

	/**
	 * @return the consumption
	 */
	public synchronized float getConsumption() {
		return consumption;
	}

	/**
	 * @param consumption the consumption to set
	 */
	public synchronized void setConsumption(Long nano, Float consumption) {
		this.consumption = consumption;
		this.onNewValue(nano, arbutus.virtuino.service.EngineMeasurement.CONSO);
	}

	/**
	 * @return the dieselQty
	 */
	public synchronized float getDieselQty() {
		return dieselQty;
	}

	/**
	 * @param dieselQty the dieselQty to set
	 */
	public synchronized void setDieselQty(Long nano, Float dieselQty) {
		this.dieselQty = dieselQty;
		this.onNewValue(nano, arbutus.virtuino.service.EngineMeasurement.DIESEL_VOL);
	}

	/**
	 * @return the exhaustTemp
	 */
	public synchronized float getExhaustTemp() {
		return exhaustTemp;
	}

	/**
	 * @param exhaustTemp the exhaustTemp to set
	 */
	public synchronized void setExhaustTemp(Long nano, Float exhaustTemp) {
		this.exhaustTemp = exhaustTemp;
		this.onNewValue(nano, arbutus.virtuino.service.EngineMeasurement.TEMP_EXHAUST);
	}

	/**
	 * @return the batVoltage
	 */
	public synchronized float getBatVoltage() {
		return batVoltage;
	}

	/**
	 * @param batVoltage the batVoltage to set
	 */
	public synchronized void setBatVoltage(Long nano, Float batVoltage) {
		this.batVoltage = batVoltage;
		this.onNewValue(nano, arbutus.virtuino.service.EngineMeasurement.BAT_VOLTAGE);
	}

	/**
	 * @return the coolantTemp
	 */
	public synchronized float getCoolantTemp() {
		return coolantTemp;
	}

	/**
	 * @param coolantTemp the coolantTemp to set
	 */
	public synchronized void setCoolantTemp(Long nano, Float coolantTemp) {
		this.coolantTemp = coolantTemp;
		this.onNewValue(nano, arbutus.virtuino.service.EngineMeasurement.TEMP_COOLANT);
	}

	private void onNewValue(long nano, arbutus.virtuino.service.EngineMeasurement measurement) {
		this.listExpectedValue.remove(measurement);
		
		if(this.listExpectedValue.isEmpty()) {
			if(this.timeService.isSynchonized()) {
				try {
					this.setDataUTCDateTime(this.timeService.getUTCDateTime(nano));
					this.write();
					this.fireMeasurementChanged();
					
				} 
				catch (SynchronizationException e) {
				}
			}
			this.initExpectedValue();
		}
		
	}
	
}
