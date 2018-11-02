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
import arbutus.test.ToolBox;
import arbutus.timeservice.ITimeService;
import arbutus.timeservice.SyncedTimeService;
import arbutus.virtuino.connectors.VirtuinoCommandType;
import arbutus.virtuino.service.IVirtuinoService;
import arbutus.virtuino.service.IVirtuinoServiceService;

public class PIRMeasurementTest {
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
	@Test(timeout=1000)
	public void Write_WhenTheValueChange__Write_ShouldBeCalled() throws Exception {
		// Arrange
		context.checking(new Expectations() 
		{{
				exactly(2).of(influxService).addPoint(with(any(String.class)), with(any(Date.class)), with(any(HashMap.class)));
				ignoring(influxService).stop();
				
				ignoring(virtuinoService).subscribe(with(any(String.class)), with(any(VirtuinoCommandType.class)), with(any(int.class)), with(any(BiConsumer.class)));
				ignoring(virtuinoService).stop();
		}});
		
		PIRMeasurement pir = new PIRMeasurement();
		
		long nano = System.nanoTime();
		pir.setPir(nano, 1.0f);
		
		// Act
		nano = System.nanoTime();
		pir.setPir(nano, 0.0f);
		
		// Assert
		context.assertIsSatisfied();
	}
	
	@SuppressWarnings("unchecked")
	@Test(timeout=1000)
	public void Write_WhenTheValueDoNotChange__Write_ShouldNotBeCalled() throws Exception {
		// Arrange
		context.checking(new Expectations() 
		{{
				exactly(1).of(influxService).addPoint(with(any(String.class)), with(any(Date.class)), with(any(HashMap.class)));
				ignoring(influxService).stop();
				
				ignoring(virtuinoService).subscribe(with(any(String.class)), with(any(VirtuinoCommandType.class)), with(any(int.class)), with(any(BiConsumer.class)));
				ignoring(virtuinoService).stop();
		}});
		
		PIRMeasurement pir = new PIRMeasurement();
		
		long nano = System.nanoTime();
		pir.setPir(nano, 1.0f);
		
		// Act
		nano = System.nanoTime();
		pir.setPir(nano, 1.0f);
		
		// Assert
		context.assertIsSatisfied();
	}
	
	@SuppressWarnings("unchecked")
	@Test(timeout=70000)
	public void Write_WhenTheValueDoNotChangeButALongTimeHappened__Write_ShouldBeCalled() throws Exception {
		// Arrange
		context.checking(new Expectations() 
		{{
				exactly(2).of(influxService).addPoint(with(any(String.class)), with(any(Date.class)), with(any(HashMap.class)));
				ignoring(influxService).stop();
				
				ignoring(virtuinoService).subscribe(with(any(String.class)), with(any(VirtuinoCommandType.class)), with(any(int.class)), with(any(BiConsumer.class)));
				ignoring(virtuinoService).stop();
		}});
		
		PIRMeasurement pir = new PIRMeasurement();
		
		long nano = System.nanoTime();
		pir.setPir(nano, 0.0f);
		
		ToolBox.wait(61);
		
		// Act
		nano = System.nanoTime();
		pir.setPir(nano, 0.0f);
		
		// Assert
		context.assertIsSatisfied();
	}

}
