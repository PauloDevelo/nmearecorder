package arbutus.nmea.sentences;

//$PGRMM Map Datum
//Garmin proprietary sentence

//$PGRMM,c---c*hh
//		 1     2
//1) Currently active horizontal datum (WGS-84, NAD27 Canada, ED50, a.s.o)
//2) Checksum
public class PGRMM extends NMEASentence {
	public static final String sticker = "$PGRMM";
	
	@nmea(pos=1)
	private String mapDatum = null;
	
	public PGRMM(StringBuilder sentence) {
		parseNMEASentence(sentence, this);
	}

	/**
	 * @return the mapDatum
	 */
	public String getMapDatum() {
		return mapDatum;
	}	
}
