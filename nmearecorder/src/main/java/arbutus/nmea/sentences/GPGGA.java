package arbutus.nmea.sentences;


//GGA Global Positioning System Fix Data. Time, Position and fix related data
//for a GPS receiver
//$--GGA,hhmmss.ss,llll.ll,a,yyyyy.yy,a,x,xx,x.x,x.x,M,x.x,M,x.x,xxxx*hh
//       1         2       3 4        5 6 7  8   9   1011  1213  14   15
//1) Time (UTC)
//2) Latitude
//3) N or S (North or South)
//4) Longitude
//5) E or W (East or West)
//6) GPS Quality Indicator,
//0 - fix not available,
//1 - GPS fix,
//2 - Differential GPS fix
//7) Number of satellites in view, 00 - 12
//8) Horizontal Dilution of precision
//9) Antenna Altitude above/below mean-sea-level (geoid)
//10) Units of antenna altitude, meters
//11) Geoidal separation, the difference between the WGS-84 earth
//ellipsoid and mean-sea-level (geoid), "-" means mean-sea-level below ellipsoid
//12) Units of geoidal separation, meters
//13) Age of differential GPS data, time in seconds since last SC104
//type 1 or 9 update, null field when DGPS is not used
//14) Differential reference station ID, 0000-1023
//15) Checksum
//
public class GPGGA extends NMEASentence {
	public static final String sticker = "$GPGGA";
	
	@nmea(pos=1)
	private String time;
	
	@nmea(pos=2)
	private Position pos = null;
	
	@nmea(pos=6)
	private GPSQuality gpsQualityIndicator;
	
	@nmea(pos=7)
	private int numberSatInView;
	
	@nmea(pos=8)
	private float horizontalDilutionPrec = Float.NaN;
	
	@nmea(pos=9)
	private float antennaAltitudeInMeter = Float.NaN;
	
	@nmea(pos=11)
	private float geoidSeparationInMeter = Float.NaN;
	
	@nmea(pos=13)
	private float ageOfDiffGPSDataInSecond = Float.NaN;
	
	@nmea(pos=14)
	private String refStationId = null;
	
	public GPGGA(long nanotime, StringBuilder sentence) {
		super(nanotime);
		parseNMEASentence(sentence, this);
	}
	
	/**
	 * @return Time (UTC)
	 */
	public String getTime() {
		return time;
	}

	/**
	 * @return Latitude
	 */
	public float getLatitude() {
		return pos.getLatitude();
	}

	/**
	 * @return Longitude
	 */
	public float getLongitude() {
		return pos.getLongitude();
	}

	/**
	 * @return GPS Quality Indicator
	 */
	public GPSQuality getGpsQualityIndicator() {
		return gpsQualityIndicator;
	}

	/**
	 * @return Number of satellites in view, 00 - 12
	 */
	public int getNumberSatInView() {
		return numberSatInView;
	}

	/**
	 * @return Horizontal Dilution of precision
	 */
	public float getHorizontalDilutionPrec() {
		return horizontalDilutionPrec;
	}

	/**
	 * @return Antenna Altitude above/below mean-sea-level (geoid)
	 */
	public float getAntennaAltitudeInMeter() {
		return antennaAltitudeInMeter;
	}

	/**
	 * @return Geoidal separation, the difference between the WGS-84 earth
	 * ellipsoid and mean-sea-level (geoid), "-" means mean-sea-level below ellipsoid
	 */
	public float getGeoidSeparationInMeter() {
		return geoidSeparationInMeter;
	}

	/**
	 * @return Age of differential GPS data, time in seconds since last SC104
	 * type 1 or 9 update, null field when DGPS is not used
	 */
	public float getAgeOfDiffGPSDataInSecond() {
		return ageOfDiffGPSDataInSecond;
	}

	/**
	 * @return Differential reference station ID, 0000-1023
	 */
	public String getRefStationId() {
		return refStationId;
	}
}
