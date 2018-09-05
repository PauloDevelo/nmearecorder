package arbutus.nmea.service.connectors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.apache.log4j.Logger;

import arbutus.util.PropertiesFile;

public class TCPReader extends NMEAReader {

	private static Logger log = Logger.getLogger(TCPReader.class);

	private PropertiesFile properties = null;
	
	public TCPReader(BiConsumer<Long, StringBuilder> consumer) {
		super(consumer);
		
		String fileSep = System.getProperty("file.separator");
		String propertiesPath = System.getProperty("user.dir") + fileSep + "properties" + fileSep + "tcpnetwork.properties";
		
		log.debug(propertiesPath);
				 
		properties = PropertiesFile.getPropertiesVM(propertiesPath);
	}
	
	@Override
	public void run() {
		try (Socket socket = new Socket(properties.getValue("host"), properties.getValueInt("port", 10110));
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))){
			
			ExecutorService executor = Executors.newFixedThreadPool(15);

			List<CompletableFuture<Void>> cfs = new ArrayList<>();
			while(!isInterrupted()) {
				StringBuilder msg = new StringBuilder(reader.readLine());
				long nanoTime = System.nanoTime();
				
				if(cfs.size() < 40) {
					cfs.add(CompletableFuture.runAsync(() -> this.getConsumer().accept(nanoTime, msg), executor));
				}
				else {
					log.warn("More than 40 CompletableFuture in progress: " + cfs.size());
				}
				
				cfs.removeIf(cf -> cf.isDone());
			}
			
			log.info("Terminaison of TCPReader.");
			
			if(cfs.size() > 0) {
				for(CompletableFuture<Void> cf : cfs) {
					cf.cancel(true);
				}
			}
			
			if(executor != null) {
				executor.shutdown();
				try {
				    if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
				    	executor.shutdownNow();
				    } 
				} catch (InterruptedException e) {
					executor.shutdownNow();
				}
				executor = null;
			}
			
		} catch (IOException | SecurityException | IllegalArgumentException e) {
			log.error("Error when reading the tcp socket", e);
		}
	}
}
