package arbutus.virtuino.connectors.serial;

import java.util.HashMap;
import java.util.Map;

import jssc.SerialPort;

public enum SerialStopBits {
	STOPBITS_1(SerialPort.STOPBITS_1),
	STOPBITS_1_5(SerialPort.STOPBITS_1_5),
	STOPBITS_2(SerialPort.STOPBITS_2);
	
	private static Map<Integer, SerialStopBits> map = new HashMap<>();
	
	private final int stopBits;

	SerialStopBits(int stopBits) {
        this.stopBits = stopBits;
    }
	
	static {
        for (SerialStopBits stopBits : SerialStopBits.values()) {
            map.put(stopBits.stopBits, stopBits);
        }
    }
	
	public static SerialStopBits valueOf(int stopBits) {
        return map.get(stopBits);
    }

    public int getVal() {
        return this.stopBits;
    }

}
