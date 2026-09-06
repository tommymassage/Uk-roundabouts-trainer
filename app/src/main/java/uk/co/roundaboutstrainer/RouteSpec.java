package uk.co.roundaboutstrainer;

/** Editable route description used by the renderer before traffic simulation is added. */
public final class RouteSpec {
    public final boolean spiral;
    public final int exit;
    public final UkRoundaboutRules.ApproachLane approachLane;
    public final UkRoundaboutRules.Signal approachSignal;
    public final float exitSignalPoint;
    public final String routeId;

    private RouteSpec(boolean spiral, int exit) {
        this.spiral = spiral;
        this.exit = exit;
        this.approachLane = UkRoundaboutRules.approachLane(spiral, exit);
        this.approachSignal = UkRoundaboutRules.approachSignal(exit);
        this.exitSignalPoint = UkRoundaboutRules.exitSignalPoint(exit);
        this.routeId = (spiral ? "spiral" : "two_lane") + "_exit_" + exit;
    }

    public static RouteSpec forScenario(boolean spiral, int exit) {
        if (exit < 1 || exit > 4) throw new IllegalArgumentException("Exit must be 1..4");
        return new RouteSpec(spiral, exit);
    }
}
