package arbutus.service;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServiceManagerTest {
	private ServiceA srvA = null;
	private ServiceB srvB = null;
	
	@Before
	public void setUp() {
		srvA = new ServiceA();
		srvB = new ServiceB();
	}
	
	@After
	public void tearDown() {
		ServiceManager svcMgr = ServiceManager.getInstance();
		
		svcMgr.unregister(InterfaceA.class);
		svcMgr.unregister(InterfaceB.class);
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
    public void StartServices()
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
    	assertEquals("Because the serviceB started.", 1, srvA.nbStart);
    	assertEquals("Because the serviceB did not stop.", 0, srvB.nbStop);

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
    
    interface InterfaceA{}
    interface InterfaceB{}
    
    class ServiceA implements InterfaceA, IService{
    	
		@Override
		public ServiceState getState() {
			// TODO Auto-generated method stub
			return null;
		}

		int nbStart = 0;
		@Override
		public void start() {
			nbStart++;
		}

		int nbStop = 0;
		@Override
		public void stop() {
			nbStop++;
		}
	};
	
	class ServiceB implements InterfaceB, IService{
		
		@Override
		public ServiceState getState() {
			// TODO Auto-generated method stub
			return null;
		}

		int nbStart = 0;
		@Override
		public void start() {
			nbStart++;
		}

		int nbStop = 0;
		@Override
		public void stop() {
			nbStop++;
		}
	}
	
	class ServiceC implements InterfaceA{
		
	}
	
	class Service implements IService{
		@Override
		public ServiceState getState() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void start() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void stop() {
			// TODO Auto-generated method stub
			
		}
	}
    
}
