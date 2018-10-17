package arbutus.rtmodel;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

import arbutus.influxdb.measurement.InfluxFieldAnnotation;
import arbutus.influxdb.measurement.InfluxMeasurementAnnotation;
import arbutus.influxdb.measurement.NMEAMeasurement;
import arbutus.nmea.sentences.Status;
import arbutus.nmea.sentences.WIMWV;

@InfluxMeasurementAnnotation(name="Anemo")
public final class AnemoMeasurement extends NMEAMeasurement<AnemoMeasurement, WIMWV> {
	
	@InfluxFieldAnnotation(name="relWindSpeed")
	private float relWindSpeed = Float.NaN;
	private List<Float> cleanedRelWindSpeedHisto = new ArrayList<Float>();
	
	@InfluxFieldAnnotation(name="relWindDir")
	private float relWindDir = Float.NaN;
	
	public AnemoMeasurement() throws InvalidClassException, ClassCastException {
		super(AnemoMeasurement.class);
	}
	
	/**
	 * @return the relWindSpeed
	 */
	public float getRelWindSpeed() {
		return relWindSpeed;
	}
	
	/**
	 * @return the relWindDir
	 */
	public float getRelWindDir() {
		return relWindDir;
	}
	
	@Override
	protected void onSetNMEASentence(WIMWV mwv) {
		if(mwv.getStatus() == Status.DataValid) {
			if(!mwv.isTrue()) {
				
				if(!Float.isNaN(mwv.getWindAngle()) && !Float.isNaN(mwv.getWindSpeedKn()))
					setRelWind(mwv.getWindAngle(), mwv.getWindSpeedKn());
			}
		}
		else {
			setRelWind(Float.NaN, Float.NaN);
		}
	}

	/**
	 * @param relWindSpeed the relWindSpeed to set
	 */
	private void setRelWind(float relWindDir, float relWindSpeed) {
		this.relWindSpeed = this.cleanRelWindSpeed(relWindSpeed);
		this.relWindDir = relWindDir;
		
	}
	
	private float cleanRelWindSpeed(float newRelWindSpeed) {
		
		if (Float.isNaN(newRelWindSpeed))
		{
			return Float.NaN;
		}
		
		float maxRelWindSpeed = getMaxRelWindSpeedFromHisto();
		if(Float.isNaN(maxRelWindSpeed) || newRelWindSpeed < 5 * (maxRelWindSpeed > 2 ? maxRelWindSpeed : 2)) {
			cleanedRelWindSpeedHisto.add(newRelWindSpeed);
			if(cleanedRelWindSpeedHisto.size() > 10) {
				cleanedRelWindSpeedHisto.remove(0);
			}
			return newRelWindSpeed;
		}
		else {
			return Float.NaN;
		}
	}
	
	private float getMaxRelWindSpeedFromHisto() {
		OptionalDouble maxSpeed = cleanedRelWindSpeedHisto.stream().mapToDouble(speed -> speed).max();
		
		if(maxSpeed.isPresent())
			return (float)maxSpeed.getAsDouble();
		else {
			return Float.NaN;
		}
	}
}
