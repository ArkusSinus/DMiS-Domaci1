package rs.raf.pds.v2.z3;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

public class FileCrawler implements Runnable{
	
	
	//dodato za C1: Umesto prekidanja Indexer niti koristimo  objekat koji signalizira kraj rada bez gubitka fajlova.
	public static final File POISON_PILL = new File("__POISON_PILL__");
	
	private final BlockingQueue<File> fileQueue;
	private final File root;
	
	FileCrawler(BlockingQueue<File> fileQueue, File root){
		this.fileQueue = fileQueue;
		this.root = root;
		
	}
	public void run() {
		try {
			crawl(root);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	
	private void crawl(File root) throws InterruptedException {
		
		// C1: Pocetni kod je preskakao fajlove direktno  u pocetnom direktorijumu jer listFiles() nad dajlom vraca null. Sada ih dodajemo u red.
		if (root.isFile()) {
	        fileQueue.put(root);
	        return;
	    }
		
		File[] entries = root.listFiles();
		
		if (entries != null) {
			for (File entry : entries)
				if (entry.isDirectory()) {
					System.out.println("Indeksiranje direktorijuma:"+entry.getPath());
					crawl(entry);
				}
				else //if (!alreadyIndexed(entry))
					fileQueue.put(entry);
		}
	}
	
	
	//C2: Dodat parametar keyword
	public static void startIndexing(File root, String keyword) {
		final int BOUND = 100;
		final int N_CONSUMERS = Runtime.getRuntime().availableProcessors();
		
		List<Thread> producers = new LinkedList<Thread>(); 
		Thread[] consumers = new Thread[N_CONSUMERS];
		
		BlockingQueue<File> queue = new LinkedBlockingQueue<File>(BOUND);
		
		
		 // C2: Originalna mapa je cuvala File objekte. Sada cuva putanju i broj pojavljivanja reci.
		//ConcurrentHashMap<String, File> indexMap = new ConcurrentHashMap<String, File>();
		ConcurrentHashMap<String, Integer> indexMap = new ConcurrentHashMap<String, Integer>();
		
		LongAdder totalOccurrences = new LongAdder();// C2: LongAdder omogucava sabiranje rezultata iz vise Indexer niti.
		
		
		for (File f : root.listFiles()) {
			Thread t = new Thread(new FileCrawler(queue, f));
			producers.add(t);
			t.start();
		}
		System.out.printf("Startovano %d producer-a\n", producers.size());
		
		for (int i = 0; i < N_CONSUMERS; i++) {
			//consumers[i] = new Thread(new Indexer(queue, indexMap));
			consumers[i] = new Thread(new Indexer(queue, indexMap, totalOccurrences, keyword));// C2: Indexer sada dobija zajednicku mapu,ukupan brojac i kljucnu rec za pretragu.
			consumers[i].setName("Consumer-"+(i+1));
			consumers[i].start();
		}
		
		// C1: Cekamo sve proizvodjace pre slanja
        // signala za gasenje potrosaca.
		for (Thread producer: producers)
			try {
				producer.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
//		for (int i = 0; i < N_CONSUMERS; i++) {
//			consumers[i].interrupt();
//		}
		
		// C1: Originalni interrupt() je mogao prekinuti potrosace dok su fajlovi jos bili u redu. Dodajemo po jedan poison pill za svakog potrosaca.
		for (int i = 0; i < N_CONSUMERS; i++) {
		    try {
		        queue.put(POISON_PILL);
		    } catch (InterruptedException e) {
		        Thread.currentThread().interrupt();
		        return;
		    }
		}
		
		
		for (int i = 0; i < N_CONSUMERS; i++) {
			try {
				consumers[i].join(); // C1: Cekamo da potrosaci obrade sve fajlove i zavrse rad nakon preuzimanja poison pill-a.
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.printf("Zavrseno indeksiranje. Ukupno %d fajlova!\n", indexMap.size());
		

	    // C2: Ukupan broj pojavljivanja
	    System.out.println("Ukupan broj pojavljivanja: " + totalOccurrences.sum());

	    // C2: Pronalazenje fajla sa najvise pojavljivanja
	    String maxFile = null;
	    int maxCount = -1;

	    for (Map.Entry<String, Integer> entry : indexMap.entrySet()) {

	        if (entry.getValue() > maxCount) {
	            maxCount = entry.getValue();
	            maxFile = entry.getKey();
	        }
	    }

	    if (maxFile != null) {
	        System.out.println("Fajl sa najvise pojavljivanja: " + maxFile);

	        System.out.println("Broj pojavljivanja u fajlu: " + maxCount);
	    } else {
	        System.out.println("Nema procitanih .txt ili .java fajlova.");
	    }

	}
	
	public static void main(String[] args) {
//		if (args.length != 1) {
//			System.out.println("Startovanje: FileCrawl naziv_direktorijuma");
//			System.exit(0);
//		}
//		File root = new File(args[0]);
//		startIndexing(root);
		
	    if (args.length != 2 || args[1].isEmpty()) {  // obavezni argumenti su: direktorijum i kljucna rec.
	        System.out.println(
	            "Nacin unosa: FileCrawler <direktorijum> <kljucna_rec>"
	        );
	        return;
	    }

	    File root = new File(args[0]);

	    if (!root.isDirectory()) { // C2: Proveravamo da li je prosledjena putanja validan direktorijum.
	        System.out.println("Greska: putanja nije direktorijum.");
	        return;
	    }

	    String keyword = args[1];

	    startIndexing(root, keyword);
	}
}
