package uk.co.roundaboutstrainer;

/** Current learner session. No external traffic is modelled here yet. */
public final class TrainerSession {
    public enum Phase { READY, APPROACH, ENTERING, CIRCULATING, EXITING, COMPLETE }

    public TrainingScenario scenario;
    public RouteSpec route;
    public Phase phase = Phase.READY;
    public float progress = 0f;

    public TrainerSession(boolean spiral, int exit) {
        setScenario(spiral, exit);
    }

    public void setScenario(boolean spiral, int exit) {
        scenario = TrainingScenario.create(spiral, exit);
        route = RouteSpec.forScenario(spiral, exit);
        reset();
    }

    public void reset() {
        progress = 0f;
        phase = Phase.READY;
    }

    public void update(float value) {
        progress = Math.max(0f, Math.min(1f, value));
        if (progress <= 0f) phase = Phase.READY;
        else if (progress < 0.24f) phase = Phase.APPROACH;
        else if (progress < 0.38f) phase = Phase.ENTERING;
        else if (progress < 0.78f) phase = Phase.CIRCULATING;
        else if (progress < 1f) phase = Phase.EXITING;
        else phase = Phase.COMPLETE;
    }
}
