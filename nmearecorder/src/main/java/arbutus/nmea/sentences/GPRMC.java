package arbutus.nmea.sentences;

import java.text.ParseException;
import java.util.Date;

import org.apache.log4j.Logger;

//RMC Recommended Minimum Navigation Information
//$--RMC,hhmmss.ss,A,llll.ll,a,yyyyy.yy,a,x.x,x.x,xxxx,x.x,a*hh
//       1         2 3       4 5        6 7   8   9    10  1112
//1) Time (UTC)
//2) Status, V = Navigation receiver warning
//3) Latitude
//4) N or S
//5) Longitude
//6) E or W
//7) Speed over ground, knots
//8) Track made good, degrees true
//9) Date, ddmmyy
//10) Magnetic Variation, degrees
//11) E or W
//12) Checksum
//
//$GPRMC,193134.00,A,2219.93324,S,16649.39025,E,0.052,,230818,,,D*64
public class GPRMC extends NMEASentence {
	private static Logger log = Logger.getLogger(GPRMC.class);
	
	public static final String sticker = "$GPRMC";
	
	@nmea(pos=2)
	private Status status;
	
	private Date utcDateTime = null;

	@nmea(pos=3)
	private Position pos = null;
	
	@nmea(pos=7)
	private float sogKnot = Float.NaN;
	@nmea(pos=8)
	private float tmgDegT = Float.NaN;
	
	private float magVarDeg = Float.NaN;
	
	public GPRMC(StringBuilder nmeaSentence) {
		parseNMEASentence(nmeaSentence, this);
	}
	
	/**
	 * @return Status, V = Navigation receiver warning
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @return the utc Date and Time
	 */
	public Date getUtcDateTime() {
		return utcDateTime;
	}

	/**
	 * @return the latitude Degree Decimal
	 * If S then negative
	 */
	public float getLatitudeDegDec() {
		return pos.getLatitude();
	}

	/**
	 * @return the longitude Degree Decimal
	 * If W then negative
	 */
	public float getLongitudeDegDec() {
		return pos.getLongitude();
	}

	/**
	 * @return Speed over ground, knots
	 */
	public float getSogKnot() {
		return sogKnot;
	}

	/**
	 * @return Track made good, degrees true
	 */
	public float getTmgDegT() {
		return tmgDegT;
	}

	/**
	 * @return Magnetic Variation, degrees
	 * If W then negative
	 */
	public float getMagVarDeg() {
		return magVarDeg;
	}

	@Override
	protected void initSpecialFields(String[] nmeaFields) {
		if(!nmeaFields[11].isEmpty()) {
			if(nmeaFields[11].compareTo("W") == 0) {
				magVarDeg =  -Float.parseFloat(nmeaFields[10]);
			}
			else {
				magVarDeg =  Float.parseFloat(nmeaFields[10]);
			}
		}
		
		if(!nmeaFields[1].isEmpty() && !nmeaFields[9].isEmpty()) {
			try {
				utcDateTime = NMEASentence.parseUTCDate(nmeaFields[9], nmeaFields[1]);
			} catch (ParseException e) {
				log.error("Error when parsing the date and time of a GPRMC sentence.", e);
			}
		}
	}
}
