package arbutus.nmea.service;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import arbutus.nmea.sentences.NMEASentencesSuite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	NMEASentencesSuite.class,
	NMEAServiceTest.class
})
public class NMEAServiceSuite {

}
