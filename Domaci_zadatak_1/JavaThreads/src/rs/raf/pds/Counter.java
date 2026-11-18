package rs.raf.pds;

public class Counter implements Runnable{
	public static int MAX_TICKS = 100000;
	
	static class Cell {
		private long value = 0;
		
		public void inc(long delta) {
			this.value += delta;
		}
		
		public long get() {
			return value;
		}
	}
	
	public int ticks = -1;
	
	private Cell cell;
	private int delta;
	private int maxTicks;
	
	Counter(Cell cell, int delta, int maxTicks){
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
		Cell cell = new Cell();
		
		Counter up= new Counter(cell, 1, MAX_TICKS);
		Counter down=new Counter(cell,-1, MAX_TICKS);
		
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
