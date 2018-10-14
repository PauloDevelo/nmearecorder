package arbutus.rtmodel;

import arbutus.influxdb.measurement.ComputedMeasurement;
import arbutus.influxdb.measurement.InfluxFieldAnnotation;
import arbutus.influxdb.measurement.InfluxMeasurementAnnotation;

@InfluxMeasurementAnnotation(name="Wind")
public final class WindMeasurement extends ComputedMeasurement<WindMeasurement> {
	@InfluxFieldAnnotation(name="appWindDir")
	private double awd = Float.NaN;
	
	@InfluxFieldAnnotation(name="trueWindSpeed")
	private float trueWindSpeed = Float.NaN;
	
	@InfluxFieldAnnotation(name="trueWindDir")
	private float trueWindDir = Float.NaN;
	
	private final FluxgateMeasurement fluxgate;
	private final AnemoMeasurement anemo;
	private final GPSMeasurement gps;
	
	/**
	 * @return the trueWindSpeed
	 */
	public float getTrueWindSpeed() {
		return this.trueWindSpeed;
	}

	/**
	 * @return the trueWindDir
	 */
	public float getTrueWindDir() {
		return this.trueWindDir;
	}
	
	/**
	 * @return the apparent Wind Dir
	 */
	public float getAwd() {
		return (float) this.awd;
	}
	
	public WindMeasurement(long thresholdInMilliSecond, FluxgateMeasurement fluxgate, AnemoMeasurement anemo, GPSMeasurement gps) {
		super(thresholdInMilliSecond, WindMeasurement.class);
		
		this.fluxgate = fluxgate;
		this.anemo = anemo;
		this.gps = gps;
		
		this.addDependency(fluxgate);
		this.addDependency(anemo);
		this.addDependency(gps);
	}

	@Override
	protected void compute() {
		if(!Float.isNaN(this.fluxgate.getHdg()) && !Float.isNaN(this.anemo.getRelWindDir()) && !Float.isNaN(this.gps.getSog()) && !(Float.isNaN(this.gps.getCog()) && !(Float.isNaN(this.anemo.getRelWindSpeed()))))
		{
			this.awd = (this.fluxgate.getHdg() + this.anemo.getRelWindDir()) % 360;
			
			double awdRad = Math.toRadians(this.awd);
			double cogRad = Math.toRadians(this.gps.getCog());
			
			double u =  this.anemo.getRelWindSpeed() * Math.sin(awdRad) - this.gps.getSog() * Math.sin(cogRad);
			double v =  this.anemo.getRelWindSpeed() * Math.cos(awdRad) - this.gps.getSog() * Math.cos(cogRad);
			
			this.trueWindSpeed = (float) Math.sqrt(u*u + v * v);
			this.trueWindDir = (float) Math.toDegrees(Math.atan2(u, v));
			
			if (this.trueWindDir < 0) {
				this.trueWindDir = this.trueWindDir + 360;
			}
		}
		else {
			this.awd = Float.NaN;
			this.trueWindSpeed = Float.NaN;
			this.trueWindDir = Float.NaN;
		}
	}
}
