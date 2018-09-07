package arbutus.rtmodel;

import java.util.Timer;

import arbutus.influxdb.InfluxField;
import arbutus.influxdb.InfluxTask;
import arbutus.nmea.sentences.*;
import arbutus.nmea.service.INMEAListener;
import arbutus.nmea.service.INMEAService;
import arbutus.service.ServiceManager;
import arbutus.util.PropertiesFile;

public class Vessel implements INMEAListener{
	private PropertiesFile properties = null;
	
	private INMEAService nmeaService = null;
	
	
	@InfluxField(name="latitude")
	private float latitudeDegDec = Float.NaN;
	@InfluxField(name="longitude")
	private float longitudeDegDec = Float.NaN;
	
	@InfluxField(name="sog")
	private float sog = Float.NaN;
	@InfluxField(name="cog")
	private float cog = Float.NaN;
	
	@InfluxField(name="hdg")
	private float hdg = Float.NaN;
	
	@InfluxField(name="depth")
	private float depth = Float.NaN;
	
	@InfluxField(name="waterTemperature")
	private float waterTemp = Float.NaN;
	
	@InfluxField(name="stw")
	private float stw = Float.NaN;
	
	@InfluxField(name="relWindSpeed")
	private float relWindSpeed = Float.NaN;
	
	@InfluxField(name="relWindDir")
	private float relWindDir = Float.NaN;
	
	private Timer timer;
	
	public Vessel() {
		String fileSep = System.getProperty("file.separator");
		String propertiesPath = System.getProperty("user.dir") + fileSep + "properties" + fileSep + "arbutus.properties";
		properties = PropertiesFile.getPropertiesVM(propertiesPath);
		
		nmeaService = ServiceManager.getInstance().getService(INMEAService.class);
		
		timer = new Timer();
        timer.schedule(new InfluxTask<Vessel>(properties.getValue("measurement"), Vessel.class, this), 
        		properties.getValueInt("initialDelaySecond", 10) * 1000, 
        		properties.getValueInt("periodSecond", 2)*1000);
		
		nmeaService.subscribe(GPRMC.class, this);
		nmeaService.subscribe(GPVTG.class, this);
		nmeaService.subscribe(HCHDG.class, this);
		nmeaService.subscribe(SDDPT.class, this);
		nmeaService.subscribe(VWVHW.class, this);
		nmeaService.subscribe(VWMTW.class, this);
		nmeaService.subscribe(WIMWV.class, this);
	}
	
	public void unsubscribe() {
		nmeaService.unsubscribe(GPRMC.class, this);
		nmeaService.unsubscribe(GPVTG.class, this);
		nmeaService.unsubscribe(HCHDG.class, this);
		nmeaService.unsubscribe(SDDPT.class, this);
		nmeaService.unsubscribe(VWVHW.class, this);
		nmeaService.unsubscribe(VWMTW.class, this);
		nmeaService.unsubscribe(WIMWV.class, this);
		
		timer.cancel();
	}

	@Override
	public void onNewNMEASentence(NMEASentence sentence) {
		if(sentence instanceof GPRMC) {
			GPRMC rmc = GPRMC.class.cast(sentence);
			
			if(rmc.getStatus() == Status.DataValid) {
				synchronized (this) {
					if(!Float.isNaN(rmc.getLatitudeDegDec()) && !Float.isNaN(rmc.getLongitudeDegDec()))
						setPosition(rmc.getLatitudeDegDec(), rmc.getLongitudeDegDec());
				}
			}
		}
		else if(sentence instanceof GPVTG) {
			GPVTG vtg = GPVTG.class.cast(sentence);	
			synchronized (this) {
				if(!Float.isNaN(vtg.getSpeedKn()) && !Float.isNaN(vtg.getTrackDegT()))
					setSpeedAndCourseOverGround(vtg.getSpeedKn(), vtg.getTrackDegT());
			}
		}
		else if(sentence instanceof HCHDG) {
			HCHDG hdg = HCHDG.class.cast(sentence);
			if(!Float.isNaN(hdg.getMagHdgDeg())){
				float hdgVal = hdg.getMagHdgDeg();
				
				if(!Float.isNaN(hdg.getMagDevDeg())) {
					hdgVal += hdg.getMagDevDeg();
				}
				
				if(!Float.isNaN(hdg.getMagVarDeg())) {
					hdgVal += hdg.getMagVarDeg();
				}
				
				setHdg(hdgVal);
			}
		}
		else if(sentence instanceof SDDPT) {
			SDDPT dpt = SDDPT.class.cast(sentence);
			
			if(!Float.isNaN(dpt.getDepth()) && !Float.isNaN(dpt.getOffsetFromTransducer())) {
				setDepth(dpt.getDepth() + dpt.getOffsetFromTransducer());
			}
		}
		else if(sentence instanceof VWVHW) {
			VWVHW vhw = VWVHW.class.cast(sentence);
			
			if(!Float.isNaN(vhw.getStwKn()))
				setStw(vhw.getStwKn());
		}
		else if(sentence instanceof VWMTW) {
			VWMTW mtw = VWMTW.class.cast(sentence);
			
			if(!Float.isNaN(mtw.getWaterTempCelcius())) 
				setWaterTemp(mtw.getWaterTempCelcius());
		}
		else if(sentence instanceof WIMWV) {
			WIMWV mwv = WIMWV.class.cast(sentence);
			
			if(!mwv.isTrue()) {
				if(!Float.isNaN(mwv.getWindAngle()) && !Float.isNaN(mwv.getWindSpeedKn()))
					setRelWind(mwv.getWindAngle(), mwv.getWindSpeedKn());
			}
		}
	}

	/**
	 * @return the latitudeDegDec
	 */
	public synchronized float getLatitudeDegDec() {
		return latitudeDegDec;
	}

	/**
	 * @param latitudeDegDec the latitudeDegDec to set
	 */
	private synchronized void setPosition(float latitudeDegDec, float longitudeDegDec) {
		this.latitudeDegDec = latitudeDegDec;
		this.longitudeDegDec = longitudeDegDec;
	}

	/**
	 * @return the longitudeDegDec
	 */
	public synchronized float getLongitudeDegDec() {
		return longitudeDegDec;
	}

	/**
	 * @return the sog
	 */
	public synchronized float getSog() {
		return sog;
	}

	/**
	 * @param sog the sog to set
	 */
	private synchronized void setSpeedAndCourseOverGround(float sog, float cog) {
		this.sog = sog;
		this.cog = cog;
	}

	/**
	 * @return the cog
	 */
	public synchronized float getCog() {
		return cog;
	}

	/**
	 * @return the hdg
	 */
	public synchronized float getHdg() {
		return hdg;
	}

	/**
	 * @param hdg the hdg to set
	 */
	private synchronized void setHdg(float hdg) {
		this.hdg = hdg;
	}

	/**
	 * @return the depth
	 */
	public synchronized float getDepth() {
		return depth;
	}

	/**
	 * @param depth the depth to set
	 */
	private synchronized void setDepth(float depth) {
		this.depth = depth;
	}

	/**
	 * @return the waterTemp
	 */
	public synchronized float getWaterTemp() {
		return waterTemp;
	}

	/**
	 * @param waterTemp the waterTemp to set
	 */
	private synchronized void setWaterTemp(float waterTemp) {
		this.waterTemp = waterTemp;
	}

	/**
	 * @return the stw
	 */
	public synchronized float getStw() {
		return stw;
	}

	/**
	 * @param stw the stw to set
	 */
	private synchronized void setStw(float stw) {
		this.stw = stw;
	}

	/**
	 * @return the relWindSpeed
	 */
	public synchronized float getRelWindSpeed() {
		return relWindSpeed;
	}

	/**
	 * @param relWindSpeed the relWindSpeed to set
	 */
	private synchronized void setRelWind(float relWindDir, float relWindSpeed) {
		this.relWindSpeed = relWindSpeed;
		this.relWindDir = relWindDir;
	}

	/**
	 * @return the relWindDir
	 */
	public synchronized float getRelWindDir() {
		return relWindDir;
	}
	
}
