package arbutus.nmea.sentences;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

public class VWVHWTest {
	
	@Test
	public void ParseACorrectSentence() {
		// Arrange
		long nano = System.nanoTime();
		
		// Act
		VWVHW vhw = new VWVHW(nano, new StringBuilder("$VWVHW,,,,,261,N,3.2,K*4D"));
		
		// Assert
		assertThat("Because the Reception time should stay the same", vhw.getReceptionNanoTime(), is(equalTo(nano)));
		assertThat(vhw.getHeadingDegT(), is(equalTo(Float.NaN)));
		assertThat(vhw.getStwKn(), is(equalTo(261f)));
	}
	
	@Test
	public void ParseAnEmptySentence() {
		// Arrange
		long nano = System.nanoTime();
		
		// Act
		VWVHW vhw = new VWVHW(nano, new StringBuilder("$VWVHW,,,,,,N,,K*4D"));
		
		// Assert
		assertThat("Because the Reception time should stay the same", vhw.getReceptionNanoTime(), is(equalTo(nano)));
		
	}
	
	@Test(expected=ArrayIndexOutOfBoundsException.class)
	public void ParseAnCorruptedSentence() {
		long nano = System.nanoTime();
		
		// Act
		VWVHW vhw = new VWVHW(nano, new StringBuilder("$VWVHW,,,,,"));
		
		// Assert
		assertThat("Because the Reception time should stay the same", vhw.getReceptionNanoTime(), is(equalTo(nano)));
		assertThat(vhw.getHeadingDegT(), is(equalTo(Float.NaN)));
		assertThat(vhw.getStwKn(), is(equalTo(Float.NaN)));
	}

}
