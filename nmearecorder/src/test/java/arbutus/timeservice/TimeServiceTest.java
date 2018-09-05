package arbutus.timeservice;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import static org.hamcrest.MatcherAssert.assertThat; 

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import arbutus.nmea.sentences.GPRMC;
import arbutus.nmea.service.INMEAListener;
import arbutus.nmea.service.INMEAService;
import arbutus.service.IService;
import arbutus.service.ServiceManager;
import arbutus.test.ToolBox;
import arbutus.timeservice.SynchronizationException;
import arbutus.timeservice.TimeService;

interface NMEAServiceInterface extends INMEAService, IService{}

public class TimeServiceTest {
	static SimpleDateFormat format = new SimpleDateFormat("dd'/'MM'/'yyyy HH':'mm':'ss'.'SSSX");
	
	private Mockery context;
	private NMEAServiceInterface nmeaService;
	
	private TimeService timeservice;
	
	@BeforeClass
	public static void ArrangeTimeSeriveTests() {
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	@Before
	public void arrange() {
		this.timeservice = new TimeService(new TimeServiceContext(4, 10, 25, "echo \"yyyy/MM/dd HH:mm:ss.SSS+00:00\""));
		
		context = new Mockery();
		nmeaService = context.mock(NMEAServiceInterface.class);
		
		ServiceManager.getInstance().register(INMEAService.class, nmeaService);
		ServiceManager.getInstance().register(ITimeService.class, this.timeservice);
	}
	
	@After
	public void tearDown() {
		ServiceManager.getInstance().stopServices();
		
		ServiceManager.getInstance().unregister(INMEAService.class);
		ServiceManager.getInstance().unregister(ITimeService.class);
	}
	
	@Test 
	public void Start__When_TimeService_Starts_Subscribe_Should_Be_Call() {
		// Arrange 
		ITimeService timeService = ServiceManager.getInstance().getService(ITimeService.class);
		
		context.checking(new Expectations() 
		{{
				oneOf(nmeaService).start();
				oneOf(nmeaService).subscribe(GPRMC.class, INMEAListener.class.cast(timeService));
				ignoring(nmeaService).stop();
				ignoring(nmeaService).unsubscribe(GPRMC.class, INMEAListener.class.cast(timeService));
		}
		});
		
		// Act
		ServiceManager.getInstance().startServices();
		
		// Assert
		context.assertIsSatisfied();
		
	}
	
	@Test
	public void Synchronized_InATimelyManner() {
		// Arrange
		ITimeService timeService = ServiceManager.getInstance().getService(ITimeService.class);
		
		context.checking(new Expectations() 
		{{
				ignoring(nmeaService).start();
				ignoring(nmeaService).subscribe(GPRMC.class, INMEAListener.class.cast(timeService));
				ignoring(nmeaService).stop();
				ignoring(nmeaService).unsubscribe(GPRMC.class, INMEAListener.class.cast(timeService));
		}
		});
		
		long nanoTime = System.nanoTime();
		this.timeservice.onNewNMEASentence(new GPRMC(nanoTime, new StringBuilder("$GPRMC,033223.00,A,1805.48706,S,17632.90433,E,6.456,246.17,040818,,,D*7A")));
		this.timeservice.onNewNMEASentence(new GPRMC(nanoTime + 1000000000L, new StringBuilder("$GPRMC,033224.00,A,1805.48763,S,17632.90251,E,6.869,254.64,040818,,,D*7B")));
		this.timeservice.onNewNMEASentence(new GPRMC(nanoTime + 2000000000L, new StringBuilder("$GPRMC,033225.00,A,1805.48824,S,17632.90062,E,6.904,249.55,040818,,,D*70")));
		this.timeservice.onNewNMEASentence(new GPRMC(nanoTime + 3000000000L, new StringBuilder("$GPRMC,033226.00,A,1805.48891,S,17632.89860,E,7.527,252.95,040818,,,D*75")));
		
		// Act
		boolean isSynced = this.timeservice.isSynchonized();
		
		// Assert
		assertTrue("After 4 GPRMC sentences, TimeService should be sync", isSynced);
	}
		
	@Test
	public void Check_TimeAccuracy() {
		//Arrange
		ITimeService timeService = ServiceManager.getInstance().getService(ITimeService.class);
		
		context.checking(new Expectations() 
		{{
				ignoring(nmeaService).start();
				ignoring(nmeaService).subscribe(GPRMC.class, INMEAListener.class.cast(timeService));
				ignoring(nmeaService).stop();
				ignoring(nmeaService).unsubscribe(GPRMC.class, INMEAListener.class.cast(timeService));
		}
		});
		
		long nanoTime = System.nanoTime();
		this.timeservice.onNewNMEASentence(new GPRMC(nanoTime, new StringBuilder("$GPRMC,033223.00,A,1805.48706,S,17632.90433,E,6.456,246.17,040818,,,D*7A")));
		this.timeservice.onNewNMEASentence(new GPRMC(nanoTime + 1000000000L, new StringBuilder("$GPRMC,033224.00,A,1805.48763,S,17632.90251,E,6.869,254.64,040818,,,D*7B")));
		this.timeservice.onNewNMEASentence(new GPRMC(nanoTime + 2000000000L, new StringBuilder("$GPRMC,033225.00,A,1805.48824,S,17632.90062,E,6.904,249.55,040818,,,D*70")));
		this.timeservice.onNewNMEASentence(new GPRMC(nanoTime + 3000000000L, new StringBuilder("$GPRMC,033226.00,A,1805.48891,S,17632.89860,E,7.527,252.95,040818,,,D*75")));
		
		// Act
		GPRMC gprmc = new GPRMC(nanoTime + 4000000000L, new StringBuilder("$GPRMC,033227.00,A,1805.48954,S,17632.89662,E,6.748,250.16,040818,,,D*73"));
		Date timeServiceDate = null;
		try {
			timeServiceDate = this.timeservice.getUTCDateTime(nanoTime + 4000000000L);
		}
		catch (SynchronizationException e1) {
			e1.printStackTrace();
		}
		
		//Arrange
		long diff = Math.abs(gprmc.getUtcDateTime().getTime() - timeServiceDate.getTime());
		assertThat(diff, is(lessThan(1L)));
	}

	@Test(expected=SynchronizationException.class)
	public void GetDateTime_TooEarly() throws SynchronizationException {
		// Arrange
		ITimeService timeService = ServiceManager.getInstance().getService(ITimeService.class);
		
		context.checking(new Expectations() 
		{{
				ignoring(nmeaService).start();
				ignoring(nmeaService).subscribe(GPRMC.class, INMEAListener.class.cast(timeService));
				ignoring(nmeaService).stop();
				ignoring(nmeaService).unsubscribe(GPRMC.class, INMEAListener.class.cast(timeService));
		}
		});
		
		ServiceManager.getInstance().startServices();
		
		// Act
		timeService.getUTCDateTime();
	}
	
	@Test
	public void GetUTCDateTime_AfterReceivingCorruptedData_ShouldStillReturnTheCorrectTime() throws ParseException {
		//Arrange
		ITimeService timeService = ServiceManager.getInstance().getService(ITimeService.class);
		
		context.checking(new Expectations() 
		{{
				ignoring(nmeaService).start();
				ignoring(nmeaService).subscribe(GPRMC.class, INMEAListener.class.cast(timeService));
				ignoring(nmeaService).stop();
				ignoring(nmeaService).unsubscribe(GPRMC.class, INMEAListener.class.cast(timeService));
		}
		});
		
		ServiceManager.getInstance().startServices();
		
		long nanoTime = System.nanoTime();
		this.timeservice.onNewNMEASentence(new GPRMC(nanoTime, new StringBuilder("$GPRMC,033223.00,A,1805.48706,S,17632.90433,E,6.456,246.17,040818,,,D*7A")));
		this.wait(1);
		this.timeservice.onNewNMEASentence(new GPRMC(nanoTime + 1000000000L, new StringBuilder("$GPRMC,033224.00,A,1805.48763,S,17632.90251,E,6.869,254.64,040818,,,D*7B")));
		this.wait(1);
		this.timeservice.onNewNMEASentence(new GPRMC(nanoTime + 2000000000L, new StringBuilder("$GPRMC,033225.00,A,1805.48824,S,17632.90062,E,6.904,249.55,040818,,,D*70")));
		this.wait(1);
		GPRMC correctRMC = new GPRMC(nanoTime + 3000000000L, new StringBuilder("$GPRMC,033226.00,A,1805.48891,S,17632.89860,E,7.527,252.95,040818,,,D*75"));
		this.timeservice.onNewNMEASentence(correctRMC);
		
		GPRMC corruptedGprmc = new GPRMC(0, new StringBuilder("$GPRMC,193134.00,A,2219.93324,S,16649.39025,E,0.052,,,,,D*64"));
		
		try {
			// Act
			TimeService.class.cast(timeService).onNewNMEASentence(corruptedGprmc);
			
			long timeDiffWithSystem = Math.abs(correctRMC.getUtcDateTime().getTime() - timeService.getUTCDateTime().getTime());
			
			// Assert
			assertThat("Because the injection of a corrupted gprmc sentence should not be considered in the timeservice", timeDiffWithSystem, is(lessThan(80L)));
		
		} catch (SynchronizationException e1) {
			e1.printStackTrace();
		}
	}
	
	
	@Test
	public void getFormatedSyncCommand_WithACorrectDateTime_ShouldReturnTheCommadFormattedWithThisDate() {
		// Arrange
		Date aDate = new Date(1535868218178L);
		
		ITimeService timeService = ServiceManager.getInstance().getService(ITimeService.class);
		context.checking(new Expectations() 
		{{
				ignoring(nmeaService).start();
				ignoring(nmeaService).subscribe(GPRMC.class, INMEAListener.class.cast(timeService));
				ignoring(nmeaService).stop();
				ignoring(nmeaService).unsubscribe(GPRMC.class, INMEAListener.class.cast(timeService));
		}
		});
		
		// Act
		try {
			StringBuilder cmd = ToolBox.callPrivateMethod(StringBuilder.class, this.timeservice, "getFormatedSyncCommand", Date.class, aDate);
			
			// Assert
			assertEquals("echo \"2018/09/02 06:03:38.178+00:00\"",  cmd.toString());
		} catch (Throwable e) {
			fail("Should not throw an exception " + e.getMessage());
		}
	}
	
	
	@Test(expected=IllegalArgumentException.class)
	public void getFormatedSyncCommand_WithADateNull_ShouldThrowAnIllegalArgumentException() throws Throwable {
		// Arrange
		ITimeService timeService = ServiceManager.getInstance().getService(ITimeService.class);
		context.checking(new Expectations() 
		{{
				ignoring(nmeaService).start();
				ignoring(nmeaService).subscribe(GPRMC.class, INMEAListener.class.cast(timeService));
				ignoring(nmeaService).stop();
				ignoring(nmeaService).unsubscribe(GPRMC.class, INMEAListener.class.cast(timeService));
		}
		});

		Date aDate = null;

		// Act
		ToolBox.callPrivateMethod(StringBuilder.class, this.timeservice, "getFormatedSyncCommand", Date.class, aDate);
	}
	
	private void wait(int seconds) {
		try {
			TimeUnit.SECONDS.sleep(seconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
