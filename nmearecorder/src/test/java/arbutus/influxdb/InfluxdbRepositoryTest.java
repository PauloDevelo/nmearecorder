package arbutus.influxdb;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import arbutus.service.ServiceState;

public class InfluxdbRepositoryTest {
	
	private InfluxdbRepository repository = null;
	
	@Before
	public void setup() {
		repository = new InfluxdbRepository();
	}
	
	@After
	public void tearDown() {
		repository.stop();
	}
	
	@Test
	public void BuildAnInfluxdbRepository() {
		// Arrange
		
		// Act
		
		// Assert
		assertEquals("Because the service should be stopped after being built", ServiceState.STOPPED, repository.getState());
	}
	
	@Test
	public void Start_ShouldBeSarted() {
		// Arrange
		
		// Act
		repository.start();
		
		// Assert
		assertEquals("Because the service should be started", ServiceState.STARTED, repository.getState());
	}
	
	@Test
	public void Stop_ShouldBeStopped() {
		// Arrange
		repository.start();
		
		// Act
		repository.stop();
		
		// Assert
		assertEquals("Because the service should be started", ServiceState.STOPPED, repository.getState());
	}
	
	@Test
	public void Write_ShouldBeok() {
		// Arrange
		repository.start();
		HashMap<String, Float> fields = new HashMap<String, Float>();
		fields.put("test", (float)0.0);
		
		// Act
		repository.addPoint("test", new Date(System.currentTimeMillis()), fields);
		
		// Assert
		assertEquals("Because the service should be started", ServiceState.STARTED, repository.getState());
	}

}
