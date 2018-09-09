package arbutus.nmea.service.connectors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import arbutus.test.ToolBox;

public class NMEAReaderStub extends NMEAReader {
	
	private static NMEAReaderStub currentInstance = null;
	public static NMEAReaderStub getCurrentInstance() {
		return currentInstance;
	}
	
	private List<String> gprmclist = new ArrayList<String>();
	private List<String> windlist = new ArrayList<String>();

	public NMEAReaderStub(BiConsumer<Long, StringBuilder> consumer) {
		super(consumer);
		
		gprmclist.add("$GPRMC,033308,A,1805.5162,S,17632.8227,E,6.9,240.8,040818,12.2,E,A*3A");
		gprmclist.add("$GPRMC,033309.00,A,1805.51439,S,17632.82166,E,6.126,250.74,040818,,,D*72");
		gprmclist.add("$GPRMC,033310,A,1805.5175,S,17632.8194,E,5.7,257.2,040818,12.2,E,A*3F");
		gprmclist.add("$GPRMC,033311.00,A,1805.51551,S,17632.81829,E,5.537,249.58,040818,,,D*74");
		gprmclist.add("$GPRMC,033312,A,1805.5184,S,17632.8160,E,7.2,264.5,040818,12.2,E,A*38");
		gprmclist.add("$GPRMC,033313.00,A,1805.51621,S,17632.81473,E,7.092,263.07,040818,,,D*7B");
		gprmclist.add("$GPRMC,033314.00,A,1805.51677,S,17632.81288,E,6.412,244.05,040818,,,D*77");
		gprmclist.add("$GPRMC,033315,A,1805.5203,S,17632.8106,E,6.2,239.5,040818,12.2,E,A*3A");
		gprmclist.add("$GPRMC,033316.00,A,1805.51864,S,17632.80965,E,6.267,245.39,040818,,,D*7A");
		gprmclist.add("$GPRMC,033317.00,A,1805.51925,S,17632.80803,E,5.551,246.69,040818,,,D*79");
		
		windlist.add("$WIMWV,305,R,16.5,N,A*09");
		windlist.add("$WIMWV,306,R,16.8,N,A*07");
		windlist.add("$WIMWV,308,R,17.2,N,A*02");
		windlist.add("$WIMWV,307,R,17.5,N,A*0A");
		windlist.add("$WIMWV,312,R,18.0,N,A*04");
		windlist.add("$WIMWV,311,R,16.5,N,A*0C");
		windlist.add("$WIMWV,302,R,16.4,N,A*0F");
		windlist.add("$WIMWV,307,R,17.0,N,A*0F");
		windlist.add("$WIMWV,305,R,16.4,N,A*08");
		windlist.add("$WIMWV,299,R,16.5,N,A*0D");

		currentInstance = this;
	}
	
	@Override
	public void run() {
		for(int i = 0; i < 10; i++) {
			String gprmc = this.gprmclist.get(i);
			this.getConsumer().accept(System.nanoTime(), new StringBuilder(gprmc));
			
			String wimwv = this.windlist.get(i);
			this.getConsumer().accept(System.nanoTime(), new StringBuilder(wimwv));
			
			ToolBox.wait(1);
			
			if(this.isInterrupted())break;
		}
	}
	
	public void injectSentence(String sentence) {
		this.getConsumer().accept(System.nanoTime(), new StringBuilder(sentence));
	}
}
