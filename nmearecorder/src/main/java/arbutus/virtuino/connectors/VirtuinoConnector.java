package arbutus.virtuino.connectors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.apache.log4j.Logger;

public abstract class VirtuinoConnector implements Runnable {
	private final static Logger log = Logger.getLogger(VirtuinoConnector.class);
	
	/**
	 * Thread of the connector. The thread is managed by the connector with the two public methods startProcess and stopProcess.
	 * The thread is responsible to notify the subscribers when new values are read approximately every VirtuinoContext.scanRateInMilliSec
	 */
	private Thread threadConnector = null;
	
	private final VirtuinoContext context;
	
	/**
	 * This flag need to be synchronysed because it is read and write from different threads.
	 */
	private boolean isInterrupted = false;
	
	private boolean loggingAlertOverSize = true;
	
	/**
	 * Contains the subscribers. This map is protected by synchronized section because it is potentially possible to add subscriber after the thread is started.
	 */
	private final HashMap<VirtuinoItem, List<BiConsumer<Long, Float>>> consumers = new HashMap<>();
	
	/**
	 * The executor responsible to call the subscribers.
	 */
	private ExecutorService executor = null;
	
	/**
	 * List of the CompletableFuture waiting for to be processed by the executor.
	 */
	private final List<CompletableFuture<Void>> cfs = new ArrayList<>();

	/**
	 * Constructor for a VirtuinoConstructor
	 * @param context The Virtuino context contains only the scan rate in milliseconds.
	 */
	public VirtuinoConnector(VirtuinoContext context) {
		this.context = context;
	}
	
	/**
	 * This synchronous function allows to subscribe a consumer when a new value is available
	 * @param item The Virtuino item
	 * @param consumer The consumer
	 */
	public synchronized void subscribe(VirtuinoItem item, BiConsumer<Long, Float> consumer) {
		List<BiConsumer<Long, Float>> existingList = this.consumers.get(item);
		
		if(existingList == null) {
			existingList = new ArrayList<>();
			existingList.add(consumer);
			this.consumers.put(item, existingList);
		}
		else {
			if(existingList.contains(consumer)) {
				log.error("The Virtuino connector " + this.getConnectorKey() + ": Attempt to subscribe the same consumer several times.");
			}
			else {
				existingList.add(consumer);
			}
		}
	}
	
	/**
	 * This synchronous function allows to unsubscribe a consumer from a Virtuino Item new value
	 * @param item The item the subscriber was subscribed on
	 * @param consumer The function to call when a new value is available
	 */
	public synchronized void unsubscribe(VirtuinoItem item, BiConsumer<Long, Float> consumer) {		
		List<BiConsumer<Long, Float>> existingList = this.consumers.get(item);
		
		if(existingList == null) {
			log.error("The Virtuino connector " + this.getConnectorKey() + ": Impossible to unsubscribe a consumer which has not been subscribed");
		}
		else {
			if(existingList.remove(consumer)) {
				if(existingList.isEmpty()) {
					this.consumers.remove(item);
				}
			}
			else {
				log.error("The Virtuino connector " + this.getConnectorKey() + ": Impossible to unsubscribe a consumer which has not been subscribed");
			}
		}
	}
	
	/**
 	 * This synchronous function returns the value read on a digital pin
 	 * @param pinNumber The digital pin number to read from
 	 * @return The value of the Virtual float.
 	 * @throws VirtuinoConnectorException Throws a VirtuinoException in case the connector was not ready and connected, or if an error occured when reading the Virtual float.
 	 */
 	public synchronized float getSyncVirtualFloat(int pinNumber) throws VirtuinoConnectorException {
 		if(this.isConnectorReady()) {
 			return this.getFloat(VirtuinoCommandType.VirtualFloat, pinNumber);
		}
		else {
			throw new VirtuinoConnectorException("The connector is not ready yet.");
		}
	}
 	
 	/**
 	 * Ths synchronous function returns the value read on a digital pin
 	 * @param pinNumber The digital pin number to read from
 	 * @return True if the digital pin is high, false if low.
 	 * @throws VirtuinoConnectorException Throws a VirtuinoException in case the connector was not ready and connected or if an error occured when reading the digital pin.
 	 */
 	public synchronized boolean readSyncDigitalPin(int pinNumber) throws VirtuinoConnectorException{
 		if(this.isConnectorReady()) {
 			float value = this.getFloat(VirtuinoCommandType.DigitalRead, pinNumber);
 	 		
 	 		return value > 0;
		}
		else {
			throw new VirtuinoConnectorException("The connector is not ready yet.");
		}
 	}
	
 	/**
 	 * This synchronous function returns the firmware code of the Virtuino library embedded in the microcontroler.
 	 * @return The firmware code.
 	 * @throws VirtuinoConnectorException Throws an VirtuinoConnectorException if the connector was not ready and connected.
 	 */
	public synchronized float getSyncFirmwareCode() throws VirtuinoConnectorException {
		if(this.isConnectorReady()) {
			return this.writeFloat(VirtuinoCommandType.FirmwareCode, 0, 1);
		}
		else {
			throw new VirtuinoConnectorException("The connector is not ready yet.");
		}
		
	}
	
	/**
	 * This synchronous function indicates if the connector is ready (Connected to the external resource and ready to get values from the Virtuino firmware). It is used currently for UTs only.
	 * @return True if the connector is connected and ready, false otherwise.
	 */
	public synchronized boolean isReady() {
		return this.isConnectorReady();
	}
	
	/**
	 * This asynchronous function start the internal thread of the connector.
	 */
	public void startProcess() {
		this.threadConnector = new Thread(this);
		this.threadConnector.start();
	}
	
	/**
	 * This asynchronous function stop the internal thread of the connector.
	 */
	public void stopProcess() {
		if (this.threadConnector != null && this.threadConnector.isAlive()) {
			synchronized(this) {
				this.isInterrupted = true;
			}
			
			try {
				int nbMilli = 0;
				while(nbMilli < this.context.getScanRateInMilliSec() && this.threadConnector.isAlive()) {
					TimeUnit.MILLISECONDS.sleep(100);
					nbMilli += 100;
				}
				
				if(this.threadConnector.isAlive()) {
					this.threadConnector.interrupt();
					TimeUnit.MILLISECONDS.sleep(100);
				}
			} catch (InterruptedException e) {
				this.threadConnector.interrupt();
			}
			
			if(!this.threadConnector.isAlive()) {
				this.threadConnector = null;
			}
			else {
				log.error("The Virtuino connector " + this.getConnectorKey() + " could not stopped");
			}
		}
	}
	
	/**
	 * This function indicates if the internal thread of the connector is still alive. It is currently used for some UTs
	 * @return true if the thread is alive, false otherwise.
	 */
	public boolean isProcessAlive() {
		return this.threadConnector != null && this.threadConnector.isAlive();
	}
	
	@Override
	public final void run() {
		this.executor = Executors.newFixedThreadPool(2);
		
		synchronized(this) {
			this.isInterrupted = false;
		}
		
		while(!isInterrupted()) {
			synchronized (this) {
				checkAndReconnect();
				manageSubscriptions();
			}
			
			try {
				Thread.sleep(this.context.getScanRateInMilliSec());
			} catch (InterruptedException e) {
				synchronized(this) {
					this.isInterrupted = true;
				}
			}
		}
		
		synchronized (this) {
			this.stop();
		}
		
		this.executor.shutdown();
		try {
			this.executor.awaitTermination(3, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			this.executor.shutdownNow();
		}
	}
	
	/**
	 * Function to check the connection with the microcontroler. Attempt to reconnect if it is not already connected.
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
	
	protected final float getFloat(VirtuinoCommandType type, int pinNumber) throws VirtuinoConnectorException {
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
	
	protected final float writeFloat(VirtuinoCommandType type, int pinNumber, float value) throws VirtuinoConnectorException {
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

	private void manageSubscriptions() {
		if(this.cfs.size() < 40) {
			if(this.loggingAlertOverSize == false) {
				log.warn("The virtuino connector " + this.getConnectorKey() + " recovered:" +  + cfs.size());
			}
			this.loggingAlertOverSize  = true;
			
			for(VirtuinoItem item : this.consumers.keySet()) {
				float value = Float.NaN;
				if(this.isConnectorReady()) {
					try {
						value = this.getFloat(item.command, item.pin);
					}
					catch (VirtuinoConnectorException e) {
						log.error("The virtuino connector " + this.getConnectorKey() + ": An error occured when reading the item " + item.toString(), e);
					}
				}
				
				final long nanoTime = System.nanoTime();
					
				for(BiConsumer<Long, Float> consumer : this.consumers.get(item)) {
					final float biConsumerValue = value;
					this.cfs.add(CompletableFuture.runAsync(() -> consumer.accept(nanoTime, biConsumerValue), executor));
				}
			}
		}
		else {
			if(this.loggingAlertOverSize) {
				log.warn("The Virtuino connector " + this.getConnectorKey() + " contains more than 40 CompletableFuture in progress: " + cfs.size());
				this.loggingAlertOverSize = false;
			}
		}
		
		
		this.cfs.removeIf(cf -> cf.isDone());
	}

	private String getConnectorKey() {
		return this.context.connectorKey;
	}

	private synchronized boolean isInterrupted() {
		return this.isInterrupted ;
	}
}
