package arbutus.rtmodel;

import arbutus.influxdb.measurement.InfluxFieldAnnotation;
import arbutus.influxdb.measurement.InfluxMeasurementAnnotation;
import arbutus.influxdb.measurement.NMEAMeasurement;
import arbutus.nmea.sentences.VWVHW;

@InfluxMeasurementAnnotation(name="Speedo")
public final class SpeedoMeasurement extends NMEAMeasurement<SpeedoMeasurement, VWVHW> {
	@InfluxFieldAnnotation(name="stw")
	private float stw = Float.NaN;
	
	public SpeedoMeasurement() {
		super(SpeedoMeasurement.class);
	}

	/**
	 * @return the stw
	 */
	public float getStw() {
		return this.stw;
	}
	
	@Override
	protected void onSetNMEASentence(VWVHW vhw) {
		setStw(vhw.getStwKn());
	}

	/**
	 * @param stw the stw to set
	 */
	private void setStw(float stw) {
		this.stw = stw;
	}

}
