package arbutus.influxdb.measurement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import arbutus.service.ServiceManager;
import arbutus.timeservice.ITimeService;

public abstract class Measurement {
	private final static Logger log = Logger.getLogger(Measurement.class);
	
	protected final ITimeService timeService;
	
	private Date dataDateTime = null;
	private final List<IMeasurementListener> listeners = new ArrayList<IMeasurementListener>();
	
	public Measurement() {
		this.timeService = ServiceManager.getInstance().getService(ITimeService.class);
	}
	
	public final Date getDataUTCDateTime() {
		return this.dataDateTime;
	}
	
	public final void addListener(IMeasurementListener listener) {
		if(listeners.contains(listener)) {
			log.warn("Listener is already in the list of the listeners.");
		}
		else {
			listeners.add(listener);
		}
	}
	
	public final void removeListener(IMeasurementListener listener) {
		if(!listeners.contains(listener)) {
			log.warn("Listener is not in the list of the listeners.");
		}
		else {
			listeners.remove(listener);
		}
	}
	
	protected final void setDataUTCDateTime(Date date) {
		this.dataDateTime = date;
	}

	protected final void fireMeasurementChanged() {
		for(IMeasurementListener listener : listeners) {
			try {
				listener.onMeasurementChanged(this);
			}
			catch (Exception e) {
				log.error("Error when a measurement changed", e);
			}
		}
	}
}
