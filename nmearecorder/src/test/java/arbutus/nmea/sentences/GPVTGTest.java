package arbutus.nmea.sentences;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

public class GPVTGTest {
	
	@Test
	public void ParseACorrectSentence() {
		// Arrange
		long nano = System.nanoTime();
		
		// Act
		GPVTG vtg = new GPVTG(nano, new StringBuilder("$GPVTG,246.17,T,,M,6.456,N,11.956,K,D*05"));
		
		// Assert
		assertThat("Because the Reception time should stay the same", vtg.getReceptionNanoTime(), is(equalTo(nano)));
		assertThat(vtg.getSpeedKn(), is(equalTo(6.456f)));
		assertThat(vtg.getTrackDegT(), is(equalTo(246.17f)));
	}
	
	@Test
	public void ParseAnEmptySentence() {
		// Arrange
		long nano = System.nanoTime();
		
		// Act
		GPVTG vtg = new GPVTG(nano, new StringBuilder("$GPVTG,,T,,M,,N,,K,D*05"));
		
		// Assert
		assertThat("Because the Reception time should stay the same", vtg.getReceptionNanoTime(), is(equalTo(nano)));
		assertThat(vtg.getSpeedKn(), is(equalTo(Float.NaN)));
		assertThat(vtg.getTrackDegT(), is(equalTo(Float.NaN)));
	}
	
	@Test(expected=ArrayIndexOutOfBoundsException.class)
	public void ParseAnCorruptedSentence() {
		// Arrange
		long nano = System.nanoTime();
		
		// Act
		new GPVTG(nano, new StringBuilder("$GPVTG,,T,,M"));
		
		// Assert
		fail("An ArrayIndexOutOfBoundsExption should be thrown");
	}

}
