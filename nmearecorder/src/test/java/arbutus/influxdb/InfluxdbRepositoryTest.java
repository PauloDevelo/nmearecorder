package arbutus.influxdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import arbutus.service.ServiceState;

public class InfluxdbRepositoryTest {
	
	private InfluxdbRepository repository = null;
	
	@Before
	public void setup() {
		
	}
	
	@After
	public void tearDown() {
		repository.stop();
	}
	
	@Test
	public void BuildAnInfluxdbRepository() {
		// Arrange
		repository = new InfluxdbRepository(new InfluxdbContext("http://dumb:8086", "dbname"));
		
		// Act
		
		// Assert
		assertEquals("Because the service should be stopped after being built", ServiceState.STOPPED, repository.getState());
	}
	
	@Test
	public void Start_AnInaccessibleInluxDB_ShouldNotBeSarted(){
		// Arrange
		repository = new InfluxdbRepository(new InfluxdbContext("http://dumb:8086", "dbname"));
		
		// Act
		try {
			repository.start();
			fail("Because the database is inaccessible, we should not be able to start the service");
		}
		catch(Exception ex) {
			
		}
		
		// Assert
		assertEquals("Because the service should be stopped, influxdb being inaccessible from here", ServiceState.STOPPED, repository.getState());
	}
	
	@Test
	@Ignore
	public void Stop_ShouldBeStopped() throws Exception {
		// Arrange
		repository = new InfluxdbRepository(new InfluxdbContext("http://192.168.43.148:8086", "arbutustimeseries"));
		repository.start();
		
		// Act
		repository.stop();
		
		// Assert
		assertEquals("Because the service should be started", ServiceState.STOPPED, repository.getState());

	}
	
	@Test
	@Ignore
	public void Write_ShouldBeok() throws Exception{
		// Arrange
		repository = new InfluxdbRepository(new InfluxdbContext("http://dumb:8086", "dbname"));
		repository.start();
		HashMap<String, Float> fields = new HashMap<String, Float>();
		fields.put("test", (float)0.0);
		
		// Act
		repository.addPoint("test", new Date(System.currentTimeMillis()), fields);
		
		// Assert
		assertEquals("Because the service should be started from here", ServiceState.STARTED, repository.getState());
	}
}
