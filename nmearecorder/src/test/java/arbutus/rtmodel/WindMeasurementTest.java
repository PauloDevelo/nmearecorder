package arbutus.rtmodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.InvalidClassException;
import java.util.Date;
import java.util.HashMap;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import arbutus.influxdb.IInfluxdbRepository;
import arbutus.nmea.sentences.GPRMC;
import arbutus.nmea.sentences.HCHDG;
import arbutus.nmea.sentences.WIMWV;
import arbutus.service.ServiceManager;
import arbutus.timeservice.ITimeService;
import arbutus.timeservice.SyncedTimeService;

public class WindMeasurementTest {
	
	private Mockery context;
	private InfluxdbRepositoryServiceInterface influxService;
	private final Date dataDate = new Date(1533353597000L);
	private FluxgateMeasurement fluxgate;
	private AnemoMeasurement anemo;
	private GPSMeasurement gps;
	private WindMeasurement wind;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setup() throws InvalidClassException, ClassCastException {
		this.context = new JUnit4Mockery() {{
		    setThreadingPolicy(new Synchroniser());
		}};
		
		this.influxService = context.mock(InfluxdbRepositoryServiceInterface.class);
		
		this.context.checking(new Expectations() 
		{{
			exactly(1).of(influxService).addPoint(with("Anemo"), with(dataDate), with(any(HashMap.class)));
			exactly(1).of(influxService).addPoint(with("GPS"), with(dataDate), with(any(HashMap.class)));
			exactly(1).of(influxService).addPoint(with("Fluxgate"), with(dataDate), with(any(HashMap.class)));
			exactly(1).of(influxService).addPoint(with("Wind"), with(dataDate), with(any(HashMap.class)));
		}});
		
		
		ServiceManager srvMgr = ServiceManager.getInstance();
		srvMgr.register(ITimeService.class, new SyncedTimeService(1000, dataDate, false));
		srvMgr.register(IInfluxdbRepository.class, this.influxService);
		
		this.fluxgate = new FluxgateMeasurement();
		this.anemo = new AnemoMeasurement();
		this.gps = new GPSMeasurement();
		this.wind = new WindMeasurement(1000, this.fluxgate, this.anemo, this.gps);
	}
	
	@After
	public void tearDown() {
		ServiceManager.getInstance().unregister(ITimeService.class);
		ServiceManager.getInstance().unregister(IInfluxdbRepository.class);
	}

	@Test
	public void Compute_WithHDG90_SOG0_AWD360_ShouldComputeTrueWindDirection90AndSpeed15() throws Exception {
		// Arrange
		HCHDG hchdg = new HCHDG(1000, new StringBuilder("$HCHDG,90,,,0,E*1A"));
		WIMWV mwv = new WIMWV(1000, new StringBuilder("$WIMWV,360,R,15,N,A*0D"));
		GPRMC rmc = new GPRMC(1000, new StringBuilder("$GPRMC,033317.00,A,1805.51925,S,17632.80803,E,0.000,0,040818,,,D*79"));

		// Act
		fluxgate.setNMEASentence(hchdg);
		anemo.setNMEASentence(mwv);
		gps.setNMEASentence(rmc);
		
		// Assert
		assertThat(wind.getTrueWindDir(), is(equalTo(90f)));
		assertThat(wind.getTrueWindSpeed(), is(equalTo(15f)));
		context.assertIsSatisfied();
	}
	
	@Test
	public void RunArbutus_WithHDG270_SOG0_AWD360_AWS15_ShouldComputeTrueWindDirection270AndSpeed15() throws Exception {
		// Arrange
		HCHDG hchdg = new HCHDG(1000, new StringBuilder("$HCHDG,270,,,0,E*1A"));
		WIMWV mwv = new WIMWV(1000, new StringBuilder("$WIMWV,360,R,15,N,A*0D"));
		GPRMC rmc = new GPRMC(1000, new StringBuilder("$GPRMC,033317.00,A,1805.51925,S,17632.80803,E,0.000,0,040818,,,D*79"));

		// Act
		fluxgate.setNMEASentence(hchdg);
		anemo.setNMEASentence(mwv);
		gps.setNMEASentence(rmc);
				
		// Assert
		assertThat(this.wind.getTrueWindDir(), is(equalTo(270f)));
		assertThat(this.wind.getTrueWindSpeed(), is(equalTo(15f)));
		context.assertIsSatisfied();
	}
	
	@Test
	public void RunArbutus_WithHDG90_SOG0_AWD360_COG90ShouldComputeTrueWindDirection90AndSpeed15() throws Exception {
		// Arrange
		HCHDG hchdg = new HCHDG(1000, new StringBuilder("$HCHDG,90,,,0,E*1A"));
		WIMWV mwv = new WIMWV(1000, new StringBuilder("$WIMWV,360,R,15,N,A*0D"));
		GPRMC rmc = new GPRMC(1000, new StringBuilder("$GPRMC,033317.00,A,1805.51925,S,17632.80803,E,0,90,040818,,,D*79"));

		// Act
		fluxgate.setNMEASentence(hchdg);
		anemo.setNMEASentence(mwv);
		gps.setNMEASentence(rmc);
				
		// Assert
		assertThat(this.wind.getTrueWindDir(), is(equalTo(90f)));
		assertThat(this.wind.getTrueWindSpeed(), is(equalTo(15f)));
		context.assertIsSatisfied();
	}
	
	@Test
	public void RunArbutus_WithHDG90_SOG15_AWD360_COG90ShouldComputeTrueWindDirection90AndSpeed0() throws Exception {
		// Arrange
		HCHDG hchdg = new HCHDG(1000, new StringBuilder("$HCHDG,90,,,0,E*1A"));
		WIMWV mwv = new WIMWV(1000, new StringBuilder("$WIMWV,360,R,15,N,A*0D"));
		GPRMC rmc = new GPRMC(1000, new StringBuilder("$GPRMC,033317.00,A,1805.51925,S,17632.80803,E,15,90,040818,,,D*79"));

		// Act
		fluxgate.setNMEASentence(hchdg);
		anemo.setNMEASentence(mwv);
		gps.setNMEASentence(rmc);
				
		// Assert
		assertThat(this.wind.getTrueWindDir(), is(equalTo(0f)));
		context.assertIsSatisfied();
	}
	
	@Test
	public void RunArbutus_WithHDG90_SOG15_AWD180_COG90ShouldComputeTrueWindDirection90AndSpeed30() throws Exception {
		// Arrange
		HCHDG hchdg = new HCHDG(1000, new StringBuilder("$HCHDG,90,,,0,E*1A"));
		WIMWV mwv = new WIMWV(1000, new StringBuilder("$WIMWV,180,R,15,N,A*0D"));
		GPRMC rmc = new GPRMC(1000, new StringBuilder("$GPRMC,033317.00,A,1805.51925,S,17632.80803,E,15,90,040818,,,D*79"));

		// Act
		fluxgate.setNMEASentence(hchdg);
		anemo.setNMEASentence(mwv);
		gps.setNMEASentence(rmc);
				
		// Assert
		assertThat(this.wind.getTrueWindSpeed(), is(equalTo(30f)));
		assertThat(this.wind.getTrueWindDir(), is(equalTo(270f)));
		context.assertIsSatisfied();
	}
}
