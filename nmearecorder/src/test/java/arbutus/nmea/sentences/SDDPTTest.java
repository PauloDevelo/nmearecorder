package arbutus.nmea.sentences;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

public class SDDPTTest {
	
	@Test
	public void ParseACorrectSentence() {
		// Arrange
		long nano = System.nanoTime();
		
		// Act
		SDDPT dpt = new SDDPT(nano, new StringBuilder("$SDDPT,1.6,0.8,*74"));
		
		// Assert
		assertThat("Because the Reception time should stay the same", dpt.getReceptionNanoTime(), is(equalTo(nano)));
		assertThat(dpt.getDepth(), is(equalTo(1.6f)));
		assertThat(dpt.getOffsetFromTransducer(), is(equalTo(0.8f)));
	}
	
	@Test
	public void ParseAnEmptySentence() {
		// Arrange
		long nano = System.nanoTime();
		
		// Act
		SDDPT dpt = new SDDPT(nano, new StringBuilder("$SDDPT,,,*74"));
		
		// Assert
		assertThat("Because the Reception time should stay the same", dpt.getReceptionNanoTime(), is(equalTo(nano)));
		assertThat(dpt.getDepth(), is(equalTo(Float.NaN)));
		assertThat(dpt.getOffsetFromTransducer(), is(equalTo(Float.NaN)));
	}
	
	@Test(expected=ArrayIndexOutOfBoundsException.class)
	public void ParseAnCorruptedSentence() {
		// Arrange
		long nano = System.nanoTime();
		
		// Act
		new SDDPT(nano, new StringBuilder("$SDDPT,"));

		// Assert
		fail("An ArrayIndexOutOfBoundsExption should be thrown");
	}

}
