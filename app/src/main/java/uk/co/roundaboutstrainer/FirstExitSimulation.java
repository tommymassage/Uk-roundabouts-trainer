package uk.co.roundaboutstrainer;

/**
 * First traffic-simulation scenario for the 2-lane roundabout.
 * Ego car starts on the 6 o'clock approach and takes the first exit (9 o'clock).
 * UK left-hand traffic: approach in the left lane, signal left before entry,
 * give way to circulating traffic from the right, remain in the outer lane,
 * then leave at the first exit.
 */
public final class FirstExitSimulation {
    public enum Phase { APPROACH, GIVE_WAY, ENTER, CIRCULATE, EXIT, COMPLETE }

    public static final int EXIT = 1;
    public static final boolean SPIRAL = false;
    public static final String APPROACH = "6 o'clock";
    public static final String EXIT_DIRECTION = "9 o'clock / first exit";
    public static final String APPROACH_LANE = "LEFT";
    public static final UkRoundaboutRules.Signal APPROACH_SIGNAL = UkRoundaboutRules.Signal.LEFT;

    private FirstExitSimulation() {}

    public static TrainingScenario scenario() {
        return TrainingScenario.create(false, EXIT);
    }

    public static RouteSpec route(RoadGeometrySpec geometry) {
        return RouteSpec.forScenario(scenario(), geometry);
    }

    public static Phase phaseFor(float progress) {
        if (progress < 0.28f) return Phase.APPROACH;
        if (progress < 0.36f) return Phase.GIVE_WAY;
        if (progress < 0.48f) return Phase.ENTER;
        if (progress < 0.72f) return Phase.CIRCULATE;
        if (progress < 0.96f) return Phase.EXIT;
        return Phase.COMPLETE;
    }

    public static String instructionFor(Phase phase) {
        switch (phase) {
            case APPROACH: return "Left lane. Signal left. Approach under control.";
            case GIVE_WAY: return "Give way to traffic already circulating from the right.";
            case ENTER: return "Enter when the gap is safe; keep to the outer lane.";
            case CIRCULATE: return "Keep left and maintain the left signal for the first exit.";
            case EXIT: return "Leave at the first exit and straighten progressively.";
            default: return "First-exit manoeuvre complete.";
        }
    }
}