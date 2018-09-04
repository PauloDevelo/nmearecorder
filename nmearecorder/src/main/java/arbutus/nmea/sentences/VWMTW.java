package arbutus.nmea.sentences;

//MTW Water Temperature
//$--MTW,x.x,C*hh
//       1   2 3
//1) Degrees
//2) Unit of Measurement, Celcius
//3) Checksum
//$VWMTW,21.6,C*17
public class VWMTW extends NMEASentence {
	public static final String sticker = "$VWMTW";
	
	@nmea(pos=1)
	private float waterTempCelcius = Float.NaN;
	
	public VWMTW(long nanotime, StringBuilder sentence) {
		super(nanotime);
		parseNMEASentence(sentence, this);
	}

	/**
	 * @return the waterTempCelcius
	 */
	public float getWaterTempCelcius() {
		return waterTempCelcius;
	}
}
