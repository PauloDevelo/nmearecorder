package arbutus.virtuino.connectors.serial;

import java.util.HashMap;
import java.util.Map;

import jssc.SerialPort;

public enum SerialParity {
	PARITY_EVEN(SerialPort.PARITY_EVEN),
	PARITY_MARK(SerialPort.PARITY_MARK),
	PARITY_NONE(SerialPort.PARITY_NONE),
	PARITY_ODD(SerialPort.PARITY_ODD),
	PARITY_SPACE(SerialPort.PARITY_SPACE);
	
	private static Map<Integer, SerialParity> map = new HashMap<>();
	
	private final int parity;

	SerialParity(int parity) {
        this.parity = parity;
    }
	
	static {
        for (SerialParity parity : SerialParity.values()) {
            map.put(parity.parity, parity);
        }
    }
	
	public static SerialParity valueOf(int parity) {
        return map.get(parity);
    }

    public int getVal() {
        return this.parity;
    }

}
