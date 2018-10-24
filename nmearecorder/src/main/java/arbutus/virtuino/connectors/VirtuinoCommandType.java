package arbutus.virtuino.connectors;

import java.util.HashMap;
import java.util.Map;

//commandType == 'I' Digital read
//commandType == 'Q' Digital read & write
//commandType == 'D' Virtual digital memory
//commandType == 'A' Analogic read
//commandType == 'O' Measure the length of a pulse or write on an analogic pin
//commandType == 'V' Virtual float memory
//commandType == 'C' bt_firmware (see firmware code for more info)

public enum VirtuinoCommandType {
	DigitalRead('I'),
	DigitalReadWrite('Q'),
	VirtualDigitalMem('D'),
	AnalogicRead('A'),
	AnalogicWrite('O'),
	VirtualFloat('V'),
	FirmwareCode('C');
	
	private final char val;
	private static Map<Character, VirtuinoCommandType> map = new HashMap<>();

	VirtuinoCommandType(char val) {
        this.val = val;
    }
	
	static {
        for (VirtuinoCommandType command : VirtuinoCommandType.values()) {
            map.put(command.val, command);
        }
    }
	
	public static VirtuinoCommandType valueOf(char commandVal) {
        return map.get(commandVal);
    }

    public char getVal() {
        return val;
    }
}
