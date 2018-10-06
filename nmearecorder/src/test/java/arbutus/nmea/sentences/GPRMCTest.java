package arbutus.nmea.sentences;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.junit.Test;

import arbutus.nmea.sentences.GPRMC;

public class GPRMCTest {
	@Test
	public void ParseACorrectGPRMCSentence() {
		// Arrange
		SimpleDateFormat format = new SimpleDateFormat("dd'/'MM'/'yyyy HH':'mm':'ssX");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		StringBuilder nmeaSentence = new StringBuilder("$GPRMC,193134.00,A,2219.93324,S,16649.39025,E,0.052,,230818,,,D*64");

		// Act
		GPRMC gprmc = new GPRMC(0, nmeaSentence);
		
		//Assert
		assertTrue("Because the sentence was parsed correctly", true);
		assertEquals("Because the data is valid", Status.DataValid,  gprmc.getStatus());
		assertEquals("Because the latitude is -22.332221", -22.332221,  gprmc.getLatitudeDegDec(), 0.000001);
		assertEquals("Because the longitude is 166.823171", 166.82317,  gprmc.getLongitudeDegDec(), 0.00001);
		assertTrue("Because the magnetic variation is unknown", Float.isNaN(gprmc.getMagVarDeg()));
		assertEquals("Because SOG is 0.052", 0.052,  gprmc.getSogKnot(), 0.0001);
		assertTrue("Because we are not on track", Float.isNaN(gprmc.getTmgDegT()));
		assertEquals("Because utc date is 23/08/2018 19:31:34Z", "23/08/2018 19:31:34Z",  format.format(gprmc.getUtcDateTime()));
	}
	
	@Test
	public void ParseASentence_WithIncorrectDate_ShouldThrowAnException()throws ParseException {
		// Arrange
		SimpleDateFormat format = new SimpleDateFormat("dd'/'MM'/'yyyy HH':'mm':'ssX");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		StringBuilder nmeaSentence = new StringBuilder("$GPRMC,193134.00,A,2219.93324,S,16649.39025,E,0.052,,3081,,,D*64");
		
		// Act
		@SuppressWarnings("unused")
		GPRMC gprmc = new GPRMC(0, nmeaSentence);
		
		// Arrange
		assertNull("Because the date cannot be parsed, it should be null.", gprmc.getUtcDateTime());
	}

}
