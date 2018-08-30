package arbutus.nmea.sentences;

import java.util.Date;

//BWC Bearing and Distance to Waypoint – Latitude, N/S, Longitude, E/W, UTC, Status
//$--BWC,hhmmss.ss,llll.ll,a,yyyyy.yy,a,x.x,T,x.x,M,x.x,N,c--c*hh
//       1         2       3 4        5 6   7 8   9 10  1112   13
/*
 1) Time (UTC)
 2) Waypoint Latitude
 3) N = North, S = South
 4) Waypoint Longitude
 5) E = East, W = West
 6) Bearing, True
 7) T = True
 8) Bearing, Magnetic
 9) M = Magnetic
10) Nautical Miles
11) N = Nautical Miles
12) Waypoint ID
13) Checksum
 */
public class GPBWC extends NMEASentence{
	public static final String sticker = "$GPBWC";
	
	private Date date = null;
	
	@nmea(pos=2)
	private Position wptPos = null;
	
	@nmea(pos=6)
	private float bearingT = Float.NaN;
	
	@nmea(pos=11)
	private float distanceNm = Float.NaN;
	
	@nmea(pos=12)
	private String wptId = null;
	
	public GPBWC(StringBuilder sentence) {
		parseNMEASentence(sentence, this);
	}

	/**
	 * @return Time (UTC)
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @return Waypoint Latitude
	 */
	public float getWptLatitude() {
		return wptPos.getLatitude();
	}

	/**
	 * @return Waypoint Longitude
	 */
	public float getWptLongitude() {
		return wptPos.getLongitude();
	}

	/**
	 * @return Bearing, True
	 */
	public float getBearingT() {
		return bearingT;
	}

	/**
	 * @return Nautical Miles
	 */
	public float getDistanceNm() {
		return distanceNm;
	}

	/**
	 * @return Waypoint ID
	 */
	public String getWptId() {
		return wptId;
	}
}
