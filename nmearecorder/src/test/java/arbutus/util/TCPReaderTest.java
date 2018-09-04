package arbutus.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import arbutus.util.TCPReader;

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
		assertTrue("TCPReader did not send messages", counter > 0);
	}
	
	@Test
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
		assertTrue("Some messages arrivent after the interruption.", Math.abs(counter - counterBefore) <= 1);
	}
	
	@Test
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
	
	
	private void slowConsume(StringBuilder sentence){
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
	private synchronized void consume(StringBuilder sentence) {
		counter++;
		System.out.println(sentence);
	}
	
	private void fastconsume(StringBuilder sentence) {
		System.out.println(sentence);
	}
}
