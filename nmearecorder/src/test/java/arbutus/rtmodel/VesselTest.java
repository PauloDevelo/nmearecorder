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
		assertTrue("We should be somewhere.", arbutus.getLatitudeDegDec() != 0 && arbutus.getLongitudeDegDec() != 0);
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
		assertThat(arbutus.getRelWindSpeed(), is(not(Float.NaN)));
	}
	
	
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
		assertThat(arbutus.getRelWindSpeed(), is(equalTo(Float.NaN)));
	}

}
