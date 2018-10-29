package arbutus.rtmodel;

import java.io.InvalidClassException;

import arbutus.nmea.sentences.*;
import arbutus.nmea.service.INMEAListener;
import arbutus.nmea.service.INMEAService;
import arbutus.service.ServiceManager;

public final class Vessel implements INMEAListener{
	private final INMEAService nmeaService;
	
	private final PIRMeasurement pirMeasurement;
	private final EngineMeasurement engineMeasurement;
	
	private final GPSMeasurement gpsMeasurement;
	private final FluxgateMeasurement fluxgateMeasurement;
	private final SounderMeasurement sounder;
	private final SpeedoMeasurement speedo;
	private final WaterTempMeasurement waterTemp;
	private final AnemoMeasurement anemo;
	
	private final WindMeasurement wind;
	private final CurrentMeasurement current;
	
	public Vessel() throws InvalidClassException {
		
		this.pirMeasurement = new PIRMeasurement();
		
		this.gpsMeasurement = new GPSMeasurement();
		this.fluxgateMeasurement = new FluxgateMeasurement();
		this.sounder = new SounderMeasurement();
		this.speedo = new SpeedoMeasurement();
		this.waterTemp = new WaterTempMeasurement();
		this.anemo = new AnemoMeasurement();

		this.wind = new WindMeasurement(750, this.fluxgateMeasurement, this.anemo, this.gpsMeasurement);
		this.current = new CurrentMeasurement(750, this.fluxgateMeasurement, this.gpsMeasurement, this.speedo);
		
		this.engineMeasurement = new EngineMeasurement(this.gpsMeasurement);
		
		nmeaService = ServiceManager.getInstance().getService(INMEAService.class);
		
		nmeaService.subscribe(GPRMC.class, this);
		nmeaService.subscribe(HCHDG.class, this);
		nmeaService.subscribe(SDDPT.class, this);
		nmeaService.subscribe(VWVHW.class, this);
		nmeaService.subscribe(VWMTW.class, this);
		nmeaService.subscribe(WIMWV.class, this);
	}
	
	public void unsubscribe() {
		nmeaService.unsubscribe(GPRMC.class, this);
		nmeaService.unsubscribe(HCHDG.class, this);
		nmeaService.unsubscribe(SDDPT.class, this);
		nmeaService.unsubscribe(VWVHW.class, this);
		nmeaService.unsubscribe(VWMTW.class, this);
		nmeaService.unsubscribe(WIMWV.class, this);
	}

	@Override
	public void onNewNMEASentence(NMEASentence sentence) {
		if(sentence instanceof GPRMC) {
			GPRMC rmc = GPRMC.class.cast(sentence);
			
			gpsMeasurement.setNMEASentence(rmc);
		}
		else if(sentence instanceof HCHDG) {
			HCHDG hdg = HCHDG.class.cast(sentence);
			
			fluxgateMeasurement.setNMEASentence(hdg);
		}
		else if(sentence instanceof SDDPT) {
			SDDPT dpt = SDDPT.class.cast(sentence);
			
			sounder.setNMEASentence(dpt);
		}
		else if(sentence instanceof VWVHW) {
			VWVHW vhw = VWVHW.class.cast(sentence);
			
			this.speedo.setNMEASentence(vhw);
		}
		else if(sentence instanceof VWMTW) {
			VWMTW mtw = VWMTW.class.cast(sentence);
			
			this.waterTemp.setNMEASentence(mtw);
		}
		else if(sentence instanceof WIMWV) {
			WIMWV mwv = WIMWV.class.cast(sentence);
			
			this.anemo.setNMEASentence(mwv);
		}
	}
	
	public GPSMeasurement getGPSMeas() {
		return this.gpsMeasurement;
	}

	public FluxgateMeasurement getFluxgateMeas() {
		return this.fluxgateMeasurement;
	}

	public AnemoMeasurement getAnemo() {
		return this.anemo;
	}

	public WindMeasurement getWind() {
		return this.wind;
	}
	
	public CurrentMeasurement getCurrent() {
		return this.current;
	}

	public EngineMeasurement getEngineMeasurement() {
		return this.engineMeasurement;
	}

	public PIRMeasurement getPirMeasurement() {
		return pirMeasurement;
	}
}
