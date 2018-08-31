package arbutus.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

public class TCPReader implements Runnable {

	private static Logger log = Logger.getLogger(TCPReader.class);
	
	private boolean isInterrupted = false;

	private PropertiesFile properties = null;
	
	private Consumer<StringBuilder> consumer = null;
	
	public TCPReader(Consumer<StringBuilder> consumer) {
		this.consumer = consumer;
		
		String fileSep = System.getProperty("file.separator");
		String propertiesPath = System.getProperty("user.dir") + fileSep + "properties" + fileSep + "tcpnetwork.properties";
		
		log.debug(propertiesPath);
				 
		properties = PropertiesFile.getPropertiesVM(propertiesPath);
	}
	
	/**
	 * @return the isInterrupted
	 */
	public synchronized boolean isInterrupted() {
		return isInterrupted;
	}

	/**
	 * @param isInterrupted the isInterrupted to set
	 */
	public synchronized void setInterrupted(boolean isInterrupted) {
		this.isInterrupted = isInterrupted;
	}
	
	@Override
	public void run() {
		try (Socket socket = new Socket(properties.getValue("host"), properties.getValueInt("port", 10110));
			 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))){

			List<CompletableFuture<Void>> cfs = new ArrayList<>();
			while(!isInterrupted()) {
				StringBuilder msg = new StringBuilder(reader.readLine());
				cfs.add(CompletableFuture.completedFuture(msg).thenAcceptAsync(s -> this.consumer.accept(s)));
				
				cfs.removeIf(cf -> cf.isDone());
				
				if(cfs.size() > 5) {
					log.warn("More than 5 thread in the queue: " + cfs.size());
				}
			}
			
			log.info("Terminaison of TCPReader.");
			
			if(cfs.size() > 0) {
				for(CompletableFuture<Void> cf : cfs) {
					cf.cancel(true);
				}
			}
			
		} catch (IOException | SecurityException | IllegalArgumentException e) {
			log.error("Error when reading the tcp socket", e);
		}
	}
}
