package uk.co.roundaboutstrainer;

/** Switchable scene layers. Keeps visual options separate from road rules and traffic logic. */
public final class VisualLayerConfig {
    public boolean showRoute = true;
    public boolean showLaneGuide = true;
    public boolean showUkMarkings = true;
    public boolean showArrows = true;
    public boolean showSigns = true;
    public boolean showTrainingHints = true;
    public boolean showOtherTraffic = false; // intentionally disabled until traffic simulation phase

    public static VisualLayerConfig preTrafficDefaults() {
        return new VisualLayerConfig();
    }
}
