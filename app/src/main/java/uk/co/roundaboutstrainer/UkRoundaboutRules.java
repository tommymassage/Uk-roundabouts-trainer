package uk.co.roundaboutstrainer;

/** Central rule model. Geometry remains editable separately from rules. */
public final class UkRoundaboutRules {
    private UkRoundaboutRules() {}

    public enum ApproachLane { LEFT, RIGHT, FOLLOW_MARKINGS }
    public enum Signal { LEFT, NONE, RIGHT }

    public static ApproachLane approachLane(boolean spiral, int exit) {
        if (spiral && exit >= 2) return ApproachLane.FOLLOW_MARKINGS;
        return exit <= 2 ? ApproachLane.LEFT : ApproachLane.RIGHT;
    }

    public static Signal approachSignal(int exit) {
        if (exit == 1) return Signal.LEFT;
        if (exit >= 3) return Signal.RIGHT;
        return Signal.NONE;
    }

    public static Signal circulatingSignal(int exit, float progress) {
        if (shouldSignalLeftToExit(exit, progress)) return Signal.LEFT;
        if (exit >= 3 && progress < exitSignalPoint(exit)) return Signal.RIGHT;
        return Signal.NONE;
    }

    public static boolean shouldSignalLeftToExit(int exit, float routeProgress) {
        if (exit == 1) return true;
        return routeProgress >= exitSignalPoint(exit);
    }

    public static float exitSignalPoint(int exit) {
        switch (exit) {
            case 2: return 0.63f;
            case 3: return 0.72f;
            case 4: return 0.82f;
            default: return 0f;
        }
    }

    public static String signalLabel(Signal s) {
        switch (s) {
            case LEFT: return "LEFT SIGNAL";
            case RIGHT: return "RIGHT SIGNAL";
            default: return "NO SIGNAL";
        }
    }

    public static String learnerHint(boolean spiral, int exit) {
        if (spiral) return "Follow lane signs and road markings; keep within your marked lane and signal left before leaving.";
        if (exit == 1) return "Use the left lane, signal left and take the first exit.";
        if (exit == 2) return "Use the appropriate lane for the road markings; signal left after passing the exit before yours.";
        return "Normally approach in the right lane; signal right, then signal left before leaving.";
    }
}