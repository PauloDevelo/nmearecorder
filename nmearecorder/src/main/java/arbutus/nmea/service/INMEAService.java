package arbutus.nmea.service;

import arbutus.nmea.sentences.NMEASentence;

public interface INMEAService{
	<T extends NMEASentence> void  subscribe(Class<T> key, INMEAListener listener);
	<T extends NMEASentence> void unsubscribe(Class<T> key, INMEAListener listener);
}
