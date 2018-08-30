package arbutus.nmea.sentences;

import java.util.HashMap;
import java.util.Map;

//6) GPS Quality Indicator,
//0 - fix not available,
//1 - GPS fix,
//2 - Differential GPS fix
public enum GPSQuality {
	FixNotAvailable(0),
	GPSFix(1),
	DiffGPSFix(2);
	
	private int numVal;
	private static Map<Integer, GPSQuality> map = new HashMap<>();

	GPSQuality(int numVal) {
        this.numVal = numVal;
    }
	
	static {
        for (GPSQuality quality : GPSQuality.values()) {
            map.put(quality.numVal, quality);
        }
    }
	
	public static GPSQuality valueOf(int qualityVal) {
        return map.get(qualityVal);
    }

    public int getNumVal() {
        return numVal;
    }
}
