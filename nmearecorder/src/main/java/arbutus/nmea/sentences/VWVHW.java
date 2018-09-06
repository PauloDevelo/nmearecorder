package arbutus.nmea.sentences;

//VHW Water Speed and Heading

//$--VHW,x.x,T,x.x,M,x.x,N,x.x,K*hh
//		 1   2 3   4 5   6 7   8 9
//1) Degress True
//2) T = True
//3) Degrees Magnetic
//4) M = Magnetic
//5) Knots (speed of vessel relative to the water)
//6) N = Knots
//7) Kilometers (speed of vessel relative to the water)
//8) K = Kilometres
//9) Checksum

//$VWVHW,,,,,261,N,3.2,K*4D
//$VWVHW,,,,,0.00,N,0.00,K*4D
public class VWVHW extends NMEASentence {
	public static final String sticker = "$VWVHW";
	
	@nmea(pos=1)
	private float headingDegT = Float.NaN;
	@nmea(pos=5)
	private float stwKn = Float.NaN;
	
	public VWVHW(long nanotime, StringBuilder sentence) {
		super(nanotime);
		parseNMEASentence(sentence, this);
	}

	/**
	 * @return Degress True
	 */
	public float getHeadingDegT() {
		return headingDegT;
	}

	/**
	 * @return Knots (speed of vessel relative to the water)
	 */
	public float getStwKn() {
		return stwKn;
	}	
}
