package part_1.bank;

/**
 * Asynchronous bank test based on the listing from the lab assignment.
 * Contains one unsafe implementation and three different synchronized variants.
 */
public class AsynchBankTest {

    public static final int NACCOUNTS = 10;
    public static final int INITIAL_BALANCE = 10_000;
    /**
     * How many transfer operations each thread performs.
     */
    public static final int REPS = 100_000;

    private static final String RESET         = "[0m";
    private static final String BOLD          = "[1m";
    private static final String DIM           = "[2m";
    private static final String BRIGHT_CYAN   = "[96m";
    private static final String BRIGHT_YELLOW = "[93m";
    private static final String BRIGHT_RED    = "[91m";
    private static final String BRIGHT_GREEN  = "[92m";

    public static void main(String[] args) throws InterruptedException {
        System.out.println(BOLD + BRIGHT_CYAN + "╔══════════════════════════════════════════════════╗" + RESET);
        System.out.println(BOLD + BRIGHT_CYAN + "║           ASYNCH BANK TEST (Lab 2, Part 1)       ║" + RESET);
        System.out.println(BOLD + BRIGHT_CYAN + "╚══════════════════════════════════════════════════╝" + RESET);
        System.out.println(DIM + "  Accounts: " + RESET + BRIGHT_YELLOW + NACCOUNTS + RESET
                + DIM + "  |  Initial balance: " + RESET + BRIGHT_YELLOW + INITIAL_BALANCE + RESET
                + DIM + "  |  Expected total: " + RESET + BRIGHT_YELLOW + (NACCOUNTS * INITIAL_BALANCE) + RESET);

        runVariantUnsafe();
        runVariantSynchronized();
        runVariantLock();
        runVariantAtomic();
    }

    private static void runVariantUnsafe() throws InterruptedException {
        System.out.println();
        System.out.println(BOLD + BRIGHT_RED + "[ 1 ] UNSAFE" + RESET + BRIGHT_RED
                + " — no synchronization, race conditions expected" + RESET);
        System.out.println(DIM + "      (sum may diverge from " + (NACCOUNTS * INITIAL_BALANCE) + " across runs)" + RESET);

        for (int run = 1; run <= 3; run++) {
            System.out.println(DIM + "      Run " + run + ":" + RESET);
            Bank bank = new UnsafeBank(NACCOUNTS, INITIAL_BALANCE);
            startAndJoinAllThreads(bank);
            System.out.println();
        }
    }

    private static void runVariantSynchronized() throws InterruptedException {
        System.out.println();
        System.out.println(BOLD + BRIGHT_GREEN + "[ 2 ] SYNCHRONIZED methods variant" + RESET);
        Bank bank = new SynchronizedBank(NACCOUNTS, INITIAL_BALANCE);
        startAndJoinAllThreads(bank);
        System.out.println();
    }

    private static void runVariantLock() throws InterruptedException {
        System.out.println();
        System.out.println(BOLD + BRIGHT_GREEN + "[ 3 ] ReentrantLock variant" + RESET);
        Bank bank = new LockBank(NACCOUNTS, INITIAL_BALANCE);
        startAndJoinAllThreads(bank);
        System.out.println();
    }

    private static void runVariantAtomic() throws InterruptedException {
        System.out.println();
        System.out.println(BOLD + BRIGHT_GREEN + "[ 4 ] AtomicIntegerArray + AtomicLong variant" + RESET);
        Bank bank = new AtomicBank(NACCOUNTS, INITIAL_BALANCE);
        startAndJoinAllThreads(bank);
        System.out.println();
    }

    private static void startAndJoinAllThreads(Bank bank) throws InterruptedException {
        TransferThread[] threads = new TransferThread[NACCOUNTS];
        for (int i = 0; i < NACCOUNTS; i++) {
            threads[i] = new TransferThread(bank, i, INITIAL_BALANCE, REPS);
            threads[i].setPriority(Thread.NORM_PRIORITY + i % 2);
            threads[i].start();
        }
        for (TransferThread t : threads) {
            t.join();
        }

        // final consistency check
        if (bank instanceof TestableBank testableBank) {
            testableBank.test();
        }
    }
}
