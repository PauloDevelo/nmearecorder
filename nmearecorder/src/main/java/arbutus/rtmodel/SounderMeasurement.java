package arbutus.rtmodel;

import arbutus.influxdb.measurement.InfluxFieldAnnotation;
import arbutus.influxdb.measurement.InfluxMeasurementAnnotation;
import arbutus.influxdb.measurement.NMEAMeasurement;
import arbutus.nmea.sentences.SDDPT;

@InfluxMeasurementAnnotation(name="Sounder")
public final class SounderMeasurement extends NMEAMeasurement<SounderMeasurement, SDDPT> {
	
	@InfluxFieldAnnotation(name="depth")
	private float depth = Float.NaN;
	
	public SounderMeasurement() {
		super(SounderMeasurement.class);
	}
	
	/**
	 * @return the depth
	 */
	public float getDepth() {
		return this.depth;
	}

	@Override
	protected void onSetNMEASentence(SDDPT dpt) {
		if(!Float.isNaN(dpt.getDepth()) && !Float.isNaN(dpt.getOffsetFromTransducer())) {
			this.setDepth(dpt.getDepth() + dpt.getOffsetFromTransducer());
		}
		else {
			this.setDepth(Float.NaN);
		}
	}

	/**
	 * @param depth the depth to set
	 */
	private void setDepth(float depth) {
		this.depth = depth;
	}
}
