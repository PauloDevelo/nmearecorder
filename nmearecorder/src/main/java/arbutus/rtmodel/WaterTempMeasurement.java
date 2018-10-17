package arbutus.rtmodel;

import java.io.InvalidClassException;

import arbutus.influxdb.measurement.InfluxFieldAnnotation;
import arbutus.influxdb.measurement.InfluxMeasurementAnnotation;
import arbutus.influxdb.measurement.NMEAMeasurement;
import arbutus.nmea.sentences.VWMTW;

@InfluxMeasurementAnnotation(name="WaterTemp")
public final class WaterTempMeasurement extends NMEAMeasurement<WaterTempMeasurement, VWMTW> {
	
	@InfluxFieldAnnotation(name="waterTemperature")
	private float waterTemp = Float.NaN;
	
	public WaterTempMeasurement() throws InvalidClassException, ClassCastException {
		super(WaterTempMeasurement.class);
	}
	
	/**
	 * @return the waterTemp
	 */
	public float getWaterTemp() {
		return waterTemp;
	}
	
	@Override
	protected void onSetNMEASentence(VWMTW mtw) {
		setWaterTemp(mtw.getWaterTempCelcius());
	}

	/**
	 * @param waterTemp the waterTemp to set
	 */
	private void setWaterTemp(float waterTemp) {
		this.waterTemp = waterTemp;
	}
}
