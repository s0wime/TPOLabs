package part_3.gradebook;

/**
 * Runs the electronic gradebook: one lecturer and three assistants assign
 * grades concurrently; after they finish, the grade table is printed.
 */
public class GradebookDemo {

    public static final int STUDENTS_PER_GROUP = 5;
    public static final int WEEKS_COUNT = 4;

    private static final String RESET         = "[0m";
    private static final String BOLD          = "[1m";
    private static final String DIM           = "[2m";
    private static final String BRIGHT_CYAN   = "[96m";
    private static final String BRIGHT_YELLOW = "[93m";
    private static final String CYAN          = "[36m";

    public static void main(String[] args) throws InterruptedException {
        System.out.println(BOLD + BRIGHT_CYAN + "╔══════════════════════════════════════════════════╗" + RESET);
        System.out.println(BOLD + BRIGHT_CYAN + "║      ELECTRONIC GRADEBOOK (Lab 2, Part 3)        ║" + RESET);
        System.out.println(BOLD + BRIGHT_CYAN + "╚══════════════════════════════════════════════════╝" + RESET);
        System.out.println(DIM + "  Writers: " + RESET + CYAN + "1 lecturer + 3 assistants" + RESET
                + DIM + "  |  Lock: " + RESET + CYAN + "ReentrantLock" + RESET);
        System.out.println(DIM + "  Groups: " + RESET + BRIGHT_YELLOW + "3" + RESET
                + DIM + "  |  Students/group: " + RESET + BRIGHT_YELLOW + STUDENTS_PER_GROUP + RESET
                + DIM + "  |  Weeks: " + RESET + BRIGHT_YELLOW + WEEKS_COUNT + RESET);
        System.out.println();

        Gradebook gradebook = new Gradebook(STUDENTS_PER_GROUP, WEEKS_COUNT);

        LecturerThread lecturer = new LecturerThread(gradebook);
        AssistantThread assistant0 = new AssistantThread(gradebook, 0);
        AssistantThread assistant1 = new AssistantThread(gradebook, 1);
        AssistantThread assistant2 = new AssistantThread(gradebook, 2);

        lecturer.start();
        assistant0.start();
        assistant1.start();
        assistant2.start();

        lecturer.join();
        assistant0.join();
        assistant1.join();
        assistant2.join();

        System.out.println(gradebook.formatTable());
    }
}
