package arbutus.influxdb.measurement;

import java.io.InvalidClassException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import arbutus.timeservice.SynchronizationException;

public abstract class ComputedMeasurement<T> extends InfluxMeasurement<T> implements IMeasurementListener{
	private final static Logger log = Logger.getLogger(ComputedMeasurement.class);
	
	private final long thresholdInMilliSecond;
	
	private final List<Measurement> dependencies = new ArrayList<Measurement>();
	
	public ComputedMeasurement(long thresholdInMilliSecond, Class<T> type) throws InvalidClassException, ClassCastException{
		super(type);
		
		this.thresholdInMilliSecond = thresholdInMilliSecond;
	}
	
	@Override
	public final void onMeasurementChanged(Measurement measurement) {
		if(areDependenciesFromSameTimeRange() && this.getDataUTCDateTime() != this.getDateMax()) {
			compute();
			
			this.setDataUTCDateTime(this.getDateMax());
			this.write();
		}
	}

	protected final void addDependency(Measurement measurement) {
		dependencies.add(measurement);
		measurement.addListener(this);
	}
	
	protected final boolean areDependenciesFromSameTimeRange() {
		if (!timeService.isSynchonized()) {
			return false;
		}
		
		Date dateMin = this.getDateMin();
		Date dateMax = this.getDateMax();
				
		return (dateMax != null && dateMin != null && dateMax.getTime() - dateMin.getTime() < thresholdInMilliSecond);
	}
	
	protected abstract void compute();
	
	private final Date getDateMin() {
		try {
			Date dateMin = timeService.getUTCDateTime();
			
			for(Measurement dependency : dependencies) {
				if (dependency.getDataUTCDateTime() == null)
					return null;
				
				if(dependency.getDataUTCDateTime().before(dateMin)) {
					dateMin = dependency.getDataUTCDateTime();
				}
			}
			
			return dateMin;
		} catch (SynchronizationException e) {
			log.error(e);
			return null;
		}
	}
	
	private final Date getDateMax() {
		Date dateMax = Date.from(Instant.ofEpochMilli(0));
		
		for(Measurement dependency : dependencies) {
			if (dependency.getDataUTCDateTime() == null)
				return null;
			
			if(dependency.getDataUTCDateTime().after(dateMax)) {
				dateMax = dependency.getDataUTCDateTime();
			}
		}
		
		return dateMax;
	}
}
