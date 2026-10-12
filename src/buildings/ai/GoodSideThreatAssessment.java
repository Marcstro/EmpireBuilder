package buildings.ai;

import java.util.EnumSet;
import java.util.EnumMap;
import java.util.Map;

/** A good-side building's current read on its situation: active ThreatFacts, a 0-100 meter, and the band that falls into. */
// currently not used, consider development or removal
public class GoodSideThreatAssessment {

    public static final double MAX_METER = 100.0;

    private static final double RISE_RATE = MAX_METER;
    private static final double DECAY_GAP_FACTOR = 0.94;
    private static final double DECAY_SNAP_THRESHOLD = 0.5;

    private final EnumSet<ThreatFact> activeFacts = EnumSet.noneOf(ThreatFact.class);
    private final Map<ThreatFact, Integer> cooldowns = new EnumMap<>(ThreatFact.class);
    private double meter;
    private GoodSideBuildingStates band = GoodSideBuildingStates.SAFE;

    private int evaluationsInBand;

    public void setFact(ThreatFact fact, boolean active) {
        if (active) {
            activeFacts.add(fact);
            cooldowns.put(fact, fact.getCooldownTicks());
        }
    }

    public void tick() {
        for (ThreatFact fact : EnumSet.copyOf(activeFacts)) {
            int remaining = cooldowns.getOrDefault(fact, fact.getCooldownTicks()) - 1;
            if (remaining <= 0) {
                activeFacts.remove(fact);
                cooldowns.remove(fact);
            } else {
                cooldowns.put(fact, remaining);
            }
        }
    }

    public boolean has(ThreatFact fact) {
        return activeFacts.contains(fact);
    }

    public EnumSet<ThreatFact> getActiveFacts() {
        return activeFacts;
    }

    public GoodSideBuildingStates update() {
        double target = 0;
        for (ThreatFact fact : activeFacts) {
            target += fact.getWeight();
        }
        target = Math.min(target, MAX_METER);

        double delta = target - meter;
        if (delta >= 0) {
            meter = Math.min(MAX_METER, meter + Math.min(delta, RISE_RATE));
        } else {
            double gap = meter - target;
            gap *= DECAY_GAP_FACTOR;
            meter = gap <= DECAY_SNAP_THRESHOLD ? target : target + gap;
        }
        meter = Math.max(0, Math.min(MAX_METER, meter));

        GoodSideBuildingStates newBand = GoodSideBuildingStates.fromMeter(meter);
        if (newBand == band) {
            evaluationsInBand++;
        } else {
            evaluationsInBand = 0;
        }
        band = newBand;
        return band;
    }

    public double getMeter() {
        return meter;
    }

    public GoodSideBuildingStates getBand() {
        return band;
    }

    public int getEvaluationsInBand() {
        return evaluationsInBand;
    }

    public boolean isBandSettled(int requiredEvaluations) {
        return evaluationsInBand >= requiredEvaluations;
    }

    public double getMilitaryShare() {
        return band.getMilitaryShare();
    }

    public double getEconomyShare() {
        return band.getEconomyShare();
    }

    public String getInfo() {
        return "{threat=" + String.format("%.1f", meter) + "/" + (int) MAX_METER +
                ", band=" + band +
                ", facts=" + activeFacts + "}";
    }
}
