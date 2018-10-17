package arbutus.influxdb.measurement;

import static org.junit.Assert.assertEquals;

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
import arbutus.service.ServiceManager;
import arbutus.timeservice.ITimeService;
import arbutus.timeservice.SyncedTimeService;
import arbutus.timeservice.TimeServiceInterface;

public class ComputedMeasurementTest {
	
	private JUnit4Mockery context;
	private InfluxdbRepositoryServiceInterface influxService;
	private long now = 0;

	@Before
	public void setUp() throws Exception {
		now  = System.currentTimeMillis();
		
		this.context = new JUnit4Mockery() {{
		    setThreadingPolicy(new Synchroniser());
		}};
		
		this.influxService = context.mock(InfluxdbRepositoryServiceInterface.class);
		TimeServiceInterface timeService = new SyncedTimeService(System.nanoTime(), new Date(now));
		
		ServiceManager srvMgr = ServiceManager.getInstance();
		srvMgr.register(ITimeService.class, timeService);
		srvMgr.register(IInfluxdbRepository.class, this.influxService);
	}
	
	@After
	public void tearDown() throws Exception {
		ServiceManager srvMgr = ServiceManager.getInstance();
		
		srvMgr.unregister(ITimeService.class);
		srvMgr.unregister(IInfluxdbRepository.class);
	}
	
	@Test
	public void onMeasurementChanged_DependenciesInSameTimeRange_ShoudCompute() throws InvalidClassException, ClassCastException {
		// Arrange
		MyLambdaMeasurement measurementA = new MyLambdaMeasurement();
		measurementA.setDataDate(new Date(now));
		MyLambdaMeasurement measurementB = new MyLambdaMeasurement();
		measurementB.setDataDate(new Date(now + 10));
		MyLambdaMeasurement measurementC = new MyLambdaMeasurement();
		measurementC.setDataDate(new Date(now + 20));
		
		MyComputedMeasurement computedMeasurement = new MyComputedMeasurement(100, measurementA, measurementB, measurementC);
		
		context.checking(new Expectations() 
		{{
				exactly(1).of(influxService).addPoint(with("testComputedMeasurementAnnotation"), with(new Date(now + 20)), with(any(HashMap.class)));
		}});
		
		// Act
		measurementA.fireAChange();
		
		//Assert
		assertEquals("Because all the depedencies are in the same time range, the compute function should be called", 1, computedMeasurement.getNbComputeCall());
		assertEquals("Because the computed measurement should get the data date of the most recent dependency", new Date(now + 20), computedMeasurement.getDataUTCDateTime());
		context.assertIsSatisfied();
	}
	
	@Test
	public void onMeasurementChanged_DependenciesNotInSameTimeRange_ShoudNotCompute() throws InvalidClassException, ClassCastException {
		// Arrange
		MyLambdaMeasurement measurementA = new MyLambdaMeasurement();
		measurementA.setDataDate(new Date(now));
		MyLambdaMeasurement measurementB = new MyLambdaMeasurement();
		measurementB.setDataDate(new Date(now + 10));
		MyLambdaMeasurement measurementC = new MyLambdaMeasurement();
		measurementC.setDataDate(new Date(now + 110));
		
		MyComputedMeasurement computedMeasurement = new MyComputedMeasurement(100, measurementA, measurementB, measurementC);
		
		context.checking(new Expectations() 
		{{
			exactly(0).of(influxService).addPoint(with(any(String.class)), with(any(Date.class)), with(any(HashMap.class)));
		}});
		
		// Act
		measurementA.fireAChange();
		
		//Assert
		assertEquals("Because all the depedencies are not in the same time range, the compute function should not be called", 0, computedMeasurement.getNbComputeCall());
		context.assertIsSatisfied();
	}
	
	@Test
	public void onMeasurementChanged_OneDependencyGotNullDataDate_ShoudNotCompute() throws InvalidClassException, ClassCastException {
		// Arrange
		MyLambdaMeasurement measurementA = new MyLambdaMeasurement();
		//measurementA.setDataDate(new Date(now));
		MyLambdaMeasurement measurementB = new MyLambdaMeasurement();
		measurementB.setDataDate(new Date(now + 10));
		MyLambdaMeasurement measurementC = new MyLambdaMeasurement();
		measurementC.setDataDate(new Date(now + 20));
		
		MyComputedMeasurement computedMeasurement = new MyComputedMeasurement(100, measurementA, measurementB, measurementC);
		
		context.checking(new Expectations() 
		{{
			exactly(0).of(influxService).addPoint(with(any(String.class)), with(any(Date.class)), with(any(HashMap.class)));
		}});
		
		// Act
		measurementA.fireAChange();
		
		//Assert
		assertEquals("Because a depedency has a null date, the compute function should not be called", 0, computedMeasurement.getNbComputeCall());
		context.assertIsSatisfied();
	}
	
	protected class MyLambdaMeasurement extends Measurement{
		public void fireAChange() {
			this.fireMeasurementChanged();
		}
		
		public void setDataDate(Date date) {
			this.setDataUTCDateTime(date);
		}
	}
	
	@InfluxMeasurementAnnotation(name="testComputedMeasurementAnnotation")
	protected class MyComputedMeasurement extends ComputedMeasurement<MyComputedMeasurement>{
		@InfluxFieldAnnotation(name="computedValue")
		private float computedValue = (float)Math.PI;
		
		private int nbComputeCall = 0;
		
		public MyComputedMeasurement(long thresholdInMilliSecond, Measurement measurementA, Measurement measurementB, Measurement measurementC) throws InvalidClassException, ClassCastException {
			super(thresholdInMilliSecond, MyComputedMeasurement.class);
			
			this.addDependency(measurementA);
			this.addDependency(measurementB);
			this.addDependency(measurementC);
		}
		
		public float getComputedValue() {
			return computedValue;
		}
		
		public int getNbComputeCall() {
			return this.nbComputeCall;
		}

		@Override
		protected void compute() {
			this.nbComputeCall++;
		}
	}
}
