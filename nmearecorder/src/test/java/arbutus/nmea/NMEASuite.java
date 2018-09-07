package arbutus.nmea;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import arbutus.nmea.sentences.NMEASentencesSuite;
import arbutus.nmea.service.NMEAServiceSuite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	NMEASentencesSuite.class,
	NMEAServiceSuite.class
})
public class NMEASuite {

}
