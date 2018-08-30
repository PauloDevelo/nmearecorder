package arbutus.timeservice;

import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import arbutus.nmea.sentences.GPRMC;
import arbutus.nmea.service.NMEAService;
import arbutus.timeservice.SynchronizationException;
import arbutus.timeservice.TimeService;

public class TimeServiceTests {
	static SimpleDateFormat format = new SimpleDateFormat("dd'/'MM'/'yyyy HH':'mm':'ss'.'SSSX");
	
	@BeforeClass
	public static void ArrangeTimeSeriveTests() {
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	private NMEAService nmeaService = null;
	private TimeService timeService = null;
	
	@Before
	public void arrange() {
		nmeaService = new NMEAService();
		timeService = new TimeService();
		
		nmeaService.subscribe(GPRMC.class, timeService);
		
		nmeaService.start();
	}
	
	@After
	public void tearDown() {
		nmeaService.stop();
		timeService.stop();
	}
	
	@Test
	public void Synchronized_InATimelyManner() {
		// Arrange
		try {
			TimeUnit.SECONDS.sleep(8);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Assert
		assertTrue("After 8 sec, TimeService should be sync", timeService.isSynchonized());
	}
		
	@Test
	public void Check_TimeAccuracy() {
		
		//Arrange
		try {
			TimeUnit.SECONDS.sleep(7);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Act
		
		// Assert
		try {
			long timeDiffWithSystem = System.currentTimeMillis() - timeService.getUTCDateTime().getTime();
			
			assertTrue("Time internet vs GPS time should not be different more than 3 sec.", Math.abs(timeDiffWithSystem) < 3000);
			

			//Arrange
			try {
				TimeUnit.SECONDS.sleep(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			long dev = timeDiffWithSystem - (System.currentTimeMillis() - timeService.getUTCDateTime().getTime());
			
			assertTrue("The deviation in mlliseconds between both clocks should less than 20ms", Math.abs(dev) < 30);
		
		} catch (SynchronizationException e1) {
			e1.printStackTrace();
		}
	}

	@Test(expected=SynchronizationException.class)
	public void GetDateTime_TooEarly() throws SynchronizationException {
		timeService.getUTCDateTime();
	}
	
	@Test
	public void GetUTCDateTime_AfterReceivingCorruptedData_ShouldStillReturnTheCorrectTime() throws ParseException {
		//Arrange
		GPRMC gprmc = new GPRMC(new StringBuilder("$GPRMC,193134.00,A,2219.93324,S,16649.39025,E,0.052,,,,,D*64"));
		
		try {
			TimeUnit.SECONDS.sleep(7);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		try {
			long timeDiffWithSystem = System.currentTimeMillis() - timeService.getUTCDateTime().getTime();

			// Act
			
			timeService.onNewNMEASentence(gprmc);
			
			long dev = timeDiffWithSystem - (System.currentTimeMillis() - timeService.getUTCDateTime().getTime());
			
			// Assert
			assertTrue("The deviation in mlliseconds between both clocks should less than 20ms", Math.abs(dev) < 20);
		
		} catch (SynchronizationException e1) {
			e1.printStackTrace();
		}
	}
}
