package arbutus.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class UtilsTests {
	
	@Test(expected=IllegalArgumentException.class)
	public void execCommandSync_EmptyCommand_ShouldThrowAnIllegalArgumentException() {
		// Arrange 
		StringBuilder cmd = new StringBuilder();
		
		// Act
		Utils.execCommandSync(cmd);
		
		// Assert
		fail("Executing an empting command should throw an IllegalArgumentException");
	}
	
	@Test
	public void execCommandSync_java_version_ShoudlReturnTrue() {
		// Arrange 
		StringBuilder cmd = new StringBuilder("java -version");
		
		// Act
		boolean success = Utils.execCommandSync(cmd);
		
		// Assert
		assertTrue("java -version should execute succesfully", success);
	}

}
