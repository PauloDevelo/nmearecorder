package arbutus.rtmodel;

import arbutus.influxdb.measurement.InfluxFieldAnnotation;
import arbutus.influxdb.measurement.InfluxMeasurementAnnotation;
import arbutus.influxdb.measurement.NMEAMeasurement;
import arbutus.nmea.sentences.HCHDG;

@InfluxMeasurementAnnotation(name="Fluxgate")
public final class FluxgateMeasurement extends NMEAMeasurement<FluxgateMeasurement, HCHDG> {
	
	@InfluxFieldAnnotation(name="hdg")
	private float hdg = Float.NaN;
	
	public FluxgateMeasurement() {
		super(FluxgateMeasurement.class);
	}
	
	/**
	 * @return the hdg
	 */
	public float getHdg() {
		return hdg;
	}
	
	@Override
	protected void onSetNMEASentence(HCHDG hdg) {
		if(!Float.isNaN(hdg.getMagHdgDeg())){
			float hdgVal = hdg.getMagHdgDeg();
			
			if(!Float.isNaN(hdg.getMagDevDeg())) {
				hdgVal += hdg.getMagDevDeg();
			}
			
			if(!Float.isNaN(hdg.getMagVarDeg())) {
				hdgVal += hdg.getMagVarDeg();
			}
			
			this.setHdg(hdgVal);
		}
		else {
			this.setHdg(Float.NaN);
		}
	}

	/**
	 * @param hdg the hdg to set
	 */
	private void setHdg(float hdg) {
		this.hdg = hdg;
	}
}
