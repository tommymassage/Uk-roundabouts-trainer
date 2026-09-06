package uk.co.roundaboutstrainer;

/** Read-only labels for the learner panel. Keeps UI text separate from drawing code. */
public final class TrainingDisplayModel {
    public final String title;
    public final String lane;
    public final String approachSignal;
    public final String objective;
    public final String hint;

    private TrainingDisplayModel(TrainingScenario scenario) {
        this.title = scenario.title();
        this.lane = scenario.laneLabel();
        this.approachSignal = scenario.signalLabel();
        this.objective = scenario.objective;
        this.hint = scenario.hint;
    }

    public static TrainingDisplayModel from(TrainingScenario scenario) {
        if (scenario == null) throw new IllegalArgumentException("scenario == null");
        return new TrainingDisplayModel(scenario);
    }

    public String summary() {
        return lane + " • " + approachSignal;
    }
}