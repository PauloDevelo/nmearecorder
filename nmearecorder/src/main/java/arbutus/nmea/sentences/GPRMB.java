package arbutus.nmea.sentences;

//RMB Recommended Minimum Navigation Information
//$--RMB,A,x.x,a,c--c,c--c,llll.ll,a,yyyyy.yy,a,x.x,x.x,x.x,A*hh
//       1 2   3 4    5    6       7 8        9 10  11  12  1314
//1) Status, V = Navigation receiver warning
//2) Cross Track error - nautical miles
//3) Direction to Steer, Left = L or Right = R
//4) TO Waypoint ID
//5) FROM Waypoint ID
//6) Destination Waypoint Latitude
//7) N or S
//8) Destination Waypoint Longitude
//9) E or W
//10) Range to destination in nautical miles
//11) Bearing to destination in degrees True
//12) Destination closing velocity in knots
//13) Arrival Status, A = Arrival Circle Entered
//14) Checksum
//
public class GPRMB extends NMEASentence {
	public static final String sticker = "$GPRMB";
	
	@nmea(pos=1)
	private Status status;
	
	@nmea(pos=2)
	private float crossTrackErrorNm = Float.NaN;
	
	@nmea(pos=3)
	private String directionToSteer = null;
	
	@nmea(pos=4)
	private String toWptId = null;
	@nmea(pos=5)
	private String fromWptId = null;
	
	@nmea(pos=6)
	private Position dest = null;
	
	@nmea(pos=10)
	private float rangeToDestinationNm = Float.NaN;
	
	@nmea(pos=11)
	private float brgToDestinationDegT = Float.NaN;
	
	@nmea(pos=12)
	private Status arrivalStatus;
	
	public GPRMB(long nanotime, StringBuilder sentence) {
		super(nanotime);
		parseNMEASentence(sentence, this);
	}

	/**
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @return the crossTrackErrorNm
	 */
	public float getCrossTrackErrorNm() {
		return crossTrackErrorNm;
	}

	/**
	 * @return Direction to Steer, Left = L or Right = R
	 */
	public String getDirectionToSteer() {
		return directionToSteer;
	}

	/**
	 * @return the toWptId
	 */
	public String getToWptId() {
		return toWptId;
	}

	/**
	 * @return the fromWptId
	 */
	public String getFromWptId() {
		return fromWptId;
	}

	/**
	 * @return the destLatitude
	 */
	public float getDestLatitude() {
		return dest.getLatitude();
	}

	/**
	 * @return the destLongitude
	 */
	public float getDestLongitude() {
		return dest.getLongitude();
	}

	/**
	 * @return the rangeToDestinationNm
	 */
	public float getRangeToDestinationNm() {
		return rangeToDestinationNm;
	}

	/**
	 * @return the brgToDestinationDegT
	 */
	public float getBrgToDestinationDegT() {
		return brgToDestinationDegT;
	}

	/**
	 * @return the arrivalStatus
	 */
	public Status getArrivalStatus() {
		return arrivalStatus;
	}
}
