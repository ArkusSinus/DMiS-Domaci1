package rs.raf.pds.v2.z3;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public class Indexer implements Runnable {
	
	private final BlockingQueue<File> queue;
	
	//private final ConcurrentHashMap<String, File> map;
	private final ConcurrentHashMap<String, Integer> map;
	
	private final LongAdder totalOccurrences; // C2: Zajednicki brojac
	private final String keyword;
	
	private volatile int indexCounter = 0;
	
	public Indexer(BlockingQueue<File> queue, ConcurrentHashMap<String, Integer> map, LongAdder totalOccurrences, String keyword) { //Prosiren konstruktor
		 this.queue = queue;
	     this.map = map;
	     this.totalOccurrences = totalOccurrences;
	     this.keyword = keyword;
	}
	
	
//	public void run() {
//		try {
//			while (true) {
//				
//				indexFile(queue.take());
//				indexCounter++;
//			}
//		} catch (InterruptedException e) {
//			Thread.currentThread().interrupt();
//		}
//		System.out.printf(Thread.currentThread().getName()+" indeksirao %d fajlova\n", indexCounter);
//	}
	
//	@Override
//	public void run() {
//	    try {
//	        while (true) {
//
//	            File file = queue.take();
//
//	            if (file == FileCrawler.POISON_PILL) {
//	                break;
//	            }
//
//	            indexFile(file);
//	            indexCounter++;
//	        }
//	    } catch (InterruptedException e) {
//	        Thread.currentThread().interrupt();
//	    }
//
//	    System.out.printf(Thread.currentThread().getName() + " indeksirao %d fajlova\n", indexCounter);
//	}
	@Override
    public void run() {
        try {
            while (true) {
            	// C1: Fajl prvo preuzimamo u promenljivu
                // kako bismo proverili signal za gasenje.
                File file = queue.take();
                
                // C1: Umesto prekidanja niti pomocu
                // interrupt(), zavrsavamo rad tek kada
                // potrosac preuzme poseban poison pill.
                if (file == FileCrawler.POISON_PILL) {
                    break;
                }

                indexFile(file);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.printf("%s indeksirao %d fajlova%n", Thread.currentThread().getName(),indexCounter);
    }
	
	
	
//	void indexFile(File file) {
//		map.put(file.getAbsolutePath(), file);
//	}
	void indexFile(File file) { // C2: Originalna metoda je samo upisivala fajl u mapu. Sada cita sadrzaj i broji pojavljivanja.

        String name = file.getName().toLowerCase();

        
        if (!name.endsWith(".txt") && !name.endsWith(".java")) { // Gledaju se samo .txt i .java fajli
            return;
        }

        int count = 0;

        try {
            for (String line : Files.readAllLines(
                    file.toPath(), StandardCharsets.UTF_8)) {

                int position = 0;
                
                // C2: indexOf() pronalazi sledece
                // pojavljivanje reci, a -1 oznacava
                // da vise nema pogodaka u tom redu.
                while ((position = line.indexOf(keyword, position)) != -1) {
                    count++;
                    position += keyword.length();
                }
            }

      
            map.put(file.getAbsolutePath(), count); // C2: Cuvamo rezultat za konkretan fajl.

            // Dodavanje ukupnom broju pojavljivanja
            totalOccurrences.add(count);

            indexCounter++;

        } catch (IOException e) {
            System.out.println(
                "Greska pri citanju fajla: " + file.getPath()
            );
        }
    }
	
	
	public int getCounter() {
		return indexCounter;
	}
}
