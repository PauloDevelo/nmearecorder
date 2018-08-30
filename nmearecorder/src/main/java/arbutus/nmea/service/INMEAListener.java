package arbutus.nmea.service;

import arbutus.nmea.sentences.NMEASentence;

public interface INMEAListener {
	void onNewNMEASentence(NMEASentence sentence);
}
