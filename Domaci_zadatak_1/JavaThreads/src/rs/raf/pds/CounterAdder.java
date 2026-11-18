package rs.raf.pds;

import java.util.concurrent.atomic.LongAdder;

public class CounterAdder implements Runnable{
	public static int MAX_TICKS = 100000;
	
	
	static class CellAdder {
		private LongAdder value = new LongAdder(); //Za razliku od AtomicLong, LongAdder moze raspodeliti azuriranja na vise unutrasnjih celija i tako smanjiti nadmetanje izmedju niti.
		
		public void inc(long delta) {
			value.add(delta); //bezbedno dodaje ili oduzima vrednost
		}
		
		public long get() {
			return value.sum(); //vraca zbir unutrasnjih vrednosti
		}
	}
	
	
	public int ticks = -1;
	
	private CellAdder cell;
	private int delta;
	private int maxTicks;
	
	CounterAdder(CellAdder cell, int delta, int maxTicks){
		this.cell = cell;
		this.delta = delta;
		this.maxTicks = maxTicks;
	}
	
	
	
	@Override
	public void run() {
		ticks = 0;

        while (ticks < maxTicks) {
            cell.inc(delta);
            ++ticks;
        }
		
	}

	public static void main(String[] args) {
		CellAdder cell = new CellAdder();
		
		CounterAdder up= new CounterAdder(cell, 1, MAX_TICKS);
		CounterAdder down=new CounterAdder(cell,-1, MAX_TICKS);
		
		Thread upWorker = new Thread(up);
		Thread downWorker = new Thread(down);
		
		upWorker.start(); downWorker.start();
		try {
			upWorker.join(); downWorker.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Konacna vrednost:"+cell.get());
	}



	
}
