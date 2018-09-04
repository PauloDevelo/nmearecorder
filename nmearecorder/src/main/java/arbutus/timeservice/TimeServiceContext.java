package arbutus.timeservice;

public class TimeServiceContext {
	
	final private int synced;
	final private int synchro;
	final private int resync;
	final private String execStr;
	
	TimeServiceContext(int synced, int synchro, int resync, String execStr){
		this.synced = synced;
		this.synchro = synchro;
		this.resync = resync;
		this.execStr = execStr;
	}

	/**
	 * @return the synced
	 */
	public int getSynced() {
		return synced;
	}

	/**
	 * @return the synchro
	 */
	public int getSynchro() {
		return synchro;
	}

	/**
	 * @return the resync
	 */
	public int getResync() {
		return resync;
	}

	/**
	 * @return the execStr
	 */
	public String getExecStr() {
		return execStr;
	}
	
	

}
