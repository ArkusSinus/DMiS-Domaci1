package rs.raf.pds.v2;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

public class VolatileExample implements Runnable {
	private volatile boolean running = false;
	private Thread thread = null;

	public void start() {
		if (!running) {
			thread = new Thread(this, "Vreme");
			running = true;
			thread.start();
		}
	}
	
	public void run() {
		while (running) {
			ispisVremena();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.out.println("Interrupted");
			}
		}
		System.out.println("Nit za ispis vremena je završila rad!");
	}
	
	public void ispisVremena() {
		Calendar cal = Calendar.getInstance();
		Date date = cal.getTime();
		DateFormat dateFormatter = DateFormat.getTimeInstance();
		System.out.println(dateFormatter.format(date));
	}
	
	public void stop() {
		Thread stopThread = thread;
		thread = null;
		running = false;
		stopThread.interrupt();
	}
	
	public static void main(String[] args) {
		VolatileExample ve = new VolatileExample();
		ve.start();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ve.stop();
	}
}
