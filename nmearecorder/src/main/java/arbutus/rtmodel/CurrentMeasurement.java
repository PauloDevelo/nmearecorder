/**
 * 
 */
package arbutus.rtmodel;

import arbutus.influxdb.measurement.ComputedMeasurement;
import arbutus.influxdb.measurement.InfluxFieldAnnotation;
import arbutus.influxdb.measurement.InfluxMeasurementAnnotation;

/**
 * @author paul_
 *
 */
@InfluxMeasurementAnnotation(name="Current")
public final class CurrentMeasurement extends ComputedMeasurement<CurrentMeasurement> {
	
	@InfluxFieldAnnotation(name="currentSpeed")
	private float currentSpeed = Float.NaN;
	
	@InfluxFieldAnnotation(name="currentDir")
	private float currentDir = Float.NaN;
	
	private final FluxgateMeasurement fluxgate;
	private final GPSMeasurement gps;
	private final SpeedoMeasurement speedo;

	public CurrentMeasurement(long thresholdInMilliSecond, FluxgateMeasurement fluxgate, GPSMeasurement gps, SpeedoMeasurement speedo) {
		super(thresholdInMilliSecond, CurrentMeasurement.class);

		this.fluxgate = fluxgate;
		this.gps = gps;
		this.speedo = speedo;
		
		this.addDependency(fluxgate);
		this.addDependency(gps);
		this.addDependency(speedo);
	}
	
	public float getCurrentSpeed() {
		return this.currentSpeed;
	}
	
	public float getCurrentDir() {
		return this.currentDir;
	}
	
	@Override
	protected void compute() {
		if(!Float.isNaN(this.fluxgate.getHdg()) && !Float.isNaN(this.speedo.getStw()) && !Float.isNaN(this.gps.getSog()) && !(Float.isNaN(this.gps.getCog())))
		{
			
			double hdgRad = Math.toRadians(this.fluxgate.getHdg());
			double cogRad = Math.toRadians(this.gps.getCog());
			
			float stw = this.speedo.getStw() == 0 ? this.gps.getSog() : this.speedo.getStw();
			
			double u =  this.gps.getSog() * Math.sin(cogRad) - stw * Math.sin(hdgRad);
			double v =  this.gps.getSog() * Math.cos(cogRad) - stw * Math.cos(hdgRad);
			
			this.currentSpeed = (float) Math.sqrt(u*u + v * v);
			this.currentDir = (float) Math.toDegrees(Math.atan2(u, v));
			
			if (this.currentDir < 0) {
				this.currentDir = this.currentDir + 360;
			}
		}
		else {
			this.currentSpeed = Float.NaN;
			this.currentDir = Float.NaN;
		}
	}
}
