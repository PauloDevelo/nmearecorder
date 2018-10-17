package arbutus.influxdb.measurement;

import static org.junit.Assert.*;

import java.io.InvalidClassException;
import java.time.Instant;
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
import arbutus.timeservice.TimeService;


public class InfluxMeasurementTest {
	private JUnit4Mockery context = null;
	private InfluxdbRepositoryServiceInterface influxService = null;
	private Date now = null;
	
	@Before
	public void setUp() throws Exception {
		this.context = new JUnit4Mockery() {{
		    setThreadingPolicy(new Synchroniser());
		}};
		
		this.influxService = context.mock(InfluxdbRepositoryServiceInterface.class);
		
		ServiceManager srvMgr = ServiceManager.getInstance();
		
		srvMgr.register(ITimeService.class, new TimeService());
		srvMgr.register(IInfluxdbRepository.class, this.influxService);
		
		now = new Date(Instant.now().toEpochMilli());
	}
	
	@After
	public void tearDown() throws Exception {
		ServiceManager srvMgr = ServiceManager.getInstance();
		
		srvMgr.unregister(ITimeService.class);
		srvMgr.unregister(IInfluxdbRepository.class);
	}

	@Test(expected=ClassCastException.class)
	public void Contructor_WithATypeWhichIsNotASubClassOfInfluxMeasurement_ShouldThrowAClassCastException() throws InvalidClassException {
		// Arrange
		
		//Act
		new MyDumbInfluxMeasurement();
	}
	
	@Test(expected=InvalidClassException.class)
	public void Contructor_WithASubClassWithoutInfluxMeasurementAnnotation_ShouldThrowAnInvalidClassException() throws InvalidClassException {
		// Arrange
		
		//Act
		new InfluxMeasurementWithoutInfluxMeasurementAnnotation();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void write_ASimpleField_TheGetterShouldBeCallAndTheAddPointFunctionOfTheRepo() throws InvalidClassException, ClassCastException {
		// Arrange
		context.checking(new Expectations() 
		{{
				exactly(1).of(influxService).addPoint(with("testMeasurementAnnotation"), with(now), with(any(HashMap.class)));
		}});
		
		MyInfluxMeasurement mymeasurement = new MyInfluxMeasurement();
		
		//Act
		mymeasurement.triggerWrite();
		
		// Assert
		assertEquals("Because the write function will call the getter once to get the value to write", 1, mymeasurement.getNumberOfGetterCall());
		assertEquals("Because the write function will call the getter B once to get the value to write", 1, mymeasurement.getNumberOfGetterBCall());
		context.assertIsSatisfied();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void write_AMeasurmentWithoutDefinedField_NoWriteShouldBePerformed() throws InvalidClassException, ClassCastException {
		// Arrange
		context.checking(new Expectations() 
		{{
			exactly(0).of(influxService).addPoint(with(any(String.class)), with(any(Date.class)), with(any(HashMap.class)));
		}});
		
		MyInfluxMeasurementWithoutField mymeasurement = new MyInfluxMeasurementWithoutField();
		
		//Act
		mymeasurement.triggerWrite();
		
		// Assert
		assertEquals("Because the write function should not be called, the getter should not be call neither", 0, mymeasurement.getNumberOfGetterCall());
		context.assertIsSatisfied();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void write_AMeasurmentWithoutGetter_WriteShouldBePerformedForTheOtherField() throws InvalidClassException, ClassCastException {
		// Arrange
		context.checking(new Expectations() 
		{{
			exactly(1).of(influxService).addPoint(with("testMeasurementAnnotation"), with(now), with(any(HashMap.class)));
		}});
		
		MyInfluxMeasurementWithoutGetter mymeasurement = new MyInfluxMeasurementWithoutGetter();
		
		//Act
		mymeasurement.triggerWrite();
		
		// Assert
		assertEquals("Because the write function will call the getter B once to get the value to write", 1, mymeasurement.getNumberOfGetterBCall());
		context.assertIsSatisfied();
	}
	
	public class DumbClass{
		
	}
	
	@InfluxMeasurementAnnotation(name="dumb")
	public class MyDumbInfluxMeasurement extends InfluxMeasurement<DumbClass>{
		public MyDumbInfluxMeasurement() throws InvalidClassException, ClassCastException {
			super(DumbClass.class);
		}
	}
	
	public class InfluxMeasurementWithoutInfluxMeasurementAnnotation extends InfluxMeasurement<InfluxMeasurementWithoutInfluxMeasurementAnnotation>{
		public InfluxMeasurementWithoutInfluxMeasurementAnnotation() throws InvalidClassException, ClassCastException {
			super(InfluxMeasurementWithoutInfluxMeasurementAnnotation.class);
		}
	}
	
	@InfluxMeasurementAnnotation(name="testMeasurementAnnotation")
	public class MyInfluxMeasurement extends InfluxMeasurement<MyInfluxMeasurement>{
		@InfluxFieldAnnotation(name="myfield")
		private float myField = (float)Math.PI;
		
		@InfluxFieldAnnotation(name="myFieldB")
		private float myFieldB = 2f * (float)Math.PI;
		
		private int numberOfGetterCall = 0;
		private int numberOfGetterBCall = 0;
		
		public MyInfluxMeasurement() throws InvalidClassException, ClassCastException {
			super(MyInfluxMeasurement.class);
			
			this.setDataUTCDateTime(now);
		}
		
		public float getMyField() {
			numberOfGetterCall++;
			return this.myField;
		}
		
		public float getMyFieldB() {
			numberOfGetterBCall++;
			return this.myFieldB;
		}
		
		public int getNumberOfGetterCall() {
			return numberOfGetterCall;
		}
		
		public int getNumberOfGetterBCall() {
			return numberOfGetterBCall;
		}
		
		public void triggerWrite() {
			this.write();
		}
	}
	
	@InfluxMeasurementAnnotation(name="testMeasurementAnnotation")
	public class MyInfluxMeasurementWithoutField extends InfluxMeasurement<MyInfluxMeasurementWithoutField>{
		private float myField = (float)Math.PI;
		
		private int numberOfGetterCall = 0;
		
		public MyInfluxMeasurementWithoutField() throws InvalidClassException, ClassCastException {
			super(MyInfluxMeasurementWithoutField.class);
			
			this.setDataUTCDateTime(now);
		}
		
		public float getMyField() {
			numberOfGetterCall++;
			return this.myField;
		}
		
		public int getNumberOfGetterCall() {
			return numberOfGetterCall;
		}
		
		public void triggerWrite() {
			this.write();
		}
	}
	
	@InfluxMeasurementAnnotation(name="testMeasurementAnnotation")
	public class MyInfluxMeasurementWithoutGetter extends InfluxMeasurement<MyInfluxMeasurementWithoutGetter>{
		@InfluxFieldAnnotation(name="myfield")
		private float myField = (float)Math.PI;
		
		@InfluxFieldAnnotation(name="myFieldB")
		private float myFieldB = 2f * (float)Math.PI;
		
		private int numberOfGetterBCall = 0;

		public MyInfluxMeasurementWithoutGetter() throws InvalidClassException, ClassCastException {
			super(MyInfluxMeasurementWithoutGetter.class);
			
			this.setDataUTCDateTime(now);
		}
		
		public void triggerWrite() {
			this.write();
		}
		
		public float getMyFieldB() {
			numberOfGetterBCall++;
			return this.myFieldB;
		}
		
		public int getNumberOfGetterBCall() {
			return numberOfGetterBCall;
		}
	}

}


