package rs.raf.pds;

public class BasicThreadV2 implements Runnable{

	@Override
	public void run() {
		//...		
	}

	public static void main(String[] args) {
		BasicThreadV2 newThread = new BasicThreadV2();
		
		Thread th = new Thread(newThread);
		th.start(); // poziva newThread.run()
		
		Thread th2 = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
			} 		
		});
		th2.start();
	}
}
