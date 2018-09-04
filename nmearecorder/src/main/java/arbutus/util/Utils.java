package arbutus.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class Utils {
	private static Logger log = Logger.getLogger(Utils.class);
	
	public static boolean execCommandSync(StringBuilder cmd, long timeOutInSecond) throws IOException, InterruptedException, IllegalStateException  {
		boolean success = false;
		Process subProcess = null;
		
		if(OSValidator.isUnix()) {
			subProcess = new ProcessBuilder().command("bash", "-c", cmd.toString()).start();
		}
		else {
			throw new IllegalStateException("This operating system " + OSValidator.getOS() + " is not supported. The command " + cmd + " cannot be executed");
		}
		
		if (subProcess.waitFor(timeOutInSecond, TimeUnit.SECONDS)) {
			String s;
	        BufferedReader br = new BufferedReader(new InputStreamReader(subProcess.getInputStream()));
	        while ((s = br.readLine()) != null)
	            log.info(s);
	        
	        success = subProcess.exitValue() == 0;
		}
		else {
			log.error("The command " + cmd + " did not finish in a timely manner.");
		}
        
        subProcess.destroy();
			
		return success;
	}

}
