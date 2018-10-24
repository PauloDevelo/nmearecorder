package arbutus;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import arbutus.influxdb.InfluxDBSuite;
import arbutus.nmea.NMEASuite;
import arbutus.rtmodel.MeasurementTestSuite;
import arbutus.rtmodel.VesselTest;
import arbutus.service.ServiceManagerTest;
import arbutus.timeservice.TimeServiceTest;
import arbutus.util.UtilSuite;
import arbutus.virtuino.VirtuinoSuite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	TimeServiceTest.class,
	ServiceManagerTest.class,
	NMEASuite.class,
	UtilSuite.class,
	InfluxDBSuite.class,
	VesselTest.class,
	MeasurementTestSuite.class,
	VirtuinoSuite.class
})

public class BlackBoxSuite {
}
