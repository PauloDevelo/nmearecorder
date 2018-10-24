package arbutus.virtuino.connectors;

public class VirtuinoConnectorException extends Exception {
	private static final long serialVersionUID = 1264601200950488768L;

	public VirtuinoConnectorException(String message) {
		super(message);
	}

	public VirtuinoConnectorException(String message, Exception e) {
		super(message, e);
	}

}
