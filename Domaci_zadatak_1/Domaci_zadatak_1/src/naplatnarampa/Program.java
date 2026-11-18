package naplatnarampa;

public class Program {

	public static void main(String[] args) {
		
		Cenovnik cenovnik = new Cenovnik(
	            new int[]{100, 150, 250, 400}
	        );

	        // Pocetna stanica
	        Stanica pocetna = new Stanica(null);

	        // Kreiranje rampe sa 4 stanice
	        NaplatnaRampa rampa = new NaplatnaRampa(
	            "Beograd",
	            4,
	            1000,
	            pocetna
	        );

	        // Otvaranje rampe
	        rampa.otvori(cenovnik);

	        try {

	            Thread.sleep(10000);

	            System.out.println("Stanje rampe:");
	            System.out.println(rampa);

	            Thread.sleep(10000);

	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	        }

	        // Zatvaranje rampe
	        rampa.zatvori();

	        System.out.println("Konacno stanje:");
	        System.out.println(rampa);

	        // Unistavanje rampe
	        rampa.unisti();

	        System.out.println("Rampa je unistena.");

	}

}
