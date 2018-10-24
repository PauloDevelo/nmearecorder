package arbutus.virtuino.connectors.serial;

import java.util.HashMap;
import java.util.Map;

import jssc.SerialPort;

public enum SerialBaud {
	BAUDRATE_110(SerialPort.BAUDRATE_110),
	BAUDRATE_115200(SerialPort.BAUDRATE_115200),
	BAUDRATE_1200(SerialPort.BAUDRATE_1200),
	BAUDRATE_128000(SerialPort.BAUDRATE_128000),
	BAUDRATE_14400(SerialPort.BAUDRATE_14400),
	BAUDRATE_19200(SerialPort.BAUDRATE_19200),
	BAUDRATE_256000(SerialPort.BAUDRATE_256000),
	BAUDRATE_300(SerialPort.BAUDRATE_300),
	BAUDRATE_38400(SerialPort.BAUDRATE_38400),
	BAUDRATE_4800(SerialPort.BAUDRATE_4800),
	BAUDRATE_57600(SerialPort.BAUDRATE_57600),
	BAUDRATE_600(SerialPort.BAUDRATE_600),
	BAUDRATE_9600(SerialPort.BAUDRATE_9600);
	
	private final int baud;
	private static Map<Integer, SerialBaud> map = new HashMap<>();

	SerialBaud(int baud) {
        this.baud = baud;
    }
	
	static {
        for (SerialBaud baud : SerialBaud.values()) {
            map.put(baud.baud, baud);
        }
    }
	
	public static SerialBaud valueOf(int baud) {
        return map.get(baud);
    }

    public int getVal() {
        return this.baud;
    }
}
