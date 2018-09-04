package arbutus.util;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class UtilsTest {
	
	@Test(expected=IllegalStateException.class)
	public void execCommandSync_EmptyCommand_ShouldThrowAnIllegalArgumentException() {
		// Arrange 
		StringBuilder cmd = new StringBuilder();
		
		// Act
		try {
			Utils.execCommandSync(cmd, 4);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
		// Assert
		fail("Executing under Windows is not supported");
	}
}
