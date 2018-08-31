package arbutus;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import arbutus.influxdb.InfluxdbRepositoryTests;
import arbutus.nmea.NMEASuite;
import arbutus.service.ServiceManagerTest;
import arbutus.timeservice.TimeServiceTests;
import arbutus.util.UtilSuite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	TimeServiceTests.class,
	ServiceManagerTest.class,
	NMEASuite.class,
	UtilSuite.class,
	InfluxdbRepositoryTests.class
})
public class BlackBoxSuite {

}
