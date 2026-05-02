package part_3.gradebook;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Electronic gradebook: grades for one discipline, 3 groups of students, by week.
 * Grades are on a 0–100 scale. All updates are protected by a single lock
 * so that lecturer and assistants can write concurrently without lost updates.
 */
public class Gradebook {

    public static final int NUM_GROUPS = 3;
    public static final int MIN_GRADE = 0;
    public static final int MAX_GRADE = 100;

    private final int studentsPerGroup;
    private final int weeksCount;
    /** grades[group][student][week] */
    private final int[][][] grades;
    private final Lock lock = new ReentrantLock();

    public Gradebook(int studentsPerGroup, int weeksCount) {
        this.studentsPerGroup = studentsPerGroup;
        this.weeksCount = weeksCount;
        this.grades = new int[NUM_GROUPS][studentsPerGroup][weeksCount];
    }

    public void setGrade(int group, int student, int week, int value) {
        if (group < 0 || group >= NUM_GROUPS || student < 0 || student >= studentsPerGroup
                || week < 0 || week >= weeksCount) {
            return;
        }
        int grade = Math.max(MIN_GRADE, Math.min(MAX_GRADE, value));
        lock.lock();
        try {
            grades[group][student][week] = grade;
        } finally {
            lock.unlock();
        }
    }

    public int getGrade(int group, int student, int week) {
        lock.lock();
        try {
            return grades[group][student][week];
        } finally {
            lock.unlock();
        }
    }

    public int getStudentsPerGroup() {
        return studentsPerGroup;
    }

    public int getWeeksCount() {
        return weeksCount;
    }

    private static final String RESET         = "[0m";
    private static final String BOLD          = "[1m";
    private static final String DIM           = "[2m";
    private static final String BRIGHT_CYAN   = "[96m";
    private static final String BRIGHT_YELLOW = "[93m";
    private static final String BRIGHT_GREEN  = "[92m";
    private static final String BRIGHT_RED    = "[91m";
    private static final String MAGENTA       = "[35m";
    private static final String CYAN          = "[36m";

    private static String gradeColor(int v) {
        if (v < 60) return BRIGHT_RED;
        if (v < 75) return BRIGHT_YELLOW;
        return BRIGHT_GREEN;
    }

    /** Builds a colored string table of all grades and group averages. */
    public String formatTable() {
        lock.lock();
        try {
            StringBuilder sb = new StringBuilder();

            for (int g = 0; g < NUM_GROUPS; g++) {
                sb.append(BOLD + BRIGHT_CYAN + "┌─ Group ").append(g + 1).append(" ").append(RESET).append("\n");

                sb.append(DIM + "  " + String.format("%-9s", "Student") + RESET);
                for (int w = 0; w < weeksCount; w++) {
                    sb.append(CYAN + String.format("  Week%-2d", w + 1) + RESET);
                }
                sb.append(MAGENTA + "    Avg" + RESET).append("\n");

                sb.append(DIM + "  " + "─".repeat(9 + weeksCount * 8 + 6) + RESET).append("\n");

                long groupSum = 0;
                int groupCount = 0;
                for (int s = 0; s < studentsPerGroup; s++) {
                    sb.append(DIM + "  " + String.format("%4d     ", s + 1) + RESET);
                    int rowSum = 0;
                    for (int w = 0; w < weeksCount; w++) {
                        int v = grades[g][s][w];
                        sb.append(gradeColor(v))
                          .append(String.format("%4d", v))
                          .append(RESET)
                          .append("   ");
                        rowSum += v;
                        groupSum += v;
                        groupCount++;
                    }
                    double avg = weeksCount > 0 ? (double) rowSum / weeksCount : 0;
                    sb.append(MAGENTA + String.format("  %5.1f", avg) + RESET).append("\n");
                }

                sb.append(DIM + "  " + "─".repeat(9 + weeksCount * 8 + 6) + RESET).append("\n");
                double groupAvg = groupCount > 0 ? (double) groupSum / groupCount : 0;
                sb.append(BOLD + "  Group avg: " + RESET)
                  .append(gradeColor((int) groupAvg))
                  .append(BOLD)
                  .append(String.format("%.1f", groupAvg))
                  .append(RESET).append("\n\n");
            }
            return sb.toString();
        } finally {
            lock.unlock();
        }
    }
}
