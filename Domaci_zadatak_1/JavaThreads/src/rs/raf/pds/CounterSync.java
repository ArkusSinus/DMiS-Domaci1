package rs.raf.pds;

public class CounterSync implements Runnable{
	public static int MAX_TICKS = 100000;
	
	static class CellSync {
		private long value = 0;
		
		public synchronized void inc(long delta) {
			this.value += delta;
		}
		
		public long get() {
			return value;
		}
	}
	
	public int ticks = -1;
	
	private CellSync cell;
	private int delta;
	private int maxTicks;
	
	CounterSync(CellSync cell, int delta, int maxTicks){
		this.cell = cell;
		this.delta = delta;
		this.maxTicks = maxTicks;
	}
	
	@Override
	public void run() {
		ticks=0;
		while(ticks<maxTicks){
			cell.inc(delta);
			++ticks;
		}
	}
	
	public static void main(String[]args){
		CellSync cell = new CellSync();
		
		CounterSync up= new CounterSync(cell, 1, MAX_TICKS);
		CounterSync down=new CounterSync(cell,-1, MAX_TICKS);
		
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
