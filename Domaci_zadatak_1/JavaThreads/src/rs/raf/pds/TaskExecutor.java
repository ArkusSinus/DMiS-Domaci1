package rs.raf.pds;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskExecutor {

	static class HelloTask implements Runnable {
		  String msg;
		  public HelloTask(String msg) {
			this.msg = msg;
		  }
		  public void run() {
			long id = Thread.currentThread().getId();
			System.out.println(msg + " from thread:" + id);
		  }
	}

	public static void main(String[] args) {
		int ntasks = 1000;
		ExecutorService exs = Executors.newFixedThreadPool(4);
		
		for (int i=0; i<ntasks; i++) {
			HelloTask t = new HelloTask("Hello from task " + i);
			exs.submit(t);
		}
		
		exs.shutdown(); // initiate shutdown, does not wait, but can’t submit more tasks
	}
}
