package rs.raf.pds;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CounterLock implements Runnable{
	public static int MAX_TICKS = 100000;
	
	static class CellLock {
		Lock lock = new ReentrantLock();
		private long value = 0;
		
		public void inc(long delta) {
			lock.lock();
			try { 
				this.value += delta;
			}finally {
				lock.unlock();
			}
		}
		
		public long get() {
			return value;
		}
	}
	
	public int ticks = -1;
	
	private CellLock cell;
	private int delta;
	private int maxTicks;
	
	CounterLock(CellLock cell, int delta, int maxTicks){
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
		CellLock cell = new CellLock();
		
		CounterLock up= new CounterLock(cell, 1, MAX_TICKS);
		CounterLock down=new CounterLock(cell,-1, MAX_TICKS);
		
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
