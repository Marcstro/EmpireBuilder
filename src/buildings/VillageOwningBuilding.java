package buildings;

import empirebuilder.Point;

import java.awt.*;
import java.util.LinkedList;

public abstract class VillageOwningBuilding extends FarmOwningBuilding{

    private static final int INCOME_HISTORY_LENGTH = 100;
    private final double[] incomeHistory = new double[INCOME_HISTORY_LENGTH];
    private int currentIncomeIndex;
    private int recordedIncomeTicks;
    private double cachedWealth;
    private boolean wealthDirty;
    LinkedList<Village> villages;

    @Override
    public synchronized void tick() {
        if (recordedIncomeTicks > 0) {
            currentIncomeIndex = (currentIncomeIndex + 1) % INCOME_HISTORY_LENGTH;
            incomeHistory[currentIncomeIndex] = 0;
        }
        if (recordedIncomeTicks < INCOME_HISTORY_LENGTH) {
            recordedIncomeTicks++;
        }
        wealthDirty = true;
        super.tick();
    }

    @Override
    public synchronized void addGold(double gold) {
        super.addGold(gold);
        if (gold > 0) {
            // The current bucket stays open for sales after this building's tick.
            incomeHistory[currentIncomeIndex] += gold;
            wealthDirty = true;
        }
    }

    /** Average gross income over up to 100 building ticks, calculated only when inspected. */
    @Override
    public synchronized double getWealth() {
        if (wealthDirty) {
            double totalIncome = 0;
            for (int i = 0; i < recordedIncomeTicks; i++) {
                totalIncome += incomeHistory[i];
            }
            cachedWealth = recordedIncomeTicks == 0 ? 0 : totalIncome / recordedIncomeTicks;
            wealthDirty = false;
        }
        return cachedWealth;
    }

    protected void inheritIncomeHistory(VillageOwningBuilding previousOwner) {
        synchronized (previousOwner) {
            System.arraycopy(previousOwner.incomeHistory, 0, incomeHistory, 0, INCOME_HISTORY_LENGTH);
            currentIncomeIndex = previousOwner.currentIncomeIndex;
            recordedIncomeTicks = previousOwner.recordedIncomeTicks;
            cachedWealth = previousOwner.cachedWealth;
            wealthDirty = previousOwner.wealthDirty;
        }
    }

    @Override
    public void setOwner(Building owner) {
        super.setOwner(owner);
        for (Village village : villages) {
            village.updateOwnedFarmsTechLevel();
        }
    }

    public VillageOwningBuilding(Point point, int foodNeededToCreateNewFarm, Color color, double health) {
        super(point, foodNeededToCreateNewFarm, color, health);
        villages = new LinkedList<>();
    }

    public void addVillage(Village village){
        villages.add(village);
    }

    public LinkedList<Village> getVillages() {
        return villages;
    }

    public void removeVillage(Village village){
        villages.remove(village);
    }

    public void setVillages(LinkedList<Village> villages) {
        this.villages = villages;
    }
}
