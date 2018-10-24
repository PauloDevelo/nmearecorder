package arbutus.virtuino.service;

import java.util.HashMap;
import java.util.Map;

//#define AGE_ENGINE_INDEX	0
//#define RPM_INDEX			1
//#define CONSO_INDEX			2
//#define QTE_GAZ_INDEX		3
//#define TEMP_INDEX			4
//#define VOLTAGE_INDEX		5
//#define TEMP_COOLANT_INDEX	6

public enum EngineMeasurement {
	AGE(0),
	RPM(1),
	CONSO(2),
	DIESEL_VOL(3),
	TEMP_EXHAUST(4),
	BAT_VOLTAGE(5),
	TEMP_COOLANT(6);
	
	private static Map<Integer, EngineMeasurement> map = new HashMap<>();
	
	private final int pin;

	EngineMeasurement(int pin) {
        this.pin = pin;
    }
	
	static {
        for (EngineMeasurement engineMeas : EngineMeasurement.values()) {
            map.put(engineMeas.pin, engineMeas);
        }
    }
	
	public static EngineMeasurement valueOf(int pin) {
        return map.get(pin);
    }

    public int getPin() {
        return this.pin;
    }
}
