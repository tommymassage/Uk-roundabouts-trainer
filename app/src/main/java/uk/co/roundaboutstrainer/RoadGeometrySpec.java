package uk.co.roundaboutstrainer;

/** Normalised geometry values (0..1). Tune these during the later UK accuracy pass. */
public final class RoadGeometrySpec {
    public final float centreX = 0.50f;
    public final float centreY = 0.50f;
    public final float outerRadius = 0.215f;
    public final float innerRadius = 0.145f;

    public final float approachLeftCentre = 0.445f;
    public final float approachRightCentre = 0.555f;
    public final float approachDividerX = 0.500f;
    public final float giveWayY = 0.682f;

    public final float splitterTipX = 0.500f;
    public final float splitterTipY = 0.704f;
    public final float splitterBottomY = 0.930f;
    public final float splitterHalfWidth = 0.027f;

    public final float[] exitAnglesDeg = {180f, 270f, 0f, 90f};

    public float approachCentreForExit(int exit) {
        return exit <= 2 ? approachLeftCentre : approachRightCentre;
    }

    public boolean validExit(int exit) { return exit >= 1 && exit <= 4; }
}
