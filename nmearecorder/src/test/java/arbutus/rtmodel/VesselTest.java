package arbutus.rtmodel;

import static org.junit.Assert.assertTrue;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat; 

import java.util.Date;
import java.util.HashMap;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.concurrent.Synchroniser;


import arbutus.influxdb.IInfluxdbRepository;
import arbutus.nmea.service.INMEAService;
import arbutus.nmea.service.NMEAService;
import arbutus.nmea.service.connectors.NMEAReaderStub;
import arbutus.service.IService;
import arbutus.service.ServiceManager;
import arbutus.test.ToolBox;
import arbutus.timeservice.ITimeService;
import arbutus.timeservice.TimeService;

interface InfluxdbRepositoryServiceInterface extends IInfluxdbRepository, IService{}

public class VesselTest {
	private Vessel arbutus;
	
	private Mockery context;
	private InfluxdbRepositoryServiceInterface influxService;
	
	@Before
	public void setup() {
		ServiceManager srvMgr = ServiceManager.getInstance();
		
		srvMgr.register(INMEAService.class, new NMEAService(NMEAReaderStub.class));
		srvMgr.register(ITimeService.class, new TimeService());
		
		this.context = new JUnit4Mockery() {{
		    setThreadingPolicy(new Synchroniser());
		}};
		
		this.influxService = context.mock(InfluxdbRepositoryServiceInterface.class);
		srvMgr.register(IInfluxdbRepository.class, this.influxService);
	}
	
	@After
	public void tearDown() {
		arbutus.unsubscribe();
		
		ServiceManager.getInstance().stopServices();
		
		ServiceManager.getInstance().unregister(INMEAService.class);
		ServiceManager.getInstance().unregister(ITimeService.class);
		ServiceManager.getInstance().unregister(IInfluxdbRepository.class);
	}
	
	@Test
	public void BuildArbutus() {
		// Arrange
		context.checking(new Expectations() 
		{{
				ignoring(influxService).start();
				ignoring(influxService).stop();
		}});
		
		ServiceManager.getInstance().startServices();
		
		// Act
		arbutus = new Vessel();
		
		// Assert
		assertTrue("Everything should be ok so far.", true);
	}
	
	@Test
	public void RunArbutus_AfterTimeServiceInitialized_ArbutusShouldGet() {
		// Arrange
		context.checking(new Expectations() 
		{{
				ignoring(influxService).start();
				ignoring(influxService).stop();
		}});
		
		ServiceManager.getInstance().startServices();
		arbutus = new Vessel();
		
		// Act
		ToolBox.wait(3);
		
		// Assert
		assertTrue("We should be somewhere.", arbutus.getGPSMeas().getLatitudeDegDec() != 0 && arbutus.getGPSMeas().getLongitudeDegDec() != 0);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void RunArbutus_After10sec_WeShouldStartPushingValues() {
		// Arrange
		context.checking(new Expectations() 
		{{
				ignoring(influxService).start();
				atLeast(1).of(influxService).addPoint(with(any(String.class)), with(any(Date.class)), with(any(HashMap.class)));
				ignoring(influxService).stop();
		}});
		
		ServiceManager.getInstance().startServices();
		arbutus = new Vessel();
		
		// Act
		ToolBox.wait(12);
		
		// Assert
		context.assertIsSatisfied();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void RunArbutus_ShouldReceiveSomeWindInformation() {
		// Arrange
		context.checking(new Expectations() 
		{{
				ignoring(influxService).start();
				ignoring(influxService).addPoint(with(any(String.class)), with(any(Date.class)), with(any(HashMap.class)));
				ignoring(influxService).stop();
		}});
		
		ServiceManager.getInstance().startServices();
		arbutus = new Vessel();
		
		ToolBox.wait(12);
		
		// Act
		
		// Assert
		assertThat(arbutus.getAnemo().getRelWindSpeed(), is(not(Float.NaN)));
	}
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void RunArbutus_WithASudenStrongSpeed_ShouldCleanSpikes() {
		// Arrange
		context.checking(new Expectations() 
		{{
				ignoring(influxService).start();
				atLeast(1).of(influxService).addPoint(with(any(String.class)), with(any(Date.class)), with(any(HashMap.class)));
				ignoring(influxService).stop();
		}});
		
		ServiceManager.getInstance().startServices();
		arbutus = new Vessel();
		
		ToolBox.wait(12);
		
		// Act
		
		// Assert
		NMEAReaderStub.getCurrentInstance().injectSentence("$WIMWV,299,R,90,N,A*0D");
		ToolBox.wait(1);
		assertThat(arbutus.getAnemo().getRelWindSpeed(), is(equalTo(Float.NaN)));
	}
	
	@Test
	public void RunArbutus_WithHDG90_SOG0_AWD360_ShouldComputeTrueWindDirection90AndSpeed15() {
		// Arrange
		context.checking(new Expectations() 
		{{
				ignoring(influxService).start();
				atLeast(1).of(influxService).addPoint(with(any(String.class)), with(any(Date.class)), with(any(HashMap.class)));
				ignoring(influxService).stop();
		}});
		
		ServiceManager.getInstance().startServices();
		NMEAReaderStub.getCurrentInstance().setInterrupted(true);
		ToolBox.wait(1);
		
		arbutus = new Vessel();
		
		// Act
		NMEAReaderStub.getCurrentInstance().injectSentence("$GPRMC,033317.00,A,1805.51925,S,17632.80803,E,0.000,0,040818,,,D*79");
		NMEAReaderStub.getCurrentInstance().injectSentence("$WIMWV,360,R,15,N,A*0D");
		NMEAReaderStub.getCurrentInstance().injectSentence("$HCHDG,90,,,0,E*1A");
		
		ToolBox.wait(1);
		
		// Assert
		assertThat(arbutus.getWind().getTrueWindDir(), is(equalTo(90f)));
		assertThat(arbutus.getWind().getTrueWindSpeed(), is(equalTo(15f)));
	}
	
	@Test
	public void RunArbutus_WithHDG270_SOG0_AWD360_AWS15_ShouldComputeTrueWindDirection270AndSpeed15() {
		// Arrange
		context.checking(new Expectations() 
		{{
				ignoring(influxService).start();
				atLeast(1).of(influxService).addPoint(with(any(String.class)), with(any(Date.class)), with(any(HashMap.class)));
				ignoring(influxService).stop();
		}});
		
		ServiceManager.getInstance().startServices();
		NMEAReaderStub.getCurrentInstance().setInterrupted(true);
		ToolBox.wait(1);
		
		arbutus = new Vessel();
		
		// Act
		NMEAReaderStub.getCurrentInstance().injectSentence("$GPRMC,033317.00,A,1805.51925,S,17632.80803,E,0.000,0,040818,,,D*79");
		NMEAReaderStub.getCurrentInstance().injectSentence("$WIMWV,360,R,15,N,A*0D");
		NMEAReaderStub.getCurrentInstance().injectSentence("$HCHDG,270,,,0,E*1A");
		
		ToolBox.wait(1);
		
		// Assert
		assertThat(arbutus.getWind().getTrueWindDir(), is(equalTo(270f)));
		assertThat(arbutus.getWind().getTrueWindSpeed(), is(equalTo(15f)));
	}
	
	@Test
	public void RunArbutus_WithHDG90_SOG0_AWD360_COG90ShouldComputeTrueWindDirection90AndSpeed15() {
		// Arrange
		context.checking(new Expectations() 
		{{
				ignoring(influxService).start();
				atLeast(1).of(influxService).addPoint(with(any(String.class)), with(any(Date.class)), with(any(HashMap.class)));
				ignoring(influxService).stop();
		}});
		
		ServiceManager.getInstance().startServices();
		NMEAReaderStub.getCurrentInstance().setInterrupted(true);
		ToolBox.wait(1);
		
		arbutus = new Vessel();
		
		// Act
		NMEAReaderStub.getCurrentInstance().injectSentence("$GPRMC,033317.00,A,1805.51925,S,17632.80803,E,0,90,040818,,,D*79");
		NMEAReaderStub.getCurrentInstance().injectSentence("$WIMWV,360,R,15,N,A*0D");
		NMEAReaderStub.getCurrentInstance().injectSentence("$HCHDG,90,,,0,E*1A");
		
		ToolBox.wait(1);
		
		// Assert
		assertThat(arbutus.getWind().getTrueWindDir(), is(equalTo(90f)));
		assertThat(arbutus.getWind().getTrueWindSpeed(), is(equalTo(15f)));
	}
	
	@Test
	public void RunArbutus_WithHDG90_SOG15_AWD360_COG90ShouldComputeTrueWindDirection90AndSpeed0() {
		// Arrange
		context.checking(new Expectations() 
		{{
				ignoring(influxService).start();
				atLeast(1).of(influxService).addPoint(with(any(String.class)), with(any(Date.class)), with(any(HashMap.class)));
				ignoring(influxService).stop();
		}});
		
		ServiceManager.getInstance().startServices();
		NMEAReaderStub.getCurrentInstance().setInterrupted(true);
		ToolBox.wait(1);
		
		arbutus = new Vessel();
		
		// Act
		NMEAReaderStub.getCurrentInstance().injectSentence("$GPRMC,033317.00,A,1805.51925,S,17632.80803,E,15,90,040818,,,D*79");
		NMEAReaderStub.getCurrentInstance().injectSentence("$WIMWV,360,R,15,N,A*0D");
		NMEAReaderStub.getCurrentInstance().injectSentence("$HCHDG,90,,,0,E*1A");
		
		ToolBox.wait(1);
		
		// Assert
		assertThat(arbutus.getWind().getTrueWindSpeed(), is(equalTo(0f)));
	}
	
	@Test
	public void RunArbutus_WithHDG90_SOG15_AWD180_COG90ShouldComputeTrueWindDirection90AndSpeed30() {
		// Arrange
		context.checking(new Expectations() 
		{{
				ignoring(influxService).start();
				atLeast(1).of(influxService).addPoint(with(any(String.class)), with(any(Date.class)), with(any(HashMap.class)));
				ignoring(influxService).stop();
		}});
		
		ServiceManager.getInstance().startServices();
		NMEAReaderStub.getCurrentInstance().setInterrupted(true);
		ToolBox.wait(1);
		
		arbutus = new Vessel();
		
		// Act
		NMEAReaderStub.getCurrentInstance().injectSentence("$GPRMC,033317.00,A,1805.51925,S,17632.80803,E,15,90,040818,,,D*79");
		NMEAReaderStub.getCurrentInstance().injectSentence("$WIMWV,180,R,15,N,A*0D");
		NMEAReaderStub.getCurrentInstance().injectSentence("$HCHDG,90,,,0,E*1A");
		
		ToolBox.wait(1);
		
		// Assert
		assertThat(arbutus.getWind().getTrueWindSpeed(), is(equalTo(30f)));
	}
	
	@Test
	@Ignore
	public void RunArbutus_WithRealData_ShouldComputeTrueWindDirection() {
		// Arrange
		context.checking(new Expectations() 
		{{
				ignoring(influxService).start();
				atLeast(1).of(influxService).addPoint(with(any(String.class)), with(any(Date.class)), with(any(HashMap.class)));
				ignoring(influxService).stop();
		}});
		
		ServiceManager.getInstance().startServices();
		NMEAReaderStub.getCurrentInstance().setInterrupted(true);
		ToolBox.wait(1);
		
		arbutus = new Vessel();
		
		// Act
		NMEAReaderStub.getCurrentInstance().injectSentence("$WIMWV,277,R,7.0,N,A*38");
		NMEAReaderStub.getCurrentInstance().injectSentence("$HCHDG,286.2,,,9.6,E*28");
		NMEAReaderStub.getCurrentInstance().injectSentence("$GPVTG,297.8,T,288.1,M,4.6,N,8.5,K,D*2E");
		
		ToolBox.wait(1);
		
		// Assert
		assertThat(arbutus.getWind().getTrueWindSpeed(), is(equalTo(30f)));
	}
}
