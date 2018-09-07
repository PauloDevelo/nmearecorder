package arbutus.nmea.service.connectors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat; 

import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;

import arbutus.nmea.service.connectors.TCPReader;

public class TCPReaderTest {
	@Test
	public void TCPReaderConstructor() {
		// Arrange 
		
		// Act
		TCPReader tcpReader = new TCPReader(this::fastconsume);
		
		// Assert
		assertNotNull("The TCPReader did no build", tcpReader);
	}
	
	@Test
	@Ignore
	public void RunTCPReader() {
		
		// Arrange 
		TCPReader tcpReader = new TCPReader(this::consume);
		Thread tcpReaderThread = new Thread(tcpReader);
				
		// Act
		tcpReaderThread.start();
		
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		tcpReader.setInterrupted(true);
		
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Assert
		assertNotNull("The TCPReader did no build", tcpReader);
		assertThat("TCPReader did send messages", counter, is(greaterThan(0)));
	}
	
	@Test
	@Ignore
	public void TCPReader_RunThenStop_ShouldStopImmediatelyAfterInterruption() {
		// Arrange 
		counter = 0;
		TCPReader tcpReader = new TCPReader(this::consume);
		Thread tcpReaderThread = new Thread(tcpReader);
				
		tcpReaderThread.start();
		
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		// Act
		int counterBefore = 0;
		synchronized(this) {
			counterBefore = counter;
			tcpReader.setInterrupted(true);
		}
		
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Assert
		assertTrue("The thread is not stopped", !tcpReaderThread.isAlive());
		assertTrue("Some messages should not arrive after the interruption.", Math.abs(counter - counterBefore) <= 1);
	}
	
	@Test
	@Ignore
	public void TCPReader_WithSlowConsumer_ShouldNotStackMoreThan20Call() {
		// Arrange 
		counter = 0;
		TCPReader tcpReader = new TCPReader(this::slowConsume);
		Thread tcpReaderThread = new Thread(tcpReader);
			
		// Act
		tcpReaderThread.start();
		
		try {
			TimeUnit.SECONDS.sleep(2);
			tcpReader.setInterrupted(true);
			
			while(tcpReaderThread.isAlive())
				TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// Assert
		assertTrue("The thread is not stopped", !tcpReaderThread.isAlive());
		//assertTrue("Seulement 20 messages doivent arriver", counter == 20);
	}
	
	
	private void slowConsume(Long nantime, StringBuilder sentence){
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		synchronized(this) {
			counter++;
			System.out.print(counter);
			System.out.println(" --> " + sentence);
		}
	}
	
	int counter = 0;
	private synchronized void consume(Long nantime, StringBuilder sentence) {
		counter++;
		System.out.println(sentence);
	}
	
	private void fastconsume(Long nantime, StringBuilder sentence) {
		System.out.println(sentence);
	}
}
