package arbutus.nmea.sentences;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

public class WIMWVTest {
	
	@Test
	public void ParseACorrectSentence() {
		// Arrange
		long nano = System.nanoTime();
		
		// Act
		WIMWV mwv = new WIMWV(nano, new StringBuilder("$WIMWV,257,R,5.8,N,A*30"));
		
		// Assert
		assertThat("Because the Reception time should stay the same", mwv.getReceptionNanoTime(), is(equalTo(nano)));
		assertThat(mwv.getWindAngle(), is(equalTo(257f)));
		assertThat(mwv.getWindSpeedKn(), is(equalTo(5.8f)));
	}
	
	@Test
	public void ParseAnEmptySentence() {
		// Arrange
		long nano = System.nanoTime();
		
		// Act
		WIMWV mwv = new WIMWV(nano, new StringBuilder("$WIMWV,,R,,N,A*30"));
		
		// Assert
		assertThat("Because the Reception time should stay the same", mwv.getReceptionNanoTime(), is(equalTo(nano)));
		assertThat(mwv.getWindAngle(), is(equalTo(Float.NaN)));
		assertThat(mwv.getWindSpeedKn(), is(equalTo(Float.NaN)));
	}
	
	@Test(expected=ArrayIndexOutOfBoundsException.class)
	public void ParseAnCorruptedSentence() {
		// Arrange
		long nano = System.nanoTime();
		
		// Act
		new WIMWV(nano, new StringBuilder("$WIMWV,"));

		// Assert
		fail("An ArrayIndexOutOfBoundsExption should be thrown");
	}

}
