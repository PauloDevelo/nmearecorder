package arbutus.influxdb.measurement;

import java.io.InvalidClassException;

import arbutus.nmea.sentences.NMEASentence;
import arbutus.timeservice.SynchronizationException;

public abstract class NMEAMeasurement<T, D extends NMEASentence> extends InfluxMeasurement<T> {

	public NMEAMeasurement(Class<T> type) throws InvalidClassException, ClassCastException{
		super(type);
	}
	
	public final void setNMEASentence(D sentence) {
		onSetDataDate(sentence);
		onSetNMEASentence(sentence);
		this.write();
		this.fireMeasurementChanged();
	}
	
	protected abstract void onSetNMEASentence(D sentence);

	protected void onSetDataDate(D sentence) {
		if(this.timeService.isSynchonized()) {
			try {
				this.setDataUTCDateTime(this.timeService.getUTCDateTime(sentence.getReceptionNanoTime()));
			} 
			catch (SynchronizationException e) {
			}
		}
	}
}
