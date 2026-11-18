package naplatnarampa;

public class Stanica {
	
	private static int sledeciId = 1;

	private final int id;
	private Cenovnik cenovnik;
	private int naplaceno;
	
	public Stanica(Cenovnik cenovnik) {
		this.id = sledeciId++;
		this.cenovnik = cenovnik;
		this.naplaceno = 0;
	}
	
	public synchronized void postaviCenovnik(Cenovnik cenovnik) {
		this.cenovnik = cenovnik;
		this.naplaceno = 0;
	}
	
	public synchronized void naplati(Kategorizovano vozilo) {
		
		if (cenovnik == null) {
			throw new IllegalArgumentException(
					"Cenovnik nije napravljen!"
			);
		}
		
		int iznos = cenovnik.putarina(vozilo);	
		naplaceno += iznos;
	}
	
	
	public synchronized int naplaceno() {
		return naplaceno;
	}
	
	public synchronized Stanica kopija() {
		return new Stanica(cenovnik);
	}
	
	@Override
	public synchronized String toString() {
		return id + "(" + naplaceno + ")";
	}
	
}
