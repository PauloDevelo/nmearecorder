package arbutus.nmea.sentences;

//HDG Heading – Deviation & Variation
//$--HDG,x.x,x.x,a,x.x,a*hh
//       1   2   3 4   5 6
//1) Magnetic Sensor heading in degrees
//2) Magnetic Deviation, degrees
//3) Magnetic Deviation direction, E = Easterly, W = Westerly
//4) Magnetic Variation degrees
//5) Magnetic Variation direction, E = Easterly, W = Westerly
//6) Checksum
//
public class HCHDG extends NMEASentence {
	public static final String sticker = "$HCHDG";
	
	@nmea(pos=1)
	private float magHdgDeg = Float.NaN;
	
	private float magDevDeg = Float.NaN;
	private float magVarDeg = Float.NaN;
	
	public HCHDG(long nanotime, StringBuilder sentence) {
		super(nanotime);
		parseNMEASentence(sentence, this);
	}

	/**
	 * @return Magnetic Sensor heading in degrees
	 */
	public float getMagHdgDeg() {
		return magHdgDeg;
	}

	/**
	 * @return Magnetic Deviation degrees
	 */
	public float getMagDevDeg() {
		return magDevDeg;
	}

	/**
	 * @return Magnetic Variation degrees
	 */
	public float getMagVarDeg() {
		return magVarDeg;
	}
	
	@Override
	protected void initSpecialFields(String[] nmeaFields) {
		if(!nmeaFields[2].isEmpty())
			magDevDeg = Float.parseFloat(nmeaFields[2]);
		
		if (!nmeaFields[3].isEmpty() && nmeaFields[3].compareTo("W") == 0) {
			magDevDeg = -magDevDeg;
		}
		
		if(!nmeaFields[4].isEmpty())
			magVarDeg = Float.parseFloat(nmeaFields[4]);
		
		if (!nmeaFields[5].isEmpty() && nmeaFields[5].compareTo("W") == 0) {
			magVarDeg = -magVarDeg;
		}
	}
	
}
