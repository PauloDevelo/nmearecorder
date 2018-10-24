package arbutus.virtuino.connectors.serial;

import java.util.HashMap;
import java.util.Map;

import jssc.SerialPort;

public enum SerialDataBits {
	DATABITS_5(SerialPort.DATABITS_5),
	DATABITS_6(SerialPort.DATABITS_6),
	DATABITS_7(SerialPort.DATABITS_7),
	DATABITS_8(SerialPort.DATABITS_8);
	
	private static Map<Integer, SerialDataBits> map = new HashMap<>();
	
	private final int dataBits;

	SerialDataBits(int dataBits) {
        this.dataBits = dataBits;
    }
	
	static {
        for (SerialDataBits dataBits : SerialDataBits.values()) {
            map.put(dataBits.dataBits, dataBits);
        }
    }
	
	public static SerialDataBits valueOf(int dataBits) {
        return map.get(dataBits);
    }

    public int getVal() {
        return this.dataBits;
    }

}
