package rs.raf.pds;

public class ParallelThread implements Runnable{
	private int num;
	
	public ParallelThread(int number) {
		this.num = number;
	}

	@Override
	public void run() {
		for (int i = 1; i <= 5; i++){
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
		
	}

	public static void main(String[] args) {
		//Pokretanje 5 niti sa različitim parametrom za mnozenje
		for (int i=1; i <= 5; i++){
			ParallelThread th = new ParallelThread(i);
			
			Thread newThread= new Thread(th);
			newThread.start();
			
			newThread.setPriority(Thread.MAX_PRIORITY);
		}
	}
}
