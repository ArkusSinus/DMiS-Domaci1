package rs.raf.pds;

import java.util.concurrent.atomic.AtomicLong;

public class CounterAtom implements Runnable{
	public static int MAX_TICKS = 100000;
	
	static class CellAtom {
		private AtomicLong value = new AtomicLong(0);
		
		public void inc(long delta) {
			value.getAndAdd(delta);
		}
		
		public long get() {
			return value.get();
		}
	}
	
	public int ticks = -1;
	
	private CellAtom cell;
	private int delta;
	private int maxTicks;
	
	CounterAtom(CellAtom cell, int delta, int maxTicks){
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
		CellAtom cell = new CellAtom();
		
		CounterAtom up= new CounterAtom(cell, 1, MAX_TICKS);
		CounterAtom down=new CounterAtom(cell,-1, MAX_TICKS);
		
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

