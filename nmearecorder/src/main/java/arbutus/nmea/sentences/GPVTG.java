package arbutus.nmea.sentences;

//VTG Track Made Good and Ground Speed
//$--VTG,x.x,T,x.x,M,x.x,N,x.x,K*hh
//       1   2 3   4 5   6 7   8 9
//1) Track Degrees
//2) T = True
//3) Track Degrees
//4) M = Magnetic
//5) Speed Knots
//6) N = Knots
//7) Speed Kilometers Per Hour
//8) K = Kilometres Per Hour
//9) Checksum
public class GPVTG extends NMEASentence {
	public static final String sticker = "$GPVTG";
	
	@nmea(pos=1)
	private float trackDegT = Float.NaN;
	
	@nmea(pos=5)
	private float speedKn = Float.NaN;
	
	public GPVTG(StringBuilder sentence) {
		parseNMEASentence(sentence, this);
	}

	/**
	 * @return the trackDegT
	 */
	public float getTrackDegT() {
		return trackDegT;
	}

	/**
	 * @return the speedKn
	 */
	public float getSpeedKn() {
		return speedKn;
	}
}
