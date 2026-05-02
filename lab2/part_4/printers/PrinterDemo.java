package part_4.printers;

/**
 * Task 4: Three threads print '|', '\', '/'
 */
public class PrinterDemo {

    public static final int LINES = 50;
    public static final int CHARS_PER_LINE = 150;
    private static final int PRINTS_PER_THREAD = LINES * CHARS_PER_LINE / 3;

    private static final String RESET         = "[0m";
    private static final String BOLD          = "[1m";
    private static final String DIM           = "[2m";
    private static final String BRIGHT_CYAN   = "[96m";
    private static final String BRIGHT_YELLOW = "[93m";
    private static final String MAGENTA       = "[35m";
    private static final String CYAN          = "[36m";
    private static final String YELLOW        = "[33m";

    public static void main(String[] args) throws InterruptedException {
        System.out.println(BOLD + BRIGHT_CYAN + "╔══════════════════════════════════════════════════╗" + RESET);
        System.out.println(BOLD + BRIGHT_CYAN + "║         THREE PRINTERS TEST (Lab 2, Part 4)      ║" + RESET);
        System.out.println(BOLD + BRIGHT_CYAN + "╚══════════════════════════════════════════════════╝" + RESET);
        System.out.println(DIM + "  Symbols: " + RESET
                + BRIGHT_CYAN + "|" + RESET + "  "
                + BRIGHT_YELLOW + "\\" + RESET + "  "
                + MAGENTA + "/" + RESET);
        System.out.println();
        runUncoordinated();
        runCoordinated();
    }

    private static final Object uncoordLock = new Object();
    private static int uncoordCount = 0;

    static class UncoordinatedSymbolThread extends Thread {
        private final String symbol;

        UncoordinatedSymbolThread(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public void run() {
            for (int i = 0; i < PRINTS_PER_THREAD; i++) {
                synchronized (uncoordLock) {
                    System.out.print(symbol);
                    uncoordCount++;
                    if (uncoordCount % CHARS_PER_LINE == 0) {
                        System.out.println();
                    }
                }
            }
        }
    }

    private static void runUncoordinated() throws InterruptedException {
        System.out.println(BOLD + BRIGHT_CYAN + "── UNCOORDINATED" + RESET
                + DIM + " — " + LINES + " lines × " + CHARS_PER_LINE + " chars" + RESET);
        uncoordCount = 0;

        Thread t1 = new UncoordinatedSymbolThread(CYAN   + "|" + RESET);
        Thread t2 = new UncoordinatedSymbolThread(YELLOW + "\\" + RESET);
        Thread t3 = new UncoordinatedSymbolThread(MAGENTA + "/" + RESET);

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();

        System.out.println();
    }

    private static final Object lock = new Object();
    private static int state = 0;

    static class CoordinatedSymbolThread extends Thread {
        private final String symbol;
        private final int targetState;

        CoordinatedSymbolThread(String symbol, int targetState) {
            this.symbol = symbol;
            this.targetState = targetState;
        }

        @Override
        public void run() {
            for (int i = 0; i < PRINTS_PER_THREAD; i++) {
                synchronized (lock) {
                    while (state % 3 != targetState) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.print(symbol);
                    state++;
                    if (state % CHARS_PER_LINE == 0) {
                        System.out.println();
                    }
                    lock.notifyAll();
                }
            }
        }
    }

    private static void runCoordinated() throws InterruptedException {
        System.out.println(BOLD + BRIGHT_CYAN + "── COORDINATED" + RESET
                + DIM + " — " + LINES + " lines × " + CHARS_PER_LINE + " chars, pattern " + RESET
                + CYAN + "|" + RESET + YELLOW + "\\" + RESET + MAGENTA + "/" + RESET
                + DIM + "  repeating" + RESET);
        state = 0;

        Thread t1 = new CoordinatedSymbolThread(CYAN   + "|" + RESET, 0);
        Thread t2 = new CoordinatedSymbolThread(YELLOW + "\\" + RESET, 1);
        Thread t3 = new CoordinatedSymbolThread(MAGENTA + "/" + RESET, 2);

        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
        System.out.println();
    }
}
