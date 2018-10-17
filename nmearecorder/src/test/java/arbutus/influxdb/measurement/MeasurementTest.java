package arbutus.influxdb.measurement;

import static org.junit.Assert.assertTrue;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.Before;
import org.junit.Test;

public class MeasurementTest {
	
	private JUnit4Mockery context = null;
	private IMeasurementListener listener = null;
	private MyMeasurement measurement = null;
	
	@Before
	public void setup() {
		context = new JUnit4Mockery() {{
		    setThreadingPolicy(new Synchroniser());
		}};
		
		this.listener = context.mock(IMeasurementListener.class);
		
		this.measurement = new MyMeasurement();
	}
	
	@Test
	public void Remove_AnUnknownListener_ShouldDoNothing() {
		// Arrange
		
		// Act
		measurement.removeListener(listener);
		
		// Assert
		assertTrue("Because removing an unknown listener should not stop the program", true);
	}
	
	@Test
	public void FireAChange_ShouldNotifyListener() {
		// Arrange
		measurement.addListener(listener);
		
		context.checking(new Expectations() 
		{{
			exactly(1).of(listener).onMeasurementChanged(with(measurement));
		}});
		
		// Act
		measurement.triggerAChange();
		
		// Assert
		context.assertIsSatisfied();	
	}
	
	@Test
	public void FireAChange_AfterRemovingTheListener_ShouldNotNotifyListener() {
		// Arrange
		measurement.addListener(listener);
		
		context.checking(new Expectations() 
		{{
			exactly(0).of(listener).onMeasurementChanged(with(measurement));
		}});
		
		// Act
		measurement.removeListener(listener);
		measurement.triggerAChange();
		
		// Assert
		context.assertIsSatisfied();	
	}
	
	@Test
	public void FireAChange_AfterAddingTheListenerTwice_ShouldNotifyTheListenerOnce() {
		// Arrange
		context.checking(new Expectations() 
		{{
			exactly(1).of(listener).onMeasurementChanged(with(measurement));
		}});
		
		// Act
		measurement.addListener(listener);
		measurement.addListener(listener);
		measurement.triggerAChange();
		
		// Assert
		context.assertIsSatisfied();	
	}
	
	public class MyMeasurement extends Measurement{
		public void triggerAChange() {
			this.fireMeasurementChanged();
		}
	}

}
