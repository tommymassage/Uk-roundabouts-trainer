package uk.co.roundaboutstrainer;

/** Lightweight safety checks for editable roundabout geometry before rendering. */
public final class GeometryValidator {
    private GeometryValidator() {}

    public static String validate(RoadGeometrySpec g) {
        if (g == null) return "Geometry missing";
        if (g.outerRadius <= g.innerRadius) return "Outer radius must be larger than inner radius";
        if (g.outerRadius <= 0f || g.outerRadius >= 0.5f) return "Outer radius out of bounds";
        if (g.innerRadius <= 0f) return "Inner radius out of bounds";
        if (g.giveWayY <= 0f || g.giveWayY >= 1f) return "Give-way position out of bounds";
        if (g.approachLeftCentre >= g.approachRightCentre) return "Approach lane centres overlap";
        if (g.splitterHalfWidth <= 0f) return "Splitter island width invalid";
        return "OK";
    }

    public static boolean isValid(RoadGeometrySpec g) {
        return "OK".equals(validate(g));
    }
}
