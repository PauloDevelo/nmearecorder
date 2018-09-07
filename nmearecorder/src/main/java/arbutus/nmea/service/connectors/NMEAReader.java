package arbutus.nmea.service.connectors;

import java.util.function.BiConsumer;

public abstract class NMEAReader implements Runnable {
	private BiConsumer<Long, StringBuilder> consumer = null;
	
	private boolean isInterrupted = false;
	
	public NMEAReader(BiConsumer<Long, StringBuilder> consumer) {
		this.consumer = consumer;
	}
	
	protected BiConsumer<Long, StringBuilder> getConsumer(){
		return this.consumer;
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

}
