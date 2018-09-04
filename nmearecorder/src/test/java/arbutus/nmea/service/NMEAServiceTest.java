package arbutus.nmea.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import arbutus.nmea.sentences.GPRMC;
import arbutus.nmea.sentences.NMEASentence;
import arbutus.nmea.service.INMEAListener;
import arbutus.nmea.service.NMEAService;
import arbutus.service.ServiceState;

public class NMEAServiceTest {
	NMEAService nmeaService = null;
	
	@Before
	public void setup() {
		nmeaService = new NMEAService();
	}
	
	@After
	public void tearDown(){
		nmeaService.stop();
	}
	
	@Test
	public void Constructor_ShouldBuildAndGetStateStopped() {
		// Arrange
		
		// Act
		
		// Assert
		assertEquals("NMEAService should be stopped after being built.", ServiceState.STOPPED, nmeaService.getState());
	}
	
	@Test
	public void Start_ShouldStart() {
		// Arrange
		
		// Act
		nmeaService.start();
		
		// Assert
		assertEquals("NMEAService should be started", ServiceState.STARTED, nmeaService.getState());
	}
	
	@Test
	public void Register_GPRMC_ShouldReceiveGPRMC() {
		// Arrange
		GPRMCReceiver receiver = new GPRMCReceiver();
		nmeaService.subscribe(GPRMC.class, receiver);
		
		// Act
		nmeaService.start();
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Arrange
		assertTrue("The receiver should have received at least one message.", receiver.counter > 0);
	}
	
	@Test
	public void Unsubscribe_GPRMC_ShouldReceiveGPRMC() {
		// Arrange
		GPRMCReceiver receiver = new GPRMCReceiver();
		nmeaService.subscribe(GPRMC.class, receiver);
		
		// Act
		nmeaService.start();
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		nmeaService.unsubscribe(GPRMC.class, receiver);
		int counter = receiver.counter;
		
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Arrange
		assertEquals("This counter should not increase because the receiver is unsubscribed", counter, receiver.counter);
	}
	
	class GPRMCReceiver implements INMEAListener{
		public int counter = 0;
		@Override
		public void onNewNMEASentence(NMEASentence sentence) {
			counter++;
		}
		
		
	}
}
