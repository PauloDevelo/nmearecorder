package arbutus.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

public class Utils {
	private static Logger log = Logger.getLogger(Utils.class);
	
	public static boolean execCommandSync(StringBuilder cmd) {
		boolean success = false;
		try {
			Process subProcess = null;
			
			if(OSValidator.isUnix()) {
				//Process subProcess = Runtime.getRuntime().exec(exec.toString());
				subProcess = new ProcessBuilder().command("bash", "-c", cmd.toString()).start();
			}
			else if(OSValidator.isWindows()) {
				subProcess = Runtime.getRuntime().exec(cmd.toString());;
			}
			else {
				log.error("This operating system " + OSValidator.getOS() + "is not supported. The command " + cmd + " cannot be executed");
				return false;
			}
			
			subProcess.waitFor();
			
			String s;
            BufferedReader br = new BufferedReader(new InputStreamReader(subProcess.getInputStream()));
            while ((s = br.readLine()) != null)
                log.info(s);
            
            success = subProcess.exitValue() == 0;
            
            subProcess.destroy();
			
			
		} catch (IOException | InterruptedException e) {
			log.error("Error when execution the command " + cmd.toString(), e);
		}
		
		return success;
	}

}
