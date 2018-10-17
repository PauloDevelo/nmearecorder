package arbutus.influxdb.measurement;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.io.InvalidClassException;
import java.util.Date;
import java.util.HashMap;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import arbutus.influxdb.IInfluxdbRepository;
import arbutus.influxdb.InfluxdbRepositoryServiceInterface;
import arbutus.nmea.sentences.GPRMC;
import arbutus.service.ServiceManager;
import arbutus.timeservice.ITimeService;
import arbutus.timeservice.SyncedTimeService;
import arbutus.timeservice.TimeServiceInterface;

public class NMEAMeasurementTest {

	private JUnit4Mockery context = null;
	private InfluxdbRepositoryServiceInterface influxService = null;
	private Date now = null;
	private long timeInNano = 0;
	
	@Before
	public void setUp() throws Exception {
		now = new Date(System.currentTimeMillis());
		timeInNano = System.nanoTime();
		
		this.context = new JUnit4Mockery() {{
		    setThreadingPolicy(new Synchroniser());
		}};
		
		this.influxService = context.mock(InfluxdbRepositoryServiceInterface.class);

		ServiceManager.getInstance().register(IInfluxdbRepository.class, this.influxService);
	}
	
	@After
	public void tearDown() throws Exception {
		ServiceManager srvMgr = ServiceManager.getInstance();
		
		srvMgr.unregister(ITimeService.class);
		srvMgr.unregister(IInfluxdbRepository.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setNMEASentence_WhenTimeSericeIsNotSync_ShouldNotWrite() throws InvalidClassException, ClassCastException {
		// Arrange
		TimeServiceInterface timeService = context.mock(TimeServiceInterface.class);
		ServiceManager.getInstance().register(ITimeService.class, timeService);
		
		context.checking(new Expectations() 
		{{
			exactly(1).of(timeService).isSynchonized();
			exactly(0).of(influxService).addPoint(with(any(String.class)), with(any(Date.class)), with(any(HashMap.class)));
		}});
		
		GPRMC rmc = new GPRMC(timeInNano, new StringBuilder("$GPRMC,033223.00,A,1805.48706,S,17632.90433,E,6.456,246.17,040818,,,D*7A"));
		
		MyNMEAMeasurement mymeasurement = new MyNMEAMeasurement();
		
		
		//Act
		mymeasurement.setNMEASentence(rmc);
		
		// Assert
		context.assertIsSatisfied();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void setNMEASentence_WhenTimeSericeIsSync_ShouldWrite() throws InvalidClassException, ClassCastException {
		// Arrange
		TimeServiceInterface timeService = new SyncedTimeService(this.timeInNano, this.now, false);
		ServiceManager.getInstance().register(ITimeService.class, timeService);
		
		context.checking(new Expectations() 
		{{	
			exactly(1).of(influxService).addPoint(with("testNMEAMeasurement"), with(now), with(any(HashMap.class)));
		}});
		
		GPRMC rmc = new GPRMC(timeInNano, new StringBuilder("$GPRMC,033223.00,A,1805.48706,S,17632.90433,E,6.456,246.17,040818,,,D*7A"));
		
		MyNMEAMeasurement mymeasurement = new MyNMEAMeasurement();
		
		//Act
		mymeasurement.setNMEASentence(rmc);
		
		// Assert
		context.assertIsSatisfied();
		assertThat(mymeasurement.getReceivedSentence(), is(equalTo(rmc)));
		assertThat(mymeasurement.getDataUTCDateTime(), is(equalTo(now)));
	}

	@InfluxMeasurementAnnotation(name="testNMEAMeasurement")
	public class MyNMEAMeasurement extends NMEAMeasurement<MyNMEAMeasurement, GPRMC>{
		
		@InfluxFieldAnnotation(name="testField")
		private float testField = (float)Math.PI;

		private GPRMC sentenceReceived = null;
		
		public MyNMEAMeasurement() throws InvalidClassException, ClassCastException {
			super(MyNMEAMeasurement.class);
		}
		
		public float getTestField() {
			return this.testField;
		}

		@Override
		protected void onSetNMEASentence(GPRMC sentence) {
			this.sentenceReceived = sentence;
		}
		
		public GPRMC getReceivedSentence() {
			return this.sentenceReceived;
		}
	}
}
