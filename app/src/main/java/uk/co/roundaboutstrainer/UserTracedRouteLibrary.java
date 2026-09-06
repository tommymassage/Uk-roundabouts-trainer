package uk.co.roundaboutstrainer;

/**
 * Normalised route centre-lines traced by the user on the approved 2-lane roundabout view.
 * Coordinates are 0..1 across the full simulator canvas.
 * These are the source-of-truth routes for the 6 o'clock approach.
 */
public final class UserTracedRouteLibrary {
    private UserTracedRouteLibrary() {}

    // 1st exit: 6 -> 9 o'clock. User trace, smoothed by renderer only.
    private static final float[][] FIRST_EXIT = {
            {0.339f,0.870f},{0.350f,0.809f},{0.350f,0.748f},{0.342f,0.688f},
            {0.335f,0.627f},{0.318f,0.566f},{0.284f,0.504f},{0.255f,0.464f},
            {0.209f,0.424f},{0.163f,0.395f},{0.117f,0.376f},{0.072f,0.358f},{0.029f,0.346f}
    };

    // 2nd exit: 6 -> 12 o'clock. User trace, smoothed by renderer only.
    private static final float[][] SECOND_EXIT = {
            {0.343f,0.867f},{0.352f,0.809f},{0.358f,0.749f},{0.356f,0.687f},
            {0.340f,0.627f},{0.315f,0.567f},{0.283f,0.504f},{0.252f,0.444f},
            {0.232f,0.384f},{0.219f,0.323f},{0.235f,0.261f},{0.314f,0.194f},
            {0.413f,0.142f},{0.453f,0.081f},{0.471f,0.020f}
    };

    public static float[][] pointsForExit(int exit) {
        if (exit == 1) return copy(FIRST_EXIT);
        if (exit == 2) return copy(SECOND_EXIT);
        return new float[0][0];
    }

    public static boolean hasUserTrace(int exit) {
        return exit == 1 || exit == 2;
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
