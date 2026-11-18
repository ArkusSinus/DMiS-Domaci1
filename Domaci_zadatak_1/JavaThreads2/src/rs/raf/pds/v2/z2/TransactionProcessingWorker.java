package rs.raf.pds.v2.z2;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
//import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionProcessingWorker implements Runnable{

	//public static volatile boolean running = true;
	private static final AtomicInteger rejectedPayments = new AtomicInteger(0); //b2: Brojac odbijenih uplata mora biti thread-safe jer vise radnika moze istovremeno odbiti transfer
	private static final AtomicInteger processedPayments = new AtomicInteger(0); //B4: Dodajemo brojac svih obradjenih uplata, ukljucujuci i uspesne i odbijene transakcije.
	
	private static final Payment POISON_PILL = new Payment(0, 0, 0); // B3: Umesto promenljive running koristimo poseban Payment objekat za gasenje radnih niti.
	
	private final BlockingQueue<Payment> transactionsQueue;
	ConcurrentHashMap<Integer, Account> bankAccounts;
	
	public TransactionProcessingWorker(BlockingQueue<Payment> transactionsQueue, ConcurrentHashMap<Integer, Account> bankAccounts) {
		this.transactionsQueue = transactionsQueue;
		this.bankAccounts = bankAccounts;
	}
	
	@Override
	public void run() {
	    try {
	        while (true) {
	        	//Originalni poll() je pravio aktivno cekanj i nepotrebno trosio procesorsko vreme.
	        	//B3: take() ceka novu uplatu, a radnik se zavrsava tek kada preuzme poison pill.
	            Payment p = transactionsQueue.take();
 
	            if (p == POISON_PILL) { // B3: Poseban objekat signalizira kraj rada.
	                break;
	            }

	            process(p);
	        }
	    } catch (InterruptedException e) {
	        Thread.currentThread().interrupt();
	    }
	}
	
//	private void process(Payment p) {
//		Account acc1 = bankAccounts.get(p.accountId1);
//		Account acc2 = bankAccounts.get(p.accountId2);
//		
//		if (acc1.getId() < acc2.getId())
//			synchronized(acc1) {
//				synchronized(acc2) {
//					acc1.addAmount(p.amount);
//					acc2.subAmount(p.amount);
//			 }
//			}
//		else 
//			synchronized(acc2) {
//				synchronized(acc1) {
//					 acc1.addAmount(p.amount);
//					 acc2.subAmount(p.amount);
//				}
//			}
//	}

	private void process(Payment p) {
	
	    Account acc1 = bankAccounts.get(p.accountId1);
	    Account acc2 = bankAccounts.get(p.accountId2);
	
	    // Zakljucavanje uvek po rastucem ID-ju.
	    Account first = acc1.getId() < acc2.getId() ? acc1 : acc2;
	    Account second = acc1.getId() < acc2.getId() ? acc2 : acc1;
	
	    synchronized (first) {
	        synchronized (second) {
	
	            // Pozitivan iznos: acc2 -> acc1
	            // Negativan iznos: acc1 -> acc2
	            Account source = p.amount >= 0 ? acc2 : acc1;
	            Account destination = p.amount >= 0 ? acc1 : acc2;
	
	            int amount = Math.abs(p.amount);
	
	            // Provera raspolozivih sredstava
	            if (source.getBalance() >= amount) {
	                source.subAmount(amount);
	                destination.addAmount(amount);
	            } else {
	                rejectedPayments.incrementAndGet(); // B2: Ako nema dovoljno sredstava, ne menjamo racune i brojimo odbijanje.
	            }
	            
	            processedPayments.incrementAndGet(); // B4: Svaku zavrsenu obradu brojimo jednom, bez obzira na to da li je transfer uspesan.
	        }
	    }
	}

	
	public static void main(String[] args) {
		final int BOUND = 50;
		final int N_WORKERS = Runtime.getRuntime().availableProcessors();
		final int MAX_ACCOUNT_ID = 50;
		final int MAX_AMOUNT = 1000;
		final int MAX_PAYMENTS = 100;
		
		//final int NUM_PAYMENT_CLIENTS = (int) (((double)BOUND)*Math.random()); 
		final int NUM_PAYMENT_CLIENTS = (int) (BOUND * Math.random()) + 1; //random br klijenata je mogao biti 0, sada ne moze
		
		BlockingQueue<Payment> transactionsQueue = new LinkedBlockingQueue<Payment>(BOUND);
		CountDownLatch synchronizer = new CountDownLatch(NUM_PAYMENT_CLIENTS);
		
		ConcurrentHashMap<Integer, Account> bankAccounts = initAccounts(MAX_ACCOUNT_ID);
		
		for (int i=0; i<NUM_PAYMENT_CLIENTS; i++) {
			Thread t = new Thread(
					new PaymentClient(transactionsQueue, synchronizer, MAX_ACCOUNT_ID, MAX_AMOUNT, MAX_PAYMENTS));
			t.setName("Payment-Client-"+i);
			t.start();
		}
		
		Thread[] tWorkers = new Thread[N_WORKERS];
		for (int i=0; i<N_WORKERS; i++) {
			TransactionProcessingWorker tpw = new TransactionProcessingWorker(transactionsQueue, bankAccounts);
			tWorkers[i]= new Thread(tpw);
			tWorkers[i].start();
		}
		
		try {
			synchronizer.await();   // Cekamo da svi proizvodjaci zavrse.
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		running = false;
//		
//		try {
//			for (int i=0; i<N_WORKERS; i++)
//				tWorkers[i].join();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		try {
		    // Po jedan poison pill za svakog Worker-a
		    for (int i = 0; i < N_WORKERS; i++) {
		        transactionsQueue.put(POISON_PILL);
		    }

		    // Cekamo da svi Worker-i zavrse
		    for (int i = 0; i < N_WORKERS; i++) {
		        tWorkers[i].join();
		    }

		} catch (InterruptedException e) {
		    Thread.currentThread().interrupt();
		    return;
		}
		
		System.out.println("Ukupno Payment Client-a:"+NUM_PAYMENT_CLIENTS);	
		System.out.println("Ukupno stanje po racunima:");
		
		//Nepotreban sleep
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		int totalBalance = 0;
		for (int i=1; i<=MAX_ACCOUNT_ID; i++) {
			Account acc = bankAccounts.get(i);
			System.out.printf("Account %d, stanje=%d\n", acc.getId(), acc.getBalance());
			totalBalance += acc.getBalance();
		}
		System.out.println("Ukupan zbirni iznos je "+totalBalance); //ukupan zbir svih racuna
		System.out.println("Broj odbijenih transakcija: " + rejectedPayments.get()); //odbijene uplate
		System.out.println("Broj uspesnih uplata: " + (processedPayments.get() - rejectedPayments.get())); //uspesne uplate
	} 
	
	public static ConcurrentHashMap<Integer, Account> initAccounts(int maxAccountId) {
		ConcurrentHashMap<Integer, Account> accounts = new ConcurrentHashMap<Integer, Account>();
		for (int i=1; i<=maxAccountId; i++) {
			//Account acc = new Account(i, 0);
			Account acc = new Account(i, 10000); //racun pocinje sa 10000, pre je sa 0.
			accounts.put(i, acc);
		}
		
		return accounts;
    }

}
