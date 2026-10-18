package buildings.ai;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoodSideThreatAssessmentTest {
    @Test
    void deploymentRequiresAnyFactOtherThanScoutDetectionForEveryCombination() {
        ThreatFact[] facts = ThreatFact.values();
        for (int mask = 0; mask < (1 << facts.length); mask++) {
            GoodSideThreatAssessment assessment = new GoodSideThreatAssessment();
            EnumSet<ThreatFact> active = EnumSet.noneOf(ThreatFact.class);
            for (int i = 0; i < facts.length; i++) {
                if ((mask & (1 << i)) != 0) {
                    active.add(facts[i]);
                    assessment.setFact(facts[i], true);
                }
            }
            EnumSet<ThreatFact> deploymentThreats = EnumSet.copyOf(active);
            deploymentThreats.remove(ThreatFact.ENEMY_SCOUT_DETECTED);

            assertEquals(!deploymentThreats.isEmpty(), assessment.shouldDeployGarrisonedUnits(), active.toString());
            assertEquals(active, assessment.getActiveFacts());
        }
    }

    @Test
    void deploymentStopsWhenOnlyScoutDetectionRemainsAfterCooldown() {
        GoodSideThreatAssessment assessment = new GoodSideThreatAssessment();
        assessment.setFact(ThreatFact.ENEMY_SCOUT_DETECTED, true);
        assessment.setFact(ThreatFact.OWNED_AREA_RAIDED, true);
        for (int i = 0; i < ThreatFact.OWNED_AREA_RAIDED.getCooldownTicks() - 1; i++) {
            assessment.tick();
        }
        assertTrue(assessment.shouldDeployGarrisonedUnits());

        assessment.tick();

        assertTrue(assessment.has(ThreatFact.ENEMY_SCOUT_DETECTED));
        assertFalse(assessment.shouldDeployGarrisonedUnits());
    }
}
