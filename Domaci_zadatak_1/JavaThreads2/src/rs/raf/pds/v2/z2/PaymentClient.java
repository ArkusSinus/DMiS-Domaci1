package rs.raf.pds.v2.z2;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class PaymentClient implements Runnable {

	private final BlockingQueue<Payment> transactionsQueue;
	private final CountDownLatch synchronizer;
	private final int maxAccountId;
	private final int maxAmount;
	private final int maxPayments;
	
	public PaymentClient(BlockingQueue<Payment> transactionsQueue, CountDownLatch synchronizer, int maxAccountId, int maxAmount, int maxPayments) {
		this.transactionsQueue = transactionsQueue;
		this.synchronizer = synchronizer;
		this.maxAccountId = maxAccountId;
		this.maxAmount = maxAmount;
		this.maxPayments = maxPayments;
	}
	@Override
	public void run() {
		try {
			for (int i=0; i<maxPayments; i++) {
				transactionsQueue.put(Payment.newPayment(maxAccountId, maxAmount)); //proizvodjac generise transakciju i dodaje je u zajednicki red.
				
				Thread.sleep((long)((double)2*Math.random()));
			}
		} catch (InterruptedException e) {
					e.printStackTrace();
		}
		
		// Obavestava glavnu nit da je proizvodjac zavrsio generisanje.
		synchronizer.countDown();
		System.out.println(Thread.currentThread().getName()+" je zavrsio rad!");
	}
	
}
