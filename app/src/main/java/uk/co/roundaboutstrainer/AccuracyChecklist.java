package uk.co.roundaboutstrainer;

/** Items intentionally kept editable and reviewed before final release. */
public final class AccuracyChecklist {
    private AccuracyChecklist() {}

    public static final String[] TWO_LANE = {
        "Approach lane allocation matches signs and road markings",
        "Give-way marking position and shape",
        "Splitter island and kerb geometry",
        "Entry deflection and lane continuity",
        "Circulating lane line geometry",
        "Exit lane continuity",
        "Directional arrows",
        "Approach and exit signalling demonstration",
        "Vehicle path remains inside selected lane"
    };

    public static final String[] SPIRAL = {
        "Destination lane assignment",
        "Spiral lane transitions",
        "Lane arrows and destination markings",
        "No unintended lane crossing",
        "Correct exit lane alignment",
        "Give-way and splitter island geometry",
        "Signal timing demonstration"
    };

    public static int totalItems() { return TWO_LANE.length + SPIRAL.length; }
}
