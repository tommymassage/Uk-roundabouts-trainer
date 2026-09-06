package uk.co.roundaboutstrainer;

/**
 * Normalised route centre-lines traced by the user on the approved 2-lane roundabout view.
 * Coordinates are 0..1 across the full simulator canvas.
 * These are the source-of-truth routes for the 6 o'clock approach.
 *
 * Current unmarked-roundabout rule used by the simulator:
 * - exits at 12 o'clock or before: left approach lane + outer arc
 * - exits after 12 o'clock: right approach lane + inner arc, then move out for the exit
 */
public final class UserTracedRouteLibrary {
    private UserTracedRouteLibrary() {}

    // 1st exit: 6 -> 9 o'clock. LEFT approach lane, OUTER arc.
    private static final float[][] FIRST_EXIT = {
            {0.339f,0.870f},{0.350f,0.809f},{0.350f,0.748f},{0.342f,0.688f},
            {0.335f,0.627f},{0.318f,0.566f},{0.284f,0.504f},{0.255f,0.464f},
            {0.209f,0.424f},{0.163f,0.395f},{0.117f,0.376f},{0.072f,0.358f},{0.029f,0.346f}
    };

    // 2nd exit: 6 -> 12 o'clock. LEFT approach lane, OUTER arc.
    private static final float[][] SECOND_EXIT = {
            {0.343f,0.867f},{0.352f,0.809f},{0.358f,0.749f},{0.356f,0.687f},
            {0.340f,0.627f},{0.315f,0.567f},{0.283f,0.504f},{0.252f,0.444f},
            {0.232f,0.384f},{0.219f,0.323f},{0.235f,0.261f},{0.314f,0.194f},
            {0.413f,0.142f},{0.453f,0.081f},{0.471f,0.020f}
    };

    // 3rd exit: 6 -> 3 o'clock. RIGHT approach lane, INNER arc, then move out to exit.
    // Derived from the user's red trace and smoothed by the renderer.
    private static final float[][] THIRD_EXIT = {
            {0.442f,0.870f},{0.445f,0.790f},{0.444f,0.705f},{0.433f,0.615f},
            {0.393f,0.525f},{0.340f,0.440f},{0.317f,0.350f},{0.331f,0.270f},
            {0.390f,0.215f},{0.500f,0.180f},{0.610f,0.180f},{0.700f,0.200f},
            {0.770f,0.220f},{0.850f,0.235f},{0.920f,0.240f},{0.985f,0.245f}
    };

    // 4th exit / U-turn: 6 -> 6 o'clock. RIGHT approach lane, INNER arc,
    // then move out and leave on the southbound exit.
    private static final float[][] FOURTH_EXIT = {
            {0.462f,0.875f},{0.460f,0.790f},{0.455f,0.700f},{0.435f,0.610f},
            {0.395f,0.520f},{0.340f,0.440f},{0.310f,0.350f},{0.320f,0.270f},
            {0.390f,0.215f},{0.500f,0.180f},{0.610f,0.190f},{0.670f,0.230f},
            {0.690f,0.320f},{0.680f,0.420f},{0.650f,0.520f},{0.620f,0.610f},
            {0.595f,0.700f},{0.585f,0.790f},{0.590f,0.875f}
    };

    public static float[][] pointsForExit(int exit) {
        if (exit == 1) return copy(FIRST_EXIT);
        if (exit == 2) return copy(SECOND_EXIT);
        if (exit == 3) return copy(THIRD_EXIT);
        if (exit == 4) return copy(FOURTH_EXIT);
        return new float[0][0];
    }

    public static boolean hasUserTrace(int exit) {
        return exit >= 1 && exit <= 4;
    }

    public static boolean usesLeftApproachLane(int exit) {
        return exit <= 2;
    }

    public static boolean usesOuterArc(int exit) {
        return exit <= 2;
    }

    private static float[][] copy(float[][] src) {
        float[][] out = new float[src.length][2];
        for (int i=0;i<src.length;i++) {
            out[i][0] = src[i][0];
            out[i][1] = src[i][1];
        }
        return out;
    }
}
