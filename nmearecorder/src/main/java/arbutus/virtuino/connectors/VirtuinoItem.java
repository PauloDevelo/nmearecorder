package arbutus.virtuino.connectors;

public final class VirtuinoItem{
	public final VirtuinoCommandType command;
	public final int pin;
	
	public VirtuinoItem(VirtuinoCommandType commandType, int pin) {
		this.command = commandType;
		this.pin = pin;
	}
	
	@Override
	public int hashCode() {
		//int hash = super.hashCode();
		int hash = this.command.getVal() * 100 +  this.pin;
		System.out.println(hash);
		return hash;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object arg0) {
		if(arg0 instanceof VirtuinoItem) {
			return this.hashCode() == arg0.hashCode();
		}
		
		return false;
	}	
	
	
}
