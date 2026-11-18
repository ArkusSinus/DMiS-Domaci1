package rs.raf.pds;

import java.lang.Thread.State;

public class ParallelThreadPriority implements Runnable{
	public static final int NUM_THREADS = 6;
	public static final int NUM_LOOPS = 6;
	
	private int num;
		
	public ParallelThreadPriority(int number) {
		this.num = number;
	}

	@Override
	public void run() {
		for (int i = 1; i <= NUM_LOOPS; i++){
			System.out.printf("%s: %d * %d = %d\n",
					Thread.currentThread().getName(), num, i, i*num);
			try {
				Thread.sleep(0);
				//Thread.sleep(1000*(long)(2*Math.random()));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Nit "+Thread.currentThread().getName()+" je završila izvršavanje");
		
	}

	public static void main(String[] args) {
		Thread[] threads = new Thread[NUM_THREADS];
			
		//Pokretanje NUM_THREADS niti sa različitim parametrom za mnozenje
		for (int i=0; i < NUM_THREADS; i++){
			ParallelThreadPriority th = new ParallelThreadPriority(i);
			
			Thread newThread= new Thread(th);
					
			newThread.setPriority(((i+1)%2)==0?Thread.MAX_PRIORITY:Thread.MIN_PRIORITY); 
			newThread.setName("Thread-"+(i+1));
			
			threads[i] = newThread;
			
			newThread.start();
			
		}
		
		try {
			checkIfThreadsCompletedVer2(threads);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Sve niti su završile izvršavanje");
	}
	static void checkIfThreadsCompletedVer1(Thread[] threads) {
		boolean finish = false;
		while (!finish) {
			finish = true;
			for(int i=0; i<NUM_THREADS; i++){
				finish = finish && (threads[i].getState() == State.TERMINATED);
			}
		}
	}
	
	static void checkIfThreadsCompletedVer2(Thread[] threads) throws InterruptedException {
		for(int i=0; i<NUM_THREADS; i++)
			threads[i].join();
	}
}
