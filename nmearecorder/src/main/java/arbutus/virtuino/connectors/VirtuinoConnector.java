package arbutus.virtuino.connectors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import org.apache.log4j.Logger;

public abstract class VirtuinoConnector implements Runnable {
	private final static Logger log = Logger.getLogger(VirtuinoConnector.class);
	
	private final VirtuinoContext context;
	
	private boolean isInterrupted = false;
	private boolean loggingAlertOverSize = true;
	
	private final HashMap<VirtuinoItem, List<BiConsumer<Long, Float>>> consumers = new HashMap<>();
	
	private final ExecutorService executor = Executors.newFixedThreadPool(15);
	private final List<CompletableFuture<Void>> cfs = new ArrayList<>();
	
	public VirtuinoConnector(VirtuinoContext context) {
		this.context = context;
	}

//	#define AGE_ENGINE_INDEX	0
//	#define RPM_INDEX			1
//	#define CONSO_INDEX			2
//	#define QTE_GAZ_INDEX		3
//	#define TEMP_INDEX			4
//	#define VOLTAGE_INDEX		5
//	#define TEMP_COOLANT_INDEX	6
	
	public synchronized void subscribe(VirtuinoItem item, BiConsumer<Long, Float> consumer) {
		List<BiConsumer<Long, Float>> existingList = this.consumers.get(item);
		
		if(existingList == null) {
			existingList = new ArrayList<>();
			existingList.add(consumer);
			this.consumers.put(item, existingList);
		}
		else {
			if(existingList.contains(consumer)) {
				log.error("Attempt to subscribe the same consumer for the same Virtuino item several times");
			}
			else {
				existingList.add(consumer);
			}
		}
	}
	
	public synchronized void unsubscribe(VirtuinoItem item, BiConsumer<Long, Float> consumer) {		
		List<BiConsumer<Long, Float>> existingList = this.consumers.get(item);
		
		if(existingList == null) {
			log.error("Impossible to unsubscribe a consumer which has not been subscribed");
		}
		else {
			if(existingList.remove(consumer)) {
				if(existingList.isEmpty()) {
					this.consumers.remove(item);
				}
			}
			else {
				log.error("Impossible to unsubscribe a consumer which has not been subscribed");
			}
		}
	}
	
 	public synchronized float getSyncVirtualFloat(int pinNumber) throws VirtuinoConnectorException {
		return this.getFloat(VirtuinoCommandType.VirtualFloat, pinNumber);
	}
	
	public synchronized float getSyncFirmwareCode() throws VirtuinoConnectorException {
		return this.writeFloat(VirtuinoCommandType.FirmwareCode, 0, 1);
	}
	
	public synchronized boolean isReady() {
		return this.isConnectorReady();
	}
	
	public synchronized void interrupt() {
		this.isInterrupted = true;
	}
	
	public synchronized boolean isInterrupted() {
		return this.isInterrupted ;
	}
	
	@Override
	public final void run() {		
		while(!isInterrupted()) {
			synchronized (this) {
				checkAndReconnect();
				manageSubscriptions();
			}
			
			try {
				Thread.sleep(this.context.scanRateInMilliSec);
			} catch (InterruptedException e) {
				this.interrupt();
			}
		}
		
		synchronized (this) {
			this.stop();
		}
	}
	
	private void manageSubscriptions() {
		for(VirtuinoItem item : this.consumers.keySet()) {
			try {
				float value = this.getFloat(item.command, item.pin);
				long nanoTime = System.nanoTime();
				
				for(BiConsumer<Long, Float> consumer : this.consumers.get(item)) {
					if(this.cfs.size() < 40) {
						this.loggingAlertOverSize  = true;
						this.cfs.add(CompletableFuture.runAsync(() -> consumer.accept(nanoTime, value), executor));
					}
					else {
						if(this.loggingAlertOverSize) {
							log.warn("More than 40 CompletableFuture in progress: " + cfs.size());
							this.loggingAlertOverSize = false;
						}
					}
				}
			}
			catch (VirtuinoConnectorException e) {
				log.error(e.getMessage());
			}
		}
		
		this.cfs.removeIf(cf -> cf.isDone());
	}

	/**
	 * Function to check the connection with the micro controler. Attempt to reconnect if it is not already connected.
	 */
	protected abstract void checkAndReconnect();
	
	/**
	 * Function to stop and free the connector's resources
	 */
	protected abstract void stop();
	
	/**
	 * Function to know the state of the connector
	 * @return
	 */
	protected abstract boolean isConnectorReady();
	
	/**
	 * Function allowing to write the command on the micro conntroler embedding the virtuino firmware
	 * @param command The command to write
	 * @return true in case of success, false otherwise.
	 * @throws VirtuinoConnectorException explaining why it failed
	 */
	protected abstract boolean writeString(String command) throws VirtuinoConnectorException;
	
	/**
	 * Function allowing to read a char in order to build the answer returned by the microcontroler embedding a Virtuino firmware
	 * @return the char read
	 * @throws VirtuinoConnectorException in case of failure explaining why the read did not work.
	 */
	protected abstract char readChar() throws VirtuinoConnectorException;
	
	private final String readAnswer() throws VirtuinoConnectorException{
		StringBuilder reponse = new StringBuilder("");
		
		char c = 0;
		
		boolean gotBeginning = false;
		boolean gotTerminaison = false;
		
		while (gotBeginning == false || gotTerminaison == false) {
			c = this.readChar();
			
			if(c == '!')
				gotBeginning = true;
			
			if(gotBeginning == true)
				reponse.append(c);
			
			if(c == '$')
				gotTerminaison = true;
		}	
		
		return reponse.toString();
	}
	
	protected final float getFloat(VirtuinoCommandType type, int pinNumber) throws VirtuinoConnectorException {
		if(this.isConnectorReady()) {
			String command = buildReadCommand(type, pinNumber);
			
			if(this.writeString(command)) {
				String reponse = this.readAnswer();

				if(!reponse.isEmpty()){
					int iBegin = reponse.indexOf(command.substring(0, 4));
					if(iBegin != -1){
						String valueStr = reponse.substring(iBegin + 5, reponse.length() - 1);
						try {
							return Float.parseFloat(valueStr);
						}
						catch(NumberFormatException ex) {
							throw new VirtuinoConnectorException("Number format exception occured for the float number [" + valueStr + "]", ex);
						}
					}
					else{
						throw new VirtuinoConnectorException("Unexpected answer for the command [" + command + "]: [" + reponse + "]");
					}
				}
				else {
					throw new VirtuinoConnectorException("We got no answer for the command [" + command + "]");
				}
			}
			else {
				throw new VirtuinoConnectorException("The attempt to write failed.");
			}	
		}
		else {
			throw new VirtuinoConnectorException("The connector is not ready yet.");
		}
	}
	
	protected final float writeFloat(VirtuinoCommandType type, int pinNumber, float value) throws VirtuinoConnectorException {
		if(this.isConnectorReady()) {
			String command = buildWriteCommand(type, pinNumber, value);
			
			if(this.writeString(command)) {
				String reponse = this.readAnswer();

				if(!reponse.isEmpty()){
					int iBegin = reponse.indexOf(command.substring(0, 4));
					if(iBegin != -1){
						String valueStr = reponse.substring(iBegin + 5, reponse.length() - 1);
						try {
							return Float.parseFloat(valueStr);
						}
						catch(NumberFormatException ex) {
							throw new VirtuinoConnectorException("Number format exception occured for the float number [" + valueStr + "]", ex);
						}
					}
					else{
						throw new VirtuinoConnectorException("Unexpected answer for the command [" + command + "]: [" + reponse + "]");
					}
				}
				else {
					throw new VirtuinoConnectorException("We got no answer for the command [" + command + "]");
				}
			}
			else {
				throw new VirtuinoConnectorException("The attempt to write failed.");
			}	
		}
		else {
			throw new VirtuinoConnectorException("The connector is not ready yet.");
		}
	}
	
	private final String buildWriteCommand(VirtuinoCommandType commandType, int pin, float value) {
		StringBuilder command = new StringBuilder(20);
		
		// Beginning of the command
		command.append("!");
		
		command.append(commandType.getVal());
		
		if(pin < 10)command.append('0');  //Pin is on 2 characters.
		command.append(pin);
		
		command.append(" ");
		
		command.append(value);
		
		// End of the command
		command.append("$");
		
		return command.toString();
	}
		
	private final String buildReadCommand(VirtuinoCommandType commandType, int pin) {
		// A command cannot be longer then 5
		StringBuilder command = new StringBuilder(10);
		
		// Beginning of the command
		command.append("!");
		
		command.append(commandType.getVal());
		
		if(pin < 10)command.append('0');  //Pin is on 2 characters.
		command.append(pin);
		
		command.append(" ?");// If we don't want to set the value, we have to write an interrogation mark
		
		// End of the command
		command.append(" $");
		
		return command.toString();
	}
}
