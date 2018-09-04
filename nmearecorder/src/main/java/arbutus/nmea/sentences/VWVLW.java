package arbutus.nmea.sentences;

//VLW Distance Traveled through Water
//$--VLW,x.x,N,x.x,N*hh
//       1   2 3   4 5
//1) Total cumulative distance
//2) N = Nautical Miles
//3) Distance since Reset
//4) N = Nautical Miles
//5) Checksum
//$VWVLW,1270.0,N,17.62,N,,,,*7A
public class VWVLW extends NMEASentence {
	public static final String sticker = "$VWVLW";
	
	@nmea(pos=1)
	private float totalCumulDistNm = Float.NaN;
	@nmea(pos=3)
	private float distSinceResetNm = Float.NaN;
	
	public VWVLW(long nanotime, StringBuilder sentence) {
		super(nanotime);
		parseNMEASentence(sentence, this);
	}

	/**
	 * @return the totalCumulDistNm
	 */
	public float getTotalCumulDistNm() {
		return totalCumulDistNm;
	}

	/**
	 * @return the distSinceResetNm
	 */
	public float getDistSinceResetNm() {
		return distSinceResetNm;
	}
}
