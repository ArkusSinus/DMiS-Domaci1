package naplatnarampa;

public class NaplatnaRampa {
	
	 private String naziv;
	 private Stanica[] stanice;
	 private double tsr;

	 private volatile boolean otvorena;
	 private Thread generator;
	 
	 private Cenovnik cenovnik;
	 
	 public NaplatnaRampa(String naziv, int brojStanica, double tsr, Stanica stanice) {
		 this.naziv = naziv;
		 this.tsr = tsr;
		 this.stanice = new Stanica[brojStanica];
		 
		 for(int i = 0; i < brojStanica; i++) {
			 this.stanice[i] = stanice.kopija();
		 }
		 
		 this.otvorena = false;
	 }
	 
	 
	 public synchronized void otvori(Cenovnik cenovnik) {
		 if(otvorena) {
			 throw new IllegalArgumentException(
					 "Rampa je vec otvorena!"
			 );
		 }
		 
		 if(stanice == null || stanice.length == 0) {
			 throw new IllegalArgumentException(
					 "Rampa je unistena ili nema stanica!"
			 );
		 }
		 
		 
		 if (cenovnik == null) {
			 throw new IllegalArgumentException(
					 "Cenoivnik ne sme biti null!"
			  );
		 }
		 
		 
		 this.cenovnik = cenovnik;
		 
		 for(Stanica stanica : stanice) {
			 stanica.postaviCenovnik(cenovnik);
		 }
		 
		 
		 otvorena = true;
		 
		 generator = new Thread(() -> simulacija(cenovnik));
		 generator.start();
	 }
	 
	 
	 
	 
	 public synchronized void zatvori() {
		 otvorena = false;
		 
		 if(generator != null) {
			 generator.interrupt();
			 
			 try{
				 generator.join();
			 } catch (InterruptedException e) {
				 Thread.currentThread().interrupt();

		            throw new IllegalStateException(
		                "Prekinuto cekanje za zavrsetak simulacije!", e
		            );
			 }
			 
			 generator = null;
		 }
	 }


	 private void simulacija(Cenovnik cenovnik) {
		
		 while (otvorena) {
			 
			 try {
				 double random = 0.7 + Math.random() * 0.6;
				 
				 long cekanje = (long) (tsr * random);
				 Thread.sleep(cekanje);
				 
				 if(!otvorena) {
					 break;
				 }
				 
				 
				 int kategorijaIzbor = (int) (Math.random() * cenovnik.brojKategorija());
				 Vozilo v = new Vozilo(kategorijaIzbor);
				 
				 
				 int indeks = (int) (Math.random() * stanice.length);
				 stanice[indeks].naplati(v);
			 } catch (InterruptedException e) {
				 break;
			 }
		 }
	 }
	 
	 
	 public synchronized int ukupanNaplaceniIznos() {
		 
		 int ukupno = 0;
		 
		 if(stanice == null) {
			 throw new IllegalArgumentException(
					 "Rampa je unistena!"
			 );
		 }
		 
		 
		 for(Stanica stanica : stanice) {
			 ukupno += stanica.naplaceno();
		 }
		 
		 return ukupno;
	 }
	 
	 
	 public synchronized void unisti() {

		    zatvori();

		    stanice = null;
		    cenovnik = null;
	}
	 
	 
	 @Override
	public String toString() {
		
		 if (stanice == null) {
			 throw new IllegalStateException(
			            "Rampa je unistena!"
			 );
		 }
		 
		 StringBuilder sb = new StringBuilder();
		 
		 sb.append(naziv);
		 sb.append("(");
		 sb.append(ukupanNaplaceniIznos());
		 sb.append(")");
		 
		 for (int i=0; i < stanice.length; i++) {
			 if (i > 0) {
				 sb.append(",");
			 }
			 
			 sb.append(stanice[i]);
		 }
		 
		 return sb.toString();
	}
	 
	 
	 
}

















