package arbutus.nmea.sentences;

//BOD Bearing – Waypoint to Waypoint
//$--BOD,x.x,T,x.x,M,c--c,c--c*hh
//       1   2 3   4 5  6      7
//1) Bearing Degrees, TRUE
//2) T = True
//3) Bearing Degrees, Magnetic
//4) M = Magnetic
//5) TO Waypoint
//6) FROM Waypoint
//7) Checksum
public class GPBOD extends NMEASentence {
	public static final String sticker = "$GPBOD";
	
	@nmea(pos=1)
	private float bearingDegT = Float.NaN;
	
	@nmea(pos=5)
	private String wptTo = null;
	
	@nmea(pos=6)
	private String wptFrom = null;
	
	public GPBOD(StringBuilder nmeaSentence) {
		parseNMEASentence(nmeaSentence, this);
	}

	/**
	 * @return Bearing Degrees, TRUE
	 */
	public float getBearingDegT() {
		return bearingDegT;
	}

	/**
	 * @return TO Waypoint
	 */
	public String getWptTo() {
		return wptTo;
	}

	/**
	 * @return FROM Waypoint
	 */
	public String getWptFrom() {
		return wptFrom;
	}

}
