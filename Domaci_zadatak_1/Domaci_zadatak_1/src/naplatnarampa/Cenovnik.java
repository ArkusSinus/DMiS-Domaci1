package naplatnarampa;

public class Cenovnik {
	
	private int[] putarine;
	
	public Cenovnik(int[] putarine) {
		this.putarine = putarine.clone();
	}
	
	public int brojKategorija() {
		return putarine.length;
	}
	
	public int putarina(Kategorizovano vozilo) {
		
		int kategorija = vozilo.kategorija();
		
		if (kategorija < 0 || kategorija >= putarine.length) {
			throw new IllegalArgumentException(
					"Nepostojeca kategorija vozila: " + kategorija
			);
		}
		
		return putarine[kategorija];
	}
	
}
