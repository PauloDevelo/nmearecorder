package arbutus.timeservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import arbutus.nmea.sentences.GPRMC;
import arbutus.nmea.service.INMEAService;
import arbutus.nmea.service.NMEAService;
import arbutus.service.ServiceManager;
import arbutus.test.ToolBox;
import arbutus.timeservice.SynchronizationException;
import arbutus.timeservice.TimeService;

public class TimeServiceTests {
	static SimpleDateFormat format = new SimpleDateFormat("dd'/'MM'/'yyyy HH':'mm':'ss'.'SSSX");
	
	private ITimeService timeService = null;
	
	@BeforeClass
	public static void ArrangeTimeSeriveTests() {
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		ServiceManager.getInstance().register(INMEAService.class, new NMEAService());
		ServiceManager.getInstance().register(ITimeService.class, new TimeService(new TimeServiceContext(4, 10, 25, "echo \"yyyy/MM/dd HH:mm:ss.SSS+00:00\"")));
	}

	@Before
	public void arrange() {
		ServiceManager.getInstance().startServices();
		
		timeService = ServiceManager.getInstance().getService(ITimeService.class);
	}
	
	@After
	public void tearDown() {
		ServiceManager.getInstance().stopServices();
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
			TimeService.class.cast(timeService).onNewNMEASentence(gprmc);
			
			long dev = timeDiffWithSystem - (System.currentTimeMillis() - timeService.getUTCDateTime().getTime());
			
			// Assert
			assertTrue("The deviation in mlliseconds between both clocks should less than 20ms", Math.abs(dev) < 20);
		
		} catch (SynchronizationException e1) {
			e1.printStackTrace();
		}
	}
	
	
	@Test
	public void formatSyncCommand_WithACorrectDateTime_ShouldReturnTheCommadFormattedWithThisDate() {
		// Arrange
		Date aDate = new Date(1535868218178L);
		
		// Act
		try {
			StringBuilder cmd = ToolBox.callPrivateMethod(StringBuilder.class, timeService, "getFormatedSyncCommand", Date.class, aDate);
			
			// Assert
			assertEquals("echo \"2018/09/02 06:03:38.178+00:00\"",  cmd.toString());
		} catch (Throwable e) {
			fail("Should not throw an exception " + e.getMessage());
		}
	}
	
	
	@Test(expected=IllegalArgumentException.class)
	public void formatSyncCommand_WithADateNull_ShouldReturnAnEmptyCommand() throws Throwable {
		// Arrange
		Date aDate = null;

		// Act
		ToolBox.callPrivateMethod(StringBuilder.class, timeService, "getFormatedSyncCommand", Date.class, aDate);
	}
}
