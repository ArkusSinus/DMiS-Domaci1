package rs.raf.pds.v2.z2;

//import java.util.concurrent.atomic.AtomicInteger;

public class Account {
	
	private final int accountId;
	//private AtomicInteger balance;
	private int balance; // B2: AtomicInteger je zamenjen obicnim int tipom. Pristup stanju stitimo zakljucavanjem racuna tokom transfera u TransactionProcessingWorker.
	
	public Account(int accountId, int balance) {
		this.accountId = accountId;
		//this.balance = new AtomicInteger(balance);
		this.balance=balance;
	}
	public int getId() {
		return accountId;
	}
	public void addAmount(int value) {
		//balance.addAndGet(value);
		balance += value; //Umesto atomske operacije koristimo obicno sabiranje unutar zakljucanog transfera.
	}
	
	public void subAmount(int value) {
		//balance.addAndGet(-value);
		balance -= value;
	}
	
	public int getBalance() {
		//return balance.get();
		return balance;
	}
	
}
