package empirebuilder;

import buildings.Village;
import entities.units.Alpaca;
import entities.units.Capybara;
import entities.units.Donkey;
import entities.units.ElfArcher;
import entities.units.FarmerMilitia;
import entities.units.Goblin;
import entities.units.HumanScout;
import entities.units.Knight;
import entities.units.MountedKnight;
import entities.units.TradingCart;
import entities.units.Unit;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnitDeathLootTest {
    @Test
    void goodUnitTypesHaveDistinctPositiveValuesAndSpecifiedCombatValues() {
        assertEquals(500, new Knight(0, 0).getValue());
        assertEquals(600, new ElfArcher(0, 0).getValue());
        assertEquals(2500, new MountedKnight(0, 0).getValue());
        List<Unit> units = List.of(new Knight(0, 0), new ElfArcher(0, 0),
                new MountedKnight(0, 0), new FarmerMilitia(0, 0), new HumanScout(0, 0),
                new TradingCart(0, 0), new Alpaca(0, 0), new Donkey(0, 0), new Capybara(0, 0));
        HashSet<Integer> values = new HashSet<>();
        for (Unit unit : units) {
            assertEquals(1, unit.getFactionId());
            assertTrue(unit.getValue() > 0);
            assertTrue(values.add(unit.getValue()), unit.getClass().getSimpleName());
        }
    }

    @Test
    void deathRewardsOnlyLivingNearbyEvilUnitsOnceInAllDirections() {
        Game game = world();
        MountedKnight fallen = new MountedKnight(10, 10);
        add(game, fallen);
        List<Goblin> recipients = new ArrayList<>();
        for (int x : new int[]{5, 10, 15}) {
            for (int y : new int[]{5, 10, 15}) {
                Goblin goblin = goblin(game, x, y);
                goblin.addLoot(10);
                recipients.add(goblin);
            }
        }
        Goblin dead = goblin(game, 10, 10);
        dead.setIsAlive(false);
        Goblin friendly = goblin(game, 10, 10);
        friendly.setFactionId(1);
        Goblin neutral = goblin(game, 10, 10);
        neutral.setFactionId(3);
        Goblin distant = goblin(game, 20, 10);
        fallen.causeHealthLoss(fallen.getHealth());

        game.tickUnits();

        for (Goblin recipient : recipients) {
            assertEquals(10 + 2500 / 9, recipient.getLoot());
        }
        assertEquals(0, dead.getLoot());
        assertEquals(0, friendly.getLoot());
        assertEquals(0, neutral.getLoot());
        assertEquals(0, distant.getLoot());
        assertFalse(game.getUnits().contains(fallen));
        assertFalse(game.getMapCellByPoint(game.getPoint(10, 10)).getUnits().contains(fallen));

        game.tickUnits();

        for (Goblin recipient : recipients) {
            assertEquals(10 + 2500 / 9, recipient.getLoot());
        }
    }

    @Test
    void deathsOutsideGoodFactionDoNotPayRewardsEvenWithNonzeroValue() {
        for (int faction : new int[]{2, 3}) {
            Game game = world();
            Knight fallen = new Knight(10, 10);
            fallen.setFactionId(faction);
            add(game, fallen);
            Goblin recipient = goblin(game, 10, 10);
            fallen.setIsAlive(false);

            game.tickUnits();

            assertEquals(0, recipient.getLoot());
        }
    }

    @Test
    void deathWithNoEligibleRecipientsStillRemovesUnit() {
        Game game = world();
        Knight fallen = new Knight(10, 10);
        add(game, fallen);
        fallen.setIsAlive(false);

        game.tickUnits();

        assertTrue(game.getUnits().isEmpty());
    }

    @Test
    void deathsAtMapEdgesRewardNearbyUnitsWithoutOutOfBoundsAccess() {
        for (int coordinate : new int[]{0, 29}) {
            Game game = world();
            Knight fallen = new Knight(coordinate, coordinate);
            add(game, fallen);
            Goblin recipient = goblin(game, coordinate, coordinate);
            fallen.setIsAlive(false);

            game.tickUnits();

            assertEquals(500, recipient.getLoot());
        }
    }

    @Test
    void despawningDoesNotAwardDeathLoot() {
        Game game = world();
        Knight unit = new Knight(10, 10);
        add(game, unit);
        Goblin recipient = goblin(game, 10, 10);

        game.despawnUnit(unit);
        game.tickUnits();

        assertEquals(0, recipient.getLoot());
    }

    @Test
    void buildingLootKeepsItsExistingEqualIntegerShares() {
        Game game = world();
        Village village = new Village(game.getPoint(10, 10));
        village.setGold(501.9);
        Goblin first = goblin(game, 10, 10);
        Goblin second = goblin(game, 10, 10);

        game.divideLootFromBuilding(village);

        assertEquals(250, first.getLoot());
        assertEquals(250, second.getLoot());
    }

    private Game world() {
        return new GameManager(30, 30, new WorldSettings()).getGame();
    }

    private void add(Game game, Unit unit) {
        game.addNewUnit(unit, (int) unit.getX(), (int) unit.getY());
    }

    private Goblin goblin(Game game, int x, int y) {
        Goblin unit = new Goblin(x, y) {
            @Override
            public void tick(Game game) {
            }
        };
        add(game, unit);
        return unit;
    }
}
