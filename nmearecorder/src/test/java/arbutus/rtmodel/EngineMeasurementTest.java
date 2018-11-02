package arbutus.rtmodel;

import java.util.Date;
import java.util.HashMap;
import java.util.function.BiConsumer;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import arbutus.influxdb.IInfluxdbRepository;
import arbutus.service.ServiceManager;
import arbutus.timeservice.ITimeService;
import arbutus.timeservice.SyncedTimeService;
import arbutus.virtuino.connectors.VirtuinoCommandType;
import arbutus.virtuino.service.IVirtuinoService;
import arbutus.virtuino.service.IVirtuinoServiceService;

public class EngineMeasurementTest {
	private Mockery context;
	private InfluxdbRepositoryServiceInterface influxService;
	private IVirtuinoServiceService virtuinoService;
	
	@Before
	public void setup() {
		ServiceManager srvMgr = ServiceManager.getInstance();
		
		srvMgr.register(ITimeService.class, new SyncedTimeService(true));
		
		this.context = new JUnit4Mockery() {{
		    setThreadingPolicy(new Synchroniser());
		}};
		
		this.influxService = context.mock(InfluxdbRepositoryServiceInterface.class);
		srvMgr.register(IInfluxdbRepository.class, this.influxService);
		
		this.virtuinoService = context.mock(IVirtuinoServiceService.class);
		srvMgr.register(IVirtuinoService.class, this.virtuinoService);
	}
	
	@After
	public void tearDown() {
		ServiceManager.getInstance().stopServices();
		
		ServiceManager.getInstance().unregister(ITimeService.class);
		ServiceManager.getInstance().unregister(IInfluxdbRepository.class);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void Write_WhenOneEngineValueIsMissing__Write_ShouldNotBeCalled() throws Exception {
		// Arrange
		context.checking(new Expectations() 
		{{
				exactly(0).of(influxService).addPoint(with(any(String.class)), with(any(Date.class)), with(any(HashMap.class)));
				ignoring(influxService).stop();
				
				ignoring(virtuinoService).subscribe(with(any(String.class)), with(any(VirtuinoCommandType.class)), with(any(int.class)), with(any(BiConsumer.class)));
				ignoring(virtuinoService).stop();
		}});
		
		GPSMeasurement gps = new GPSMeasurement();
		EngineMeasurement engine = new EngineMeasurement(gps);
		
		// Act
		long nano = System.nanoTime();
		engine.setAge(nano, 12f);
		engine.setBatVoltage(nano, 13f);
		engine.setConsumption(nano, 1.2f);
		engine.setCoolantTemp(nano, 78f);
		engine.setDieselQty(nano, 148f);
		engine.setExhaustTemp(nano, 35f);
		//engine.setRpm(nano, 1740f);
		
		// Assert
		context.assertIsSatisfied();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void Write_WhenOneEngineValueIsMissing__Write_ShouldNotBeCalled2() throws Exception {
		// Arrange
		context.checking(new Expectations() 
		{{
				exactly(0).of(influxService).addPoint(with(any(String.class)), with(any(Date.class)), with(any(HashMap.class)));
				ignoring(influxService).stop();
				
				ignoring(virtuinoService).subscribe(with(any(String.class)), with(any(VirtuinoCommandType.class)), with(any(int.class)), with(any(BiConsumer.class)));
				ignoring(virtuinoService).stop();
		}});
		
		GPSMeasurement gps = new GPSMeasurement();
		EngineMeasurement engine = new EngineMeasurement(gps);
		
		// Act
		long nano = System.nanoTime();
		engine.setAge(nano, 12f);
		engine.setBatVoltage(nano, 13f);
		engine.setConsumption(nano, 1.2f);
		engine.setCoolantTemp(nano, 78f);
		engine.setDieselQty(nano, 148f);
		//engine.setExhaustTemp(nano, 35f);
		engine.setRpm(nano, 1740f);
		
		// Assert
		context.assertIsSatisfied();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void Write_WhenOneEngineValueIsMissing__Write_ShouldNotBeCalled3() throws Exception {
		// Arrange
		context.checking(new Expectations() 
		{{
				exactly(0).of(influxService).addPoint(with(any(String.class)), with(any(Date.class)), with(any(HashMap.class)));
				ignoring(influxService).stop();
				
				ignoring(virtuinoService).subscribe(with(any(String.class)), with(any(VirtuinoCommandType.class)), with(any(int.class)), with(any(BiConsumer.class)));
				ignoring(virtuinoService).stop();
		}});
		
		GPSMeasurement gps = new GPSMeasurement();
		EngineMeasurement engine = new EngineMeasurement(gps);
		
		// Act
		long nano = System.nanoTime();
		engine.setAge(nano, 12f);
		engine.setBatVoltage(nano, 13f);
		engine.setConsumption(nano, 1.2f);
		engine.setCoolantTemp(nano, 78f);
		//engine.setDieselQty(nano, 148f);
		engine.setExhaustTemp(nano, 35f);
		engine.setRpm(nano, 1740f);
		
		// Assert
		context.assertIsSatisfied();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void Write_WhenOneEngineValueIsMissing__Write_ShouldNotBeCalled4() throws Exception {
		// Arrange
		context.checking(new Expectations() 
		{{
				exactly(0).of(influxService).addPoint(with(any(String.class)), with(any(Date.class)), with(any(HashMap.class)));
				ignoring(influxService).stop();
				
				ignoring(virtuinoService).subscribe(with(any(String.class)), with(any(VirtuinoCommandType.class)), with(any(int.class)), with(any(BiConsumer.class)));
				ignoring(virtuinoService).stop();
		}});
		
		GPSMeasurement gps = new GPSMeasurement();
		EngineMeasurement engine = new EngineMeasurement(gps);
		
		// Act
		long nano = System.nanoTime();
		engine.setAge(nano, 12f);
		engine.setBatVoltage(nano, 13f);
		engine.setConsumption(nano, 1.2f);
		//engine.setCoolantTemp(nano, 78f);
		engine.setDieselQty(nano, 148f);
		engine.setExhaustTemp(nano, 35f);
		engine.setRpm(nano, 1740f);
		
		// Assert
		context.assertIsSatisfied();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void Write_WhenOneEngineValueIsMissing__Write_ShouldNotBeCalled5() throws Exception {
		// Arrange
		context.checking(new Expectations() 
		{{
				exactly(0).of(influxService).addPoint(with(any(String.class)), with(any(Date.class)), with(any(HashMap.class)));
				ignoring(influxService).stop();
				
				ignoring(virtuinoService).subscribe(with(any(String.class)), with(any(VirtuinoCommandType.class)), with(any(int.class)), with(any(BiConsumer.class)));
				ignoring(virtuinoService).stop();
		}});
		
		GPSMeasurement gps = new GPSMeasurement();
		EngineMeasurement engine = new EngineMeasurement(gps);
		
		// Act
		long nano = System.nanoTime();
		engine.setAge(nano, 12f);
		engine.setBatVoltage(nano, 13f);
		//engine.setConsumption(nano, 1.2f);
		engine.setCoolantTemp(nano, 78f);
		engine.setDieselQty(nano, 148f);
		engine.setExhaustTemp(nano, 35f);
		engine.setRpm(nano, 1740f);
		
		// Assert
		context.assertIsSatisfied();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void Write_WhenOneEngineValueIsMissing__Write_ShouldNotBeCalled6() throws Exception {
		// Arrange
		context.checking(new Expectations() 
		{{
				exactly(0).of(influxService).addPoint(with(any(String.class)), with(any(Date.class)), with(any(HashMap.class)));
				ignoring(influxService).stop();
				
				ignoring(virtuinoService).subscribe(with(any(String.class)), with(any(VirtuinoCommandType.class)), with(any(int.class)), with(any(BiConsumer.class)));
				ignoring(virtuinoService).stop();
		}});
		
		GPSMeasurement gps = new GPSMeasurement();
		EngineMeasurement engine = new EngineMeasurement(gps);
		
		// Act
		long nano = System.nanoTime();
		engine.setAge(nano, 12f);
		//engine.setBatVoltage(nano, 13f);
		engine.setConsumption(nano, 1.2f);
		engine.setCoolantTemp(nano, 78f);
		engine.setDieselQty(nano, 148f);
		engine.setExhaustTemp(nano, 35f);
		engine.setRpm(nano, 1740f);
		
		// Assert
		context.assertIsSatisfied();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void Write_WhenOneEngineValueIsMissing__Write_ShouldNotBeCalled7() throws Exception {
		// Arrange
		context.checking(new Expectations() 
		{{
				exactly(0).of(influxService).addPoint(with(any(String.class)), with(any(Date.class)), with(any(HashMap.class)));
				ignoring(influxService).stop();
				
				ignoring(virtuinoService).subscribe(with(any(String.class)), with(any(VirtuinoCommandType.class)), with(any(int.class)), with(any(BiConsumer.class)));
				ignoring(virtuinoService).stop();
		}});
		
		GPSMeasurement gps = new GPSMeasurement();
		EngineMeasurement engine = new EngineMeasurement(gps);
		
		// Act
		long nano = System.nanoTime();
		//engine.setAge(nano, 12f);
		engine.setBatVoltage(nano, 13f);
		engine.setConsumption(nano, 1.2f);
		engine.setCoolantTemp(nano, 78f);
		engine.setDieselQty(nano, 148f);
		engine.setExhaustTemp(nano, 35f);
		engine.setRpm(nano, 1740f);
		
		// Assert
		context.assertIsSatisfied();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void Write_WhenNoneEngineValueIsMissing__Write_ShouldBeCalled() throws Exception {
		// Arrange
		context.checking(new Expectations() 
		{{
				exactly(1).of(influxService).addPoint(with("Engine"), with(any(Date.class)), with(any(HashMap.class)));
				ignoring(influxService).stop();
				
				ignoring(virtuinoService).subscribe(with(any(String.class)), with(any(VirtuinoCommandType.class)), with(any(int.class)), with(any(BiConsumer.class)));
				ignoring(virtuinoService).stop();
		}});
		
		GPSMeasurement gps = new GPSMeasurement();
		EngineMeasurement engine = new EngineMeasurement(gps);
		
		// Act
		long nano = System.nanoTime();
		engine.setAge(nano, 12f);
		engine.setBatVoltage(nano, 13f);
		engine.setConsumption(nano, 1.2f);
		engine.setCoolantTemp(nano, 78f);
		engine.setDieselQty(nano, 148f);
		engine.setExhaustTemp(nano, 35f);
		engine.setRpm(nano, 1740f);
		
		// Assert
		context.assertIsSatisfied();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void Write_WhenOneEngineValueIsNan_Write_ShouldBeCalled() throws Exception {
		// Arrange
		context.checking(new Expectations() 
		{{
				exactly(1).of(influxService).addPoint(with("Engine"), with(any(Date.class)), with(any(HashMap.class)));
				ignoring(influxService).stop();
				
				ignoring(virtuinoService).subscribe(with(any(String.class)), with(any(VirtuinoCommandType.class)), with(any(int.class)), with(any(BiConsumer.class)));
				ignoring(virtuinoService).stop();
		}});
		
		GPSMeasurement gps = new GPSMeasurement();
		EngineMeasurement engine = new EngineMeasurement(gps);
		
		// Act
		long nano = System.nanoTime();
		engine.setAge(nano, 12f);
		engine.setBatVoltage(nano, 13f);
		engine.setConsumption(nano, Float.NaN);
		engine.setCoolantTemp(nano, Float.NaN);
		engine.setDieselQty(nano, 148f);
		engine.setExhaustTemp(nano, 35f);
		engine.setRpm(nano, 1740f);
		
		// Assert
		context.assertIsSatisfied();
	}
	
	@Test
	public void ComputeConsumptionPer100Nm_WhenSOG_is_null_Should_Compute_Positive_infinite_value() {
		
	}
}
