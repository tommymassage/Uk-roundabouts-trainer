package uk.co.roundaboutstrainer;

/** One learner exercise. Keeps training logic independent from the renderer. */
public final class TrainingScenario {
    public final boolean spiral;
    public final int exit;
    public final UkRoundaboutRules.ApproachLane approachLane;
    public final UkRoundaboutRules.Signal approachSignal;
    public final String hint;

    private TrainingScenario(boolean spiral, int exit) {
        this.spiral = spiral;
        this.exit = exit;
        this.approachLane = UkRoundaboutRules.approachLane(spiral, exit);
        this.approachSignal = UkRoundaboutRules.approachSignal(exit);
        this.hint = UkRoundaboutRules.learnerHint(spiral, exit);
    }

    public static TrainingScenario create(boolean spiral, int exit) {
        if (exit < 1 || exit > 4) throw new IllegalArgumentException("Exit must be 1..4");
        return new TrainingScenario(spiral, exit);
    }

    public String title() {
        return (spiral ? "Spiral" : "2 Lane") + " • Exit " + exit;
    }

    public String laneLabel() {
        switch (approachLane) {
            case LEFT: return "LEFT LANE";
            case RIGHT: return "RIGHT LANE";
            default: return "FOLLOW ROAD MARKINGS";
        }
    }
}
