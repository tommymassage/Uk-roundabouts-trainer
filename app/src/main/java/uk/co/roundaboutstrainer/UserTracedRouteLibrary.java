package uk.co.roundaboutstrainer;

/**
 * Normalised centre-lines extracted from the user's approved red traces.
 * Source of truth: user's 6 o'clock approach drawings.
 * Unmarked-roundabout rule: exits at/before 12 use left approach + outer arc;
 * exits after 12 use right approach + inner arc, moving outward before exit.
 */
public final class UserTracedRouteLibrary {
    private UserTracedRouteLibrary() {}

    private static final float[][] FIRST_EXIT = {
        {0.3366f,0.8932f},{0.3405f,0.8498f},{0.3503f,0.8064f},{0.3509f,0.7630f},
        {0.3457f,0.7196f},{0.3411f,0.6753f},{0.3359f,0.6319f},{0.3268f,0.5885f},
        {0.3086f,0.5451f},{0.2819f,0.5009f},{0.2507f,0.4592f},{0.2181f,0.4297f},
        {0.1855f,0.4089f},{0.1523f,0.3889f},{0.1198f,0.3759f},{0.0872f,0.3637f},
        {0.0547f,0.3533f},{0.0215f,0.3438f}
    };

    private static final float[][] SECOND_EXIT = {
        {0.3411f,0.8785f},{0.3503f,0.8203f},{0.3581f,0.7613f},{0.3574f,0.7031f},
        {0.3438f,0.6441f},{0.3255f,0.5859f},{0.2956f,0.5269f},{0.2624f,0.4679f},
        {0.2389f,0.4097f},{0.2227f,0.3507f},{0.2233f,0.2925f},{0.2513f,0.2370f},
        {0.2956f,0.2049f},{0.3392f,0.1884f},{0.3835f,0.1727f},{0.4258f,0.1250f},
        {0.4590f,0.0660f},{0.4772f,0.0069f}
    };

    private static final float[][] THIRD_EXIT = {
        {0.4427f,0.8620f},{0.4421f,0.7821f},{0.4453f,0.7014f},{0.4362f,0.6215f},
        {0.4069f,0.5408f},{0.3555f,0.4609f},{0.3268f,0.3811f},{0.3151f,0.3003f},
        {0.3561f,0.2240f},{0.4160f,0.1840f},{0.4766f,0.1675f},{0.5371f,0.1589f},
        {0.5970f,0.1727f},{0.6576f,0.2005f},{0.7181f,0.2214f},{0.7780f,0.2283f},
        {0.8385f,0.2309f},{0.8991f,0.2378f}
    };

    private static final float[][] FOURTH_EXIT = {
        {0.4609f,0.8715f},{0.4701f,0.7708f},{0.4557f,0.6693f},{0.4251f,0.5677f},
        {0.3841f,0.4670f},{0.3249f,0.3689f},{0.3184f,0.2674f},{0.3867f,0.2075f},
        {0.4629f,0.1840f},{0.5391f,0.1840f},{0.6152f,0.2057f},{0.6790f,0.2908f},
        {0.6842f,0.3924f},{0.6628f,0.4939f},{0.6445f,0.5946f},{0.6191f,0.6962f},
        {0.6191f,0.7977f},{0.6393f,0.7969f}
    };

    public static float[][] pointsForExit(int exit) {
        if (exit == 1) return copy(FIRST_EXIT);
        if (exit == 2) return copy(SECOND_EXIT);
        if (exit == 3) return copy(THIRD_EXIT);
        if (exit == 4) return copy(FOURTH_EXIT);
        return new float[0][0];
    }
    public static boolean hasUserTrace(int exit) { return exit >= 1 && exit <= 4; }
    public static boolean usesLeftApproachLane(int exit) { return exit <= 2; }
    public static boolean usesOuterArc(int exit) { return exit <= 2; }
    private static float[][] copy(float[][] src) {
        float[][] out=new float[src.length][2];
        for(int i=0;i<src.length;i++){ out[i][0]=src[i][0]; out[i][1]=src[i][1]; }
        return out;
    }
}
