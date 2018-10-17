package arbutus.service;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServiceManagerTest {
	private Service srv = null;
	private ServiceA srvA = null;
	private ServiceB srvB = null;
	int globalPosStart = 0;
	int globalPosStop = 0;
	
	@Before
	public void setUp() {
		globalPosStart = 0;
		globalPosStop = 0;
		srv = new Service();
		srvA = new ServiceA();
		srvB = new ServiceB();
	}
	
	@After
	public void tearDown() {
		ServiceManager svcMgr = ServiceManager.getInstance();
		
		svcMgr.unregister(InterfaceA.class);
		svcMgr.unregister(InterfaceB.class);
		svcMgr.unregister(IService.class);
	}
	
    @Test
    public void Registrations()
    {
        // Arrange
    	ServiceManager svcMgr = ServiceManager.getInstance();
    	ServiceC srvC = new ServiceC();
    	
    	// Act - Assert
    	assertTrue("Because serviceA implements IService and it was registered.", svcMgr.register(InterfaceA.class, srvA));
    	assertFalse("Because we cannot add twice the save serice interface.", svcMgr.register(InterfaceA.class, srvA));
    	assertTrue("Because serviceB implements IService and it was registered.", svcMgr.register(InterfaceB.class, srvB));
    	assertFalse("Because serviceC does not implement IService.", svcMgr.register(InterfaceA.class, srvC));
    }
    
    @Test
    public void GetServices()
    {
        // Arrange
    	ServiceManager svcMgr = ServiceManager.getInstance();
    	svcMgr.register(InterfaceA.class, srvA);
		svcMgr.register(InterfaceB.class, srvB);
    	
    	// Act
    	InterfaceA mySrvA = svcMgr.getService(InterfaceA.class);
    	InterfaceB mySrvB = svcMgr.getService(InterfaceB.class);
    	
    	//Assert
    	assertNotNull("Because serviceA was registered.", mySrvA);
    	assertNotNull("Because serviceB was registered.", mySrvB);
    	assertEquals(srvA, mySrvA);
    	assertEquals(srvB, mySrvB);
    }
    
    @Test
    public void StartServices() throws Exception
    {
        // Arrange
    	ServiceManager svcMgr = ServiceManager.getInstance();
    	svcMgr.register(InterfaceA.class, srvA);
		svcMgr.register(InterfaceB.class, srvB);
		
    	// Act
		svcMgr.startServices();
		
		//Assert
    	assertEquals("Because the serviceA started.", 1, srvA.nbStart);
    	assertEquals("Because the serviceA did not stop.", 0, srvB.nbStop);
    	assertEquals("Because the serviceB started.", 1, srvB.nbStart);
    	assertEquals("Because the serviceB did not stop.", 0, srvB.nbStop);
    }
    
    @Test
    public void StartServices_StartsShouldBeOrdered() throws Exception
    {
        // Arrange
    	ServiceManager svcMgr = ServiceManager.getInstance();
    	svcMgr.register(InterfaceA.class, srvA);
		svcMgr.register(InterfaceB.class, srvB);
		
    	// Act
		svcMgr.startServices();
		
		//Assert
    	assertEquals("Because the serviceA started first.", 0, srvA.posStart);
    	assertEquals("Because the serviceB started second.", 1, srvB.nbStart);
    }
    
    @Test
    public void StopServices()
    {
        // Arrange
    	ServiceManager svcMgr = ServiceManager.getInstance();
    	svcMgr.register(InterfaceA.class, srvA);
		svcMgr.register(InterfaceB.class, srvB);
		
    	// Act
    	svcMgr.stopServices();
    	
    	//Assert
    	assertEquals("Because the serviceA did not start.", 0, srvA.nbStart);
    	assertEquals("Because the serviceA did stop.", 1, srvA.nbStop);
    	assertEquals("Because the serviceB did not start.", 0, srvA.nbStart);
    	assertEquals("Because the serviceB did stop.", 1, srvB.nbStop);
    }
    
    @Test
    public void StopServices_ShouldBeOrdered()
    {
        // Arrange
    	ServiceManager svcMgr = ServiceManager.getInstance();
    	svcMgr.register(InterfaceA.class, srvA);
		svcMgr.register(InterfaceB.class, srvB);
		
    	// Act
    	svcMgr.stopServices();
    	
    	//Assert
    	assertEquals("Because the serviceA did stop second.", 1, srvA.posStop);
    	assertEquals("Because the serviceB did stop first.", 0, srvB.posStop);
    }
    
    @Test
    public void StartServices_With_OneServiceFailing_All_Service_Should_Be_Stoped_BeforeThrowing_Exception() {
    	// Arrange
    	ServiceManager svcMgr = ServiceManager.getInstance();
    	
		svcMgr.register(InterfaceA.class, srvA);
		svcMgr.register(IService.class, srv);
		
    	// Act
    	try {
			svcMgr.startServices();
			fail("Because an exception should have been thrown in startServices");
		} catch (Exception e) {
			//Assert
	    	assertEquals("Because the serviceA started once.", 1, srvA.nbStart);
	    	assertEquals("Because the serviceA did stop because of the exception thrown by srv.", 1, srvA.nbStop);
		}
    }
    
    interface InterfaceA{}
    interface InterfaceB{}
    
    class ServiceA implements InterfaceA, IService{
    	
		@Override
		public ServiceState getState() {
			return null;
		}

		int posStart = -1;
		int nbStart = 0;
		@Override
		public void start() {
			this.nbStart++;
			this.posStart = globalPosStart++;
		}

		int posStop = -1;
		int nbStop = 0;
		@Override
		public void stop() {
			this.nbStop++;
			this.posStop = globalPosStop++;
		}
	};
	
	class ServiceB implements InterfaceB, IService{
		
		@Override
		public ServiceState getState() {
			// TODO Auto-generated method stub
			return null;
		}

		int posStart = -1;
		int nbStart = 0;
		@Override
		public void start() {
			this.nbStart++;
			this.posStart = globalPosStart++;
		}

		int posStop = -1;
		int nbStop = 0;
		@Override
		public void stop() {
			this.nbStop++;
			this.posStop = globalPosStop++;
		}
	}
	
	class ServiceC implements InterfaceA{
		
	}
	
	class Service implements IService{
		@Override
		public ServiceState getState() {
			return ServiceState.STOPPED;
		}

		@Override
		public void start() throws Exception{
			throw new Exception();
		}

		@Override
		public void stop() {
		}
	}
    
}
