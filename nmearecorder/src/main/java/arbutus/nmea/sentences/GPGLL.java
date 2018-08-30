package arbutus.nmea.sentences;

//GLL Geographic Position – Latitude/Longitude
//$--GLL,llll.ll,a,yyyyy.yy,a,hhmmss.ss,A*hh
//       1       2 3        4 5         6 7
//1) Latitude
//2) N or S (North or South)
//3) Longitude
//4) E or W (East or West)
//5) Time (UTC)
//6) Status A - Data Valid, V - Data Invalid
//7) Checksum
//
public class GPGLL extends NMEASentence {
	public static final String sticker = "$GPGLL";
	
	@nmea(pos=5)
	private String timeUTC = null;
	
	@nmea(pos=1)
	private Position pos = null;
	
	@nmea(pos=6)
	private Status status;
	
	public GPGLL(StringBuilder sentence) {
		parseNMEASentence(sentence, this);
	}

	/**
	 * @return the time UTC
	 */
	public String getTimeUTC() {
		return timeUTC;
	}

	/**
	 * @return latitude
	 */
	public float getLatitude() {
		return pos.getLatitude();
	}

	/**
	 * @return longitude
	 */
	public float getLongitude() {
		return pos.getLongitude();
	}

	/**
	 * @return status
	 */
	public Status getStatus() {
		return status;
	}
}
