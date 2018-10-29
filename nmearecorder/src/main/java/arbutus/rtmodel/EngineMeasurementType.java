package arbutus.rtmodel;

import java.util.HashMap;
import java.util.Map;

//#define AGE_ENGINE_INDEX	0
//#define RPM_INDEX			1
//#define CONSO_INDEX			2
//#define QTE_GAZ_INDEX		3
//#define TEMP_INDEX			4
//#define VOLTAGE_INDEX		5
//#define TEMP_COOLANT_INDEX	6

public enum EngineMeasurementType {
	AGE(0),
	RPM(1),
	CONSO(2),
	DIESEL_VOL(3),
	TEMP_EXHAUST(4),
	BAT_VOLTAGE(5),
	TEMP_COOLANT(6);
	
	private static Map<Integer, EngineMeasurementType> map = new HashMap<>();
	
	private final int pin;

	EngineMeasurementType(int pin) {
        this.pin = pin;
    }
	
	static {
        for (EngineMeasurementType engineMeas : EngineMeasurementType.values()) {
            map.put(engineMeas.pin, engineMeas);
        }
    }
	
	public static EngineMeasurementType valueOf(int pin) {
        return map.get(pin);
    }

    public int getPin() {
        return this.pin;
    }
}
