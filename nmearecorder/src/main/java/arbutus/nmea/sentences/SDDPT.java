package arbutus.nmea.sentences;

//DPT

//$--DPT,x.x,x.x*hh
//       1   2   3
//1) Depth, meters
//2) Offset from transducer;
//positive means distance from transducer to water line,
//negative means distance from transducer to keel
//3) Checksum
//$SDDPT,8.4,0.8,*7F
public class SDDPT extends NMEASentence {
	public static final String sticker = "$SDDPT";
	
	@nmea(pos=1)
	private float depth = Float.NaN;
	@nmea(pos=2)
	private float offsetFromTransducer = Float.NaN;
	
	public SDDPT(long nanotime, StringBuilder sentence) {
		super(nanotime);
		parseNMEASentence(sentence, this);
	}

	/**
	 * @return Depth, meters
	 */
	public float getDepth() {
		return depth;
	}

	/**
	 * @return Offset from transducer
	 * positive means distance from transducer to water line,
	 * negative means distance from transducer to keel
	 */
	public float getOffsetFromTransducer() {
		return offsetFromTransducer;
	}
}
