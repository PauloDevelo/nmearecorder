package arbutus.rtmodel;

import java.io.InvalidClassException;

import org.apache.log4j.Logger;

import arbutus.influxdb.measurement.InfluxFieldAnnotation;
import arbutus.influxdb.measurement.InfluxMeasurement;
import arbutus.influxdb.measurement.InfluxMeasurementAnnotation;
import arbutus.service.ServiceManager;
import arbutus.timeservice.SynchronizationException;
import arbutus.virtuino.connectors.VirtuinoCommandType;
import arbutus.virtuino.service.IVirtuinoService;
import arbutus.virtuino.service.VirtuinoServiceException;
import arbutus.virtuino.service.VirtuinoServiceType;

@InfluxMeasurementAnnotation(name="PIR")
public class PIRMeasurement extends InfluxMeasurement<PIRMeasurement> {
	private static Logger log = Logger.getLogger(PIRMeasurement.class);
	private static final long DELAY_IN_NANO = 60000000000L;

	@InfluxFieldAnnotation(name="pir")
	private float pir = Float.NaN;
	
	private long lastMeasurement;
	
	
	public PIRMeasurement() throws InvalidClassException, ClassCastException {
		super(PIRMeasurement.class);
		
		lastMeasurement = System.nanoTime() - 2 * DELAY_IN_NANO;
		
		IVirtuinoService virtuinoService = ServiceManager.getInstance().getService(IVirtuinoService.class);
		try {
			virtuinoService.subscribe(VirtuinoServiceType.PIR.getVal(), VirtuinoCommandType.DigitalRead, 12, this::setPir);
		} catch (VirtuinoServiceException e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * @return the pir
	 */
	public synchronized float getPir() {
		return this.pir;
	}

	/**
	 * @param pir the pir to set
	 */
	public synchronized void setPir(Long nano, Float pir) {
		long periodFromLastMeasurement = nano - this.lastMeasurement;
		if(this.pir != pir || periodFromLastMeasurement > DELAY_IN_NANO) {
			this.pir = pir;
			this.lastMeasurement = nano;
			
			if(this.timeService.isSynchonized()) {
				try {
					this.setDataUTCDateTime(this.timeService.getUTCDateTime(nano));
					this.write();
					this.fireMeasurementChanged();
					
				} 
				catch (SynchronizationException e) {
				}
			}
		}
	}
	
	
}
