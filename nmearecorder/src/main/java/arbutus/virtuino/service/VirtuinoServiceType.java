package arbutus.virtuino.service;

import java.util.HashMap;
import java.util.Map;

public enum VirtuinoServiceType {
	Engine("engine"),
	PIR("pir");
	
	private final String idConnector;
	private static Map<String, VirtuinoServiceType> map = new HashMap<>();

	VirtuinoServiceType(String idConnector) {
        this.idConnector = idConnector;
    }
	
	static {
        for (VirtuinoServiceType connectorId : VirtuinoServiceType.values()) {
            map.put(connectorId.idConnector, connectorId);
        }
    }

    public String getVal() {
        return this.idConnector;
    }
}
