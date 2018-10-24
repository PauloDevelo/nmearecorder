package arbutus.virtuino.service;

import java.util.function.BiConsumer;

public interface IEngineService {
	
	/**
	 * Gets the value of an engine measurement synchronously
	 * @param measurement The measurement we want to get
	 * @return The value of the measurement
	 */
	float getValue(EngineMeasurement measurement);
	
	/**
	 * Allow to be notified regularly of a measurement asynchronously
	 * @param measurement The measurement we want to subscribe
	 * @param consumer The consumer for the notification
	 */
	void subscribe(EngineMeasurement measurement, BiConsumer<Long, Float> consumer);
	
	/**
	 * Unsubscribe a subscription
	 * @param measurement For this measurement
	 * @param consumer Make sure the the instance of the consumer is the same that the one use for the subscription. Using this::consumerFunction will create a new instance of BiConsumer. So it is required to store the Biconsumer instance in order to perform the unsubscription ... See unit test about unsubscribe.
	 */
	void unsubscribe(EngineMeasurement measurement, BiConsumer<Long, Float> consumer);

}
