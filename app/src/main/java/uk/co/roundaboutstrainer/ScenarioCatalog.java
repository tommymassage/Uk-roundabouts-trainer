package uk.co.roundaboutstrainer;

/** Fixed catalog of learner scenarios available before live traffic is introduced. */
public final class ScenarioCatalog {
    private ScenarioCatalog() {}

    public static TrainingScenario[] all() {
        TrainingScenario[] items = new TrainingScenario[8];
        int i = 0;
        for (boolean spiral : new boolean[]{false, true}) {
            for (int exit = 1; exit <= 4; exit++) {
                items[i++] = TrainingScenario.create(spiral, exit);
            }
        }
        return items;
    }

    public static int count() { return 8; }
}
