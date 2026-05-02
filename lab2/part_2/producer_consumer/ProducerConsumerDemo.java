package part_2.producer_consumer;

/**
 * Tests the Producer-Consumer pattern with a bounded buffer of integers.
 * Runs tests for buffer sizes 100, 1000, and 5000 and verifies correctness
 * (count, sum, no duplicates, correct sequence).
 */
public class ProducerConsumerDemo {

    /**
     * Number of integers to produce/consume per test (must be >= max buffer size).
     */
    public static final int TOTAL_ITEMS = 10_000;

    private static final String RESET         = "[0m";
    private static final String BOLD          = "[1m";
    private static final String DIM           = "[2m";
    private static final String BRIGHT_CYAN   = "[96m";
    private static final String BRIGHT_YELLOW = "[93m";
    private static final String BRIGHT_GREEN  = "[92m";
    private static final String BRIGHT_RED    = "[91m";
    private static final String CYAN          = "[36m";

    public static void main(String[] args) throws InterruptedException {
        System.out.println(BOLD + BRIGHT_CYAN + "╔══════════════════════════════════════════════════╗" + RESET);
        System.out.println(BOLD + BRIGHT_CYAN + "║        PRODUCER-CONSUMER TEST (Lab 2, Part 2)    ║" + RESET);
        System.out.println(BOLD + BRIGHT_CYAN + "╚══════════════════════════════════════════════════╝" + RESET);
        System.out.println(DIM + "  Synchronization: " + RESET + CYAN + "wait / notifyAll" + RESET
                + DIM + "  |  Items per run: " + RESET + BRIGHT_YELLOW + TOTAL_ITEMS + RESET);
        System.out.println();

        runTest(100);
        runTest(1000);
        runTest(5000);
    }

    private static void runTest(int bufferSize) throws InterruptedException {
        System.out.println(BOLD + BRIGHT_CYAN + "── Buffer size: " + BRIGHT_YELLOW + bufferSize + RESET);

        BoundedBuffer buffer = new BoundedBuffer(bufferSize);
        Producer producer = new Producer(buffer, TOTAL_ITEMS);
        Consumer consumer = new Consumer(buffer, TOTAL_ITEMS);

        long start = System.nanoTime();

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();

        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        int[] received = consumer.getReceived();

        long expectedSum = (long) TOTAL_ITEMS * (TOTAL_ITEMS - 1) / 2;
        long actualSum = 0;
        boolean[] seen = new boolean[TOTAL_ITEMS];
        boolean orderOk = true;

        for (int i = 0; i < TOTAL_ITEMS; i++) {
            int v = received[i];
            actualSum += v;
            if (v < 0 || v >= TOTAL_ITEMS || seen[v]) {
                orderOk = false;
            } else {
                seen[v] = true;
            }
        }
        boolean noDuplicates = true;
        for (int i = 0; i < TOTAL_ITEMS; i++) {
            if (!seen[i]) {
                noDuplicates = false;
                break;
            }
        }

        boolean ok = (actualSum == expectedSum && noDuplicates && received.length == TOTAL_ITEMS && orderOk);

        System.out.println(DIM + "  Count:        " + RESET + BRIGHT_YELLOW + received.length
                + DIM + " (expected " + TOTAL_ITEMS + ")" + RESET);
        System.out.println(DIM + "  Sum:          " + RESET + BRIGHT_YELLOW + actualSum
                + DIM + " (expected " + expectedSum + ")" + RESET);
        System.out.println(DIM + "  No duplicates 0.." + (TOTAL_ITEMS - 1) + ": " + RESET
                + (noDuplicates ? BRIGHT_GREEN + "true" : BRIGHT_RED + "false") + RESET);
        System.out.println(DIM + "  Result:       " + RESET
                + (ok ? BOLD + BRIGHT_GREEN + "CORRECT" : BOLD + BRIGHT_RED + "INCORRECT") + RESET);
        System.out.println(DIM + "  Time:         " + RESET + BRIGHT_YELLOW + elapsedMs + " ms" + RESET);
        System.out.println();
    }
}
