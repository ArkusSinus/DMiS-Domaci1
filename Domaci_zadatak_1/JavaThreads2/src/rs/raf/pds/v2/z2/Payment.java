package rs.raf.pds.v2.z2;

public class Payment {
	final int accountId1;
	final int accountId2;
	final int amount;
	
	public Payment(int accountId1, int accountId2, int amount) {
		this.accountId1 = accountId1;
		this.accountId2 = accountId2;
		this.amount = amount;
	}
	
	public static Payment newPayment(int maxAccountId, int maxAmount) {
		int acc1 = (int) (((double)maxAccountId)*Math.random())+1;
		int acc2 = (int) (((double)maxAccountId)*Math.random())+1;
		if (acc2 == acc1)
			if ((acc2-1)!=0)
				acc2--;
			else 
				acc2++;
		
		//int amount = (int) (((double)maxAccountId)*(Math.random()-0.5)*2.0);
		int amount = (int) (((double)maxAmount)*(Math.random()-0.5)*2.0); //umesto maxAccountId koristimo maxAmount, pa je granica prava
		Payment p = new Payment(acc1, acc2, amount);
		
		return p;
		
	}
}
