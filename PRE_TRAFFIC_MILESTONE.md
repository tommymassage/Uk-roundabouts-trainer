# Pre-traffic milestone

This milestone freezes the architecture immediately before other-road-user traffic simulation is introduced.

## Completed foundations
- Editable UK roundabout geometry independent from visual background.
- UK rule model independent from renderer.
- Eight learner scenarios: 2 Lane + Spiral, exits 1-4.
- Route specification model for every scenario.
- Trainer session phases: ready, approach, entering, circulating, exiting, complete.
- Switchable visual layers with other traffic deliberately disabled.
- Geometry validation checks.
- Training display model and UK accuracy checklist.

## Still intentionally deferred
- Moving traffic vehicles.
- Gap acceptance / give-way decision engine.
- Collision/conflict zones.
- Traffic density and spawning.
- Priority interaction with other road users.

## Accuracy policy
Road markings, arrows, lane paths, splitter islands, signalling points and vehicle paths remain editable. A dedicated UK accuracy pass will tune them against correct road layouts and regulations before public release.
