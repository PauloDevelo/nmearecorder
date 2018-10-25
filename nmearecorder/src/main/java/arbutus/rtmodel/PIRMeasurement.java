package arbutus.rtmodel;

import java.io.InvalidClassException;

import arbutus.influxdb.measurement.InfluxFieldAnnotation;
import arbutus.influxdb.measurement.InfluxMeasurement;
import arbutus.influxdb.measurement.InfluxMeasurementAnnotation;
import arbutus.timeservice.SynchronizationException;

@InfluxMeasurementAnnotation(name="PIR")
public class PIRMeasurement extends InfluxMeasurement<PIRMeasurement> {

	private static final long DELAY_IN_NANO = 60000000000L;

	@InfluxFieldAnnotation(name="pir")
	private float pir = Float.NaN;
	
	private long lastMeasurement;
	
	
	public PIRMeasurement() throws InvalidClassException, ClassCastException {
		super(PIRMeasurement.class);
		
		lastMeasurement = System.nanoTime() - 2 * DELAY_IN_NANO;
		
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
		long periodFromLastMeasurement = System.nanoTime() - this.lastMeasurement;
		if(this.pir != pir || periodFromLastMeasurement > DELAY_IN_NANO) {
			this.pir = pir;
			
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
