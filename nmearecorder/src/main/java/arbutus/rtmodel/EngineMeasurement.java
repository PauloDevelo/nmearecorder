package arbutus.rtmodel;

import java.io.InvalidClassException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import arbutus.influxdb.measurement.IMeasurementListener;
import arbutus.influxdb.measurement.InfluxFieldAnnotation;
import arbutus.influxdb.measurement.InfluxMeasurement;
import arbutus.influxdb.measurement.InfluxMeasurementAnnotation;
import arbutus.influxdb.measurement.Measurement;
import arbutus.service.ServiceManager;
import arbutus.timeservice.SynchronizationException;
import arbutus.virtuino.connectors.VirtuinoCommandType;
import arbutus.virtuino.service.IVirtuinoService;
import arbutus.virtuino.service.VirtuinoServiceType;

@InfluxMeasurementAnnotation(name="Engine")
public class EngineMeasurement extends InfluxMeasurement<EngineMeasurement> implements IMeasurementListener {
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
	
	@InfluxFieldAnnotation(name="consumptionPer100nm")
	private float consumptionPer100nm = Float.NaN;

	private ArrayList<EngineMeasurementType> listExpectedValue = new ArrayList<>();

	private final GPSMeasurement gps;

	public EngineMeasurement(GPSMeasurement gps) throws InvalidClassException, ClassCastException {
		super(EngineMeasurement.class);
		
		this.initExpectedValue();
		
		IVirtuinoService virtuinoService = ServiceManager.getInstance().getService(IVirtuinoService.class);
		for(EngineMeasurementType measurement : EngineMeasurementType.values()){
			switch(measurement) {
			case AGE:
				virtuinoService.subscribe(VirtuinoServiceType.Engine, VirtuinoCommandType.VirtualFloat, measurement.getPin(), this::setAge);
				break;
			case BAT_VOLTAGE:
				virtuinoService.subscribe(VirtuinoServiceType.Engine, VirtuinoCommandType.VirtualFloat, measurement.getPin(), this::setBatVoltage);
				break;
			case CONSO:
				virtuinoService.subscribe(VirtuinoServiceType.Engine, VirtuinoCommandType.VirtualFloat, measurement.getPin(), this::setConsumption);
				break;
			case DIESEL_VOL:
				virtuinoService.subscribe(VirtuinoServiceType.Engine, VirtuinoCommandType.VirtualFloat, measurement.getPin(), this::setDieselQty);
				break;
			case RPM:
				virtuinoService.subscribe(VirtuinoServiceType.Engine, VirtuinoCommandType.VirtualFloat, measurement.getPin(), this::setRpm);
				break;
			case TEMP_COOLANT:
				virtuinoService.subscribe(VirtuinoServiceType.Engine, VirtuinoCommandType.VirtualFloat, measurement.getPin(), this::setCoolantTemp);
				break;
			case TEMP_EXHAUST:
				virtuinoService.subscribe(VirtuinoServiceType.Engine, VirtuinoCommandType.VirtualFloat, measurement.getPin(), this::setExhaustTemp);
				break;
			default:
				log.warn("The engine measurement " + measurement + " is not in the EngineMeasurementType");
				break;
			}
		}
		
		this.gps = gps;
		this.gps.addListener(this);
	}

	private void initExpectedValue() {
		this.listExpectedValue.clear();
		
		for(EngineMeasurementType measurement : EngineMeasurementType.values()) {
			this.listExpectedValue.add(measurement);
		}
	}
	
	/**
	 * @return the consumptionPer100nm
	 */
	public synchronized float getConsumptionPer100nm() {
		return consumptionPer100nm;
	}

	/**
	 * @param consumptionPer100nm the consumptionPer100nm to set
	 */
	public synchronized void setConsumptionPer100nm(float consumptionPer100nm) {
		this.consumptionPer100nm = consumptionPer100nm;
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
		this.onNewValue(nano, EngineMeasurementType.AGE);
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
		this.onNewValue(nano, EngineMeasurementType.RPM);
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
		computeConsumptionPer100Nm();
		
		this.onNewValue(nano, EngineMeasurementType.CONSO);
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
		this.onNewValue(nano, EngineMeasurementType.DIESEL_VOL);
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
		this.onNewValue(nano, EngineMeasurementType.TEMP_EXHAUST);
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
		this.onNewValue(nano, EngineMeasurementType.BAT_VOLTAGE);
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
		this.onNewValue(nano, EngineMeasurementType.TEMP_COOLANT);
	}
	
	@Override
	public void onMeasurementChanged(Measurement measurement) {
		computeConsumptionPer100Nm();
	}

	private void onNewValue(long nano, EngineMeasurementType measurement) {
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

	private void computeConsumptionPer100Nm() {
		if(!Float.isNaN(this.gps.getSog()) && !Float.isNaN(this.getConsumption())) {
			if(this.gps.getSog() != 0) {
				float timeFor100NM = 100f / this.gps.getSog();
				this.setConsumptionPer100nm(this.getConsumption() * timeFor100NM);
			}
			else {
				this.setConsumptionPer100nm(Float.POSITIVE_INFINITY);
			}
		}
		else {
			this.setConsumptionPer100nm(Float.NaN);
		}
	}
	
}
