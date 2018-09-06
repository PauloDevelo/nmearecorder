package arbutus.nmea.sentences;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

public class VWMTWTest {
	@Test
	public void ParseACorrectSentence() {
		// Arrange
		long nano = System.nanoTime();
		
		// Act
		VWMTW mtw = new VWMTW(nano, new StringBuilder("$VWMTW,21.6,C*17"));
		
		// Assert
		assertThat("Because the Reception time should stay the same", mtw.getReceptionNanoTime(), is(equalTo(nano)));
		assertThat(mtw.getWaterTempCelcius(), is(equalTo(21.6f)));
	}
	
	@Test
	public void ParseAnEmptySentence() {
		// Arrange
		long nano = System.nanoTime();
		
		// Act
		VWMTW mtw = new VWMTW(nano, new StringBuilder("$VWMTW,,C*17"));
		
		// Assert
		assertThat("Because the Reception time should stay the same", mtw.getReceptionNanoTime(), is(equalTo(nano)));
		assertThat(mtw.getWaterTempCelcius(), is(equalTo(Float.NaN)));
	}
	
	@Test(expected=ArrayIndexOutOfBoundsException.class)
	public void ParseAnCorruptedSentence() {
		// Arrange
		long nano = System.nanoTime();
		
		// Act
		VWMTW dpt = new VWMTW(nano, new StringBuilder("$VWMTW,"));

		// Assert
		fail("An ArrayIndexOutOfBoundsExption should be thrown");
	}

}
