
package rs.raf.pds;

import java.util.function.LongSupplier;

public class CounterBenchmark {

    private static final int MAX_TICKS = 1000000;
    private static final int RUNS = 10;

   
    static class Result {  // Cuva rezultat i trajanje jednog pokretanja
        long value;
        double timeMs;

        Result(long value, double timeMs) {
            this.value = value;
            this.timeMs = timeMs;
        }
    }

    
    
    private static Result execute(Runnable up, Runnable down, LongSupplier getValue) throws InterruptedException { // Pokrece dve niti, ceka njihov zavrsetak i vraca konacni rezultat i vreme izvrsavanja.

        Thread upWorker = new Thread(up);
        Thread downWorker = new Thread(down);

        long start = System.nanoTime();

        upWorker.start();
        downWorker.start();

        upWorker.join();
        downWorker.join();

        long end = System.nanoTime();

        double timeMs = (end - start) / MAX_TICKS;

        return new Result(getValue.getAsLong(), timeMs);
    }

    
    private static Result runOnce(String type) // Pravi odgovarajuci brojac i pokrece jedan test
            throws InterruptedException {

        switch (type) {

            case "Counter": {
                Counter.Cell cell = new Counter.Cell();

                return execute(
                    new Counter(cell, 1, MAX_TICKS),
                    new Counter(cell, -1, MAX_TICKS),
                    cell::get
                );
            }

            case "CounterSync": {
                CounterSync.CellSync cell =
                    new CounterSync.CellSync();

                return execute(
                    new CounterSync(cell, 1, MAX_TICKS),
                    new CounterSync(cell, -1, MAX_TICKS),
                    cell::get
                );
            }

            case "CounterLock": {
                CounterLock.CellLock cell =
                    new CounterLock.CellLock();

                return execute(
                    new CounterLock(cell, 1, MAX_TICKS),
                    new CounterLock(cell, -1, MAX_TICKS),
                    cell::get
                );
            }

            case "CounterAtom": {
                CounterAtom.CellAtom cell =
                    new CounterAtom.CellAtom();

                return execute(
                    new CounterAtom(cell, 1, MAX_TICKS),
                    new CounterAtom(cell, -1, MAX_TICKS),
                    cell::get
                );
            }

            case "CounterAdder": {
                CounterAdder.CellAdder cell =
                    new CounterAdder.CellAdder();

                return execute(
                    new CounterAdder(cell, 1, MAX_TICKS),
                    new CounterAdder(cell, -1, MAX_TICKS),
                    cell::get
                );
            }

            default:
                throw new IllegalArgumentException(
                    "Nepoznat tip brojaca: " + type
                );
        }
    }

    
    
    private static void benchmark(String type) // Pokrece jednu implementaciju 10 puta i racuna statistiku
            throws InterruptedException {

        int wrongResults = 0;
        long maxDeviation = 0;
        double totalTime = 0;

        for (int i = 0; i < RUNS; i++) {

            Result result = runOnce(type);

            if (result.value != 0) {
                wrongResults++;
            }

            long deviation = Math.abs(result.value);

            if (deviation > maxDeviation) {
                maxDeviation = deviation;
            }

            totalTime += result.timeMs;
        }

        double averageTime = totalTime / RUNS;

        System.out.printf(
            "%-15s %-15d %-20d %.3f ms%n",
            type, wrongResults, maxDeviation, averageTime
        );
    }

    public static void main(String[] args)
            throws InterruptedException {

        System.out.println(
            "MAX_TICKS = " + MAX_TICKS +
            ", broj ponavljanja = " + RUNS
        );

        System.out.printf(
            "%-15s %-15s %-20s %s%n",
            "Brojac", "Netacnih", "Max odstupanje", "Prosecno vreme"
        );

        benchmark("Counter");
        benchmark("CounterSync");
        benchmark("CounterLock");
        benchmark("CounterAtom");
        benchmark("CounterAdder");
    }
}
