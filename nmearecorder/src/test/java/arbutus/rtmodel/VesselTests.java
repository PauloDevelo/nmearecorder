package arbutus.rtmodel;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import arbutus.influxdb.IInfluxdbRepository;
import arbutus.influxdb.InfluxdbRepository;
import arbutus.nmea.service.INMEAService;
import arbutus.nmea.service.NMEAService;
import arbutus.service.ServiceManager;
import arbutus.timeservice.ITimeService;
import arbutus.timeservice.TimeService;

public class VesselTests {
	private Vessel arbutus;
	
	@BeforeClass
	public static void InitTests() {
		ServiceManager srvMgr = ServiceManager.getInstance();
		
		srvMgr.register(INMEAService.class, new NMEAService());
		srvMgr.register(ITimeService.class, new TimeService());
		srvMgr.register(IInfluxdbRepository.class, new InfluxdbRepository());
		
		ServiceManager.getInstance().startServices();
	}
	
	@Before
	public void setup() {
		arbutus = new Vessel();
	}
	
	@After
	public void tearDown() {
		arbutus.unsubscribe();
	}
	
	@Test
	public void BuildArbutus() {
		// Arrange
		
		// Act
		
		// Assert
		assertTrue("Everything should be ok so far.", true);
	}
	
	@Test
	public void RunArbutus() {
		// Arrange
		// wait for the timeservice to sync.
		try {
			TimeUnit.SECONDS.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Act
		
		// Assert
		assertTrue("We should be somewhere.", arbutus.getLatitudeDegDec() != 0 && arbutus.getLongitudeDegDec() != 0);
	}

}
