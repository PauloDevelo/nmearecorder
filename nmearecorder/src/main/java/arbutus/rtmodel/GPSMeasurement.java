package arbutus.rtmodel;

import java.io.InvalidClassException;

import arbutus.influxdb.measurement.InfluxFieldAnnotation;
import arbutus.influxdb.measurement.InfluxMeasurementAnnotation;
import arbutus.influxdb.measurement.NMEAMeasurement;
import arbutus.nmea.sentences.GPRMC;
import arbutus.nmea.sentences.Status;


@InfluxMeasurementAnnotation(name="GPS")
public final class GPSMeasurement extends NMEAMeasurement<GPSMeasurement, GPRMC> {

	@InfluxFieldAnnotation(name="latitude")
	private float latitudeDegDec = Float.NaN;
	@InfluxFieldAnnotation(name="longitude")
	private float longitudeDegDec = Float.NaN;
	
	@InfluxFieldAnnotation(name="sog")
	private float sog = Float.NaN;
	@InfluxFieldAnnotation(name="cog")
	private float cog = Float.NaN;
	
	public GPSMeasurement() throws InvalidClassException, ClassCastException {
		super(GPSMeasurement.class);
	}
	
	/**
	 * @return the latitudeDegDec
	 */
	public float getLatitudeDegDec() {
		return latitudeDegDec;
	}

	/**
	 * @return the longitudeDegDec
	 */
	public float getLongitudeDegDec() {
		return longitudeDegDec;
	}

	/**
	 * @return the sog
	 */
	public float getSog() {
		return sog;
	}
	
	/**
	 * @return the cog
	 */
	public float getCog() {
		return cog;
	}
	
	@Override
	protected void onSetDataDate(GPRMC rmc) {
		setDataUTCDateTime(rmc.getUtcDateTime());
	}

	@Override
	protected void onSetNMEASentence(GPRMC rmc) {
		if(rmc.getStatus() == Status.DataValid) {
			setPosition(rmc.getLatitudeDegDec(), rmc.getLongitudeDegDec());
			setSpeedAndCourseOverGround(rmc.getSogKnot(), rmc.getTmgDegT());
		}
		else {
			setPosition(Float.NaN, Float.NaN);
			setSpeedAndCourseOverGround(Float.NaN, Float.NaN);
		}
	}
	
	/**
	 * @param latitudeDegDec the latitudeDegDec to set
	 */
	private void setPosition(float latitudeDegDec, float longitudeDegDec) {
		this.latitudeDegDec = latitudeDegDec;
		this.longitudeDegDec = longitudeDegDec;
	}
	
	/**
	 * @param sog the sog to set
	 */
	private void setSpeedAndCourseOverGround(float sog, float cog) {
		this.sog = sog;
		this.cog = cog;
	}
}
