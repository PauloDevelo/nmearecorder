package arbutus.nmea.sentences;

import java.util.HashMap;
import java.util.Map;

//Status A - Data Valid, V - Data Invalid
public enum Status {
	DataValid('A'),
	DataInvalid('V');
	
	private char numVal;
	private static Map<Character, Status> map = new HashMap<>();

	Status(char numVal) {
        this.numVal = numVal;
    }
	
	static {
        for (Status status : Status.values()) {
            map.put(status.numVal, status);
        }
    }
	
	public static Status valueOf(char statusVal) {
        return map.get(statusVal);
    }

    public int getNumVal() {
        return numVal;
    }
}
