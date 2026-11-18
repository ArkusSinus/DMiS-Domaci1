package rs.raf.pds;

//A2: BigInteger koristimo za tacno racunanje velikih faktorijela bez gubitka preciznosti.
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;

public class FactorielWorker implements Runnable{

	static class FactorielValue {
		//double value = 1.0;
		BigInteger value = BigInteger.ONE; //BigInteger cuva tacan rezultat
		
		AtomicInteger currentValue;
		
		FactorielValue(int factValue){
			currentValue = new AtomicInteger(factValue);
		}
		public int getNextNumber() {
			return currentValue.getAndDecrement();
		}
		public synchronized void mul(BigInteger val) {
			value = value.multiply(val); // umesto *, koristimo ugradjenu f-ju
		}
		
		public synchronized BigInteger get() { //Povratni tip promenjen sa double na BigInteger
			return value;
		}
	}
	
	public static final int FACTORIEL_NUMBER = 150;
	public static final int NUM_THREADS = 3;
	
	private FactorielValue fact;
	
	FactorielWorker(FactorielValue fact){
		this.fact = fact;
	}
	
	@Override
	public void run() {
		//double tempValue = 1.0;
		BigInteger tempValue = BigInteger.ONE; //Opet BigInteger zbog preciznosti
		
		while(true) {
			int nextValue = fact.getNextNumber();	// ovo je atomska operacija, koristi se atomska promenljiva pa ne treba da se stiti sa lock-om!
			
			// Signal za worker nit da zavrsi rad
			if (nextValue <=1)	
				break;
			
			//tempValue *= nextValue;
			tempValue = tempValue.multiply(BigInteger.valueOf(nextValue)); //valueOf() pretvara int u BigInteger, zhbog preciznog mnozenja
			
			//factResult.mul(nextValue); moze i ovako, s tim sto je onda donja naredba suvisna
		}
		fact.mul(tempValue); //Svaka nit samo jednom dodaje svoj rezultat
	}
	
//	public static void main(String[] args) {
//		// Inicijalizujemo pocetnu vrednost za racunanje faktorijela
//		FactorielValue fv = new FactorielValue(FACTORIEL_NUMBER);
//		
//		Thread[] threads = new Thread[NUM_THREADS];
//		
//		long t = System.nanoTime();
//		
//		for (int i=0; i< NUM_THREADS; i++) {
//			FactorielWorker fw = new FactorielWorker(fv); // Referenca na zajednicki objekat FactorielValue se deli svim nitima
//			threads[i] = new Thread(fw);
//			threads[i].start();
//		}
//		
//		// Čekanje da sve niti završe svoj rad
//		for (int i=0; i<NUM_THREADS; i++)
//			try {
//				threads[i].join();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		
//		System.out.printf("Faktorijel od %d je %f\nIzracunat za vreme %f ms\n", FACTORIEL_NUMBER, fv.get(), (double)(System.nanoTime()-t)/10000000.0);
//	}
	public static void main(String[] args) {

        int number = FACTORIEL_NUMBER;
        int numThreads = NUM_THREADS;

        // A2: Originalni kod je koristio fiksne vrednosti
        // 150 i 3. Sada ih mozemo zadati argumentima.
        // Bez argumenata ostaju podrazumevane vrednosti.
        try {
            if (args.length > 2) {
                System.out.println(
                    "Upotreba: FactorielWorker [broj] [brojNiti]"
                );
                return;
            }

            if (args.length >= 1)
                number = Integer.parseInt(args[0]);

            if (args.length == 2)
                numThreads = Integer.parseInt(args[1]);

        } catch (NumberFormatException e) {
            System.out.println("Argumenti moraju biti celi brojevi.");
            return;
	      }
	
	    if (number < 0 || numThreads < 1) {
	    	System.out.println("Broj mora biti >= 0, a broj niti >= 1.");
	        return;
        }

        FactorielValue fv = new FactorielValue(number);

        
        Thread[] threads = new Thread[numThreads]; //Broj niti vise nije fiksan.

        long t = System.nanoTime();

        for (int i = 0; i < numThreads; i++) {
            FactorielWorker fw = new FactorielWorker(fv);
            threads[i] = new Thread(fw);
            threads[i].start();
        }

        // Cekamo sve niti da zavrse.
        for (int i = 0; i < numThreads; i++) {
            try {
                threads[i].join(); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        // IZMENA: nanosekunde u milisekunde.
        double timeMs =
            (System.nanoTime() - t) / 1000000.0;

        System.out.printf("Faktorijel od %d je %s%n" + "Izracunat za vreme %.3f ms%n", number, fv.get(), timeMs); //Umesto %f koristimo %s jer ispisujemo BigInteger, a ne double.
    }
	

}
