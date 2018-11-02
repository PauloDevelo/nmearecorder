package arbutus.virtuino.service;

import java.util.function.BiConsumer;

import arbutus.virtuino.connectors.VirtuinoCommandType;
import arbutus.virtuino.connectors.VirtuinoConnector;

public interface IVirtuinoService {
	
	void addVirtuinoConnector(String connectorKey, VirtuinoConnector connector) throws VirtuinoServiceException;
	
	/**
	 * Gets the value of an engine measurement synchronously
	 * @param measurement The measurement we want to get
	 * @return The value of the measurement
	 */
	float getVirtualFloat(String connectorKey, int pin);
	
	/**
	 * Allow to be notified regularly of a measurement asynchronously
	 * @param measurement The measurement we want to subscribe
	 * @param consumer The consumer for the notification
	 */
	void subscribe(String connectorKey, VirtuinoCommandType type, int pin, BiConsumer<Long, Float> consumer) throws VirtuinoServiceException;
	
	/**
	 * Unsubscribe a subscription
	 * @param measurement For this measurement
	 * @param consumer Make sure the the instance of the consumer is the same that the one use for the subscription. Using this::consumerFunction will create a new instance of BiConsumer. So it is required to store the Biconsumer instance in order to perform the unsubscription ... See unit test about unsubscribe.
	 */
	void unsubscribe(String connectorKey, VirtuinoCommandType type, int pin, BiConsumer<Long, Float> consumer) throws VirtuinoServiceException;

}
