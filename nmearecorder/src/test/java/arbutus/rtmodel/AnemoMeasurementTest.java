package arbutus.rtmodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.InvalidClassException;
import java.util.Date;
import java.util.HashMap;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import arbutus.influxdb.IInfluxdbRepository;
import arbutus.nmea.sentences.WIMWV;
import arbutus.service.ServiceManager;
import arbutus.timeservice.ITimeService;
import arbutus.timeservice.SyncedTimeService;

public class AnemoMeasurementTest {
	private Mockery context;
	private InfluxdbRepositoryServiceInterface influxService;
	private final Date dataDate = new Date(1533353597000L);
	private AnemoMeasurement anemo;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setup() throws InvalidClassException, ClassCastException {
		this.context = new JUnit4Mockery() {{
		    setThreadingPolicy(new Synchroniser());
		}};
		
		this.influxService = context.mock(InfluxdbRepositoryServiceInterface.class);
		
		this.context.checking(new Expectations() 
		{{
			atLeast(1).of(influxService).addPoint(with("Anemo"), with(dataDate), with(any(HashMap.class)));
		}});
		
		ServiceManager srvMgr = ServiceManager.getInstance();
		srvMgr.register(ITimeService.class, new SyncedTimeService(1000, dataDate, false));
		srvMgr.register(IInfluxdbRepository.class, this.influxService);
		
		this.anemo = new AnemoMeasurement();
	}
	
	@After
	public void tearDown() {
		ServiceManager.getInstance().unregister(ITimeService.class);
		ServiceManager.getInstance().unregister(IInfluxdbRepository.class);
	}

	@Test
	public void RunArbutus_WithASudenStrongSpeed_ShouldCleanSpikes() throws Exception {
		// Arrange
		WIMWV mwv = new WIMWV(1000, new StringBuilder("$WIMWV,299,R,2,N,A*0D"));
		this.anemo.setNMEASentence(mwv);
		
		mwv = new WIMWV(1000, new StringBuilder("$WIMWV,299,R,1,N,A*0D"));
		this.anemo.setNMEASentence(mwv);
		
		mwv = new WIMWV(1000, new StringBuilder("$WIMWV,299,R,3,N,A*0D"));
		this.anemo.setNMEASentence(mwv);
		
		mwv = new WIMWV(1000, new StringBuilder("$WIMWV,299,R,0.5,N,A*0D"));
		this.anemo.setNMEASentence(mwv);
		
		
		// Act
		mwv = new WIMWV(1000, new StringBuilder("$WIMWV,299,R,90,N,A*0D"));
		this.anemo.setNMEASentence(mwv);
		
		// Assert
		assertThat(this.anemo.getRelWindSpeed(), is(equalTo(Float.NaN)));
		
	}
}
