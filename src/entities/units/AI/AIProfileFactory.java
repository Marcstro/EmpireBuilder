package entities.units.AI;

import entities.units.AI.nodes.*;

public class AIProfileFactory {

    private static Node createDefenderAI(boolean isMelee) {
        Node combatNode = isMelee ? new EngagedEnemyMeleeNode() : new EngagedEnemyRangedNode();
        return new ReactiveSelector(
                // Tier 1: Combat emergencies (always checked, highest priority)
                new SearchForEnemyNode(),
                combatNode,
                // Tier 2: Committed work (remembers choice, re-evals every 60 ticks)
                new StatefulSelector(60,
                    new ClearInvestigatedAreaNode(),
                    new WalkTowardsPointTargetNode(),
                    new IdleNode()
                )
        );
    }

    public static Node createMeleeDefenderAI() {
        return createDefenderAI(true);
    }

    public static Node createRangedDefenderAI() {
        return createDefenderAI(false);
    }

    private static Node createAttackerAI(boolean isMelee) {
        Node combatNode = isMelee ? new EngagedEnemyMeleeNode() : new EngagedEnemyRangedNode();
        return new ReactiveSelector(
                new DepositLootNode(),
                new ReturnLootNode(),
                new SearchForEnemyNode(),
                combatNode,
                new StatefulSelector(60,
                    new RaidAreaNode(),
                    new WalkTowardsPointTargetNode(),
                    new IdleNode()
                )
        );
    }

    public static Node createMeleeAttackerAI() {
        return createAttackerAI(true);
    }

    public static Node createRangedAttackerAI() {
        return createAttackerAI(false);
    }

    public static Node createDestructiveAttackerAI(){
        return new ReactiveSelector(
                new SearchForEnemyNode(),
                new EngagedEnemyMeleeNode(),
                new StatefulSelector(60,
                    new WalkTowardsPointTargetNode(),
                    new IdleNode()
                )
        );
    }

    public static Node justIdleAI(){
        return new IdleNode();
    }
}
