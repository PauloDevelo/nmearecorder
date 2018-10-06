package arbutus.nmea.sentences;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

public class HCHDGTest {
	
	@Test
	public void ParseACorrectSentence() {
		// Arrange
		long nano = System.nanoTime();
		
		// Act
		HCHDG hdg = new HCHDG(nano, new StringBuilder("$HCHDG,233.2,,,12.2,E*18"));
		
		// Assert
		assertThat("Because the Reception time should stay the same", hdg.getReceptionNanoTime(), is(equalTo(nano)));
		assertThat(hdg.getMagDevDeg(), is(equalTo(Float.NaN)));
		assertThat(hdg.getMagHdgDeg(), is(equalTo(233.2f)));
		assertThat(hdg.getMagVarDeg(), is(equalTo(12.2f)));
	}
	
	@Test
	public void ParseAnEmptySentence() {
		// Arrange
		long nano = System.nanoTime();
		
		// Act
		HCHDG hdg = new HCHDG(nano, new StringBuilder("$HCHDG,,,,,E*18"));
		
		// Assert
		assertThat("Because the Reception time should stay the same", hdg.getReceptionNanoTime(), is(equalTo(nano)));
		assertThat(hdg.getMagDevDeg(), is(equalTo(Float.NaN)));
		assertThat(hdg.getMagHdgDeg(), is(equalTo(Float.NaN)));
		assertThat(hdg.getMagVarDeg(), is(equalTo(Float.NaN)));
	}
	
	@Test(expected=ArrayIndexOutOfBoundsException.class)
	public void ParseAnCorruptedSentence() {
		// Arrange
		long nano = System.nanoTime();
		
		// Act
		new HCHDG(nano, new StringBuilder("$HCHDG,,"));
		
		// Assert
		fail("An ArrayIndexOutOfBoundsExption should be thrown");
	}

}
