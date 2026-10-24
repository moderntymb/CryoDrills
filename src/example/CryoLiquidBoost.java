package example;

import mindustry.content.Liquids;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.consumers.ConsumeLiquidBase;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;

/**
 * Один liquid booster, який підтримує воду та кріоген.
 *
 * Water:
 * - ванільна витрата;
 * - ванільний boost.
 *
 * Cryofluid:
 * - 2x витрата;
 * - фактичний speed boost визначається CryoDrillLogic.
 */
public class CryoLiquidBoost extends ConsumeLiquidBase {

    public static final float CRYO_CONSUMPTION = 2f;
    public static final float EPSILON = 0.001f;

    public CryoLiquidBoost(float waterAmount) {
        super(waterAmount);
        boost();
    }

    @Override
    public void apply(Block block) {
        super.apply(block);

        // Дозволяємо обидві рідини.
        block.liquidFilter[Liquids.water.id] = true;
        block.liquidFilter[Liquids.cryofluid.id] = true;
    }

    @Override
    public boolean consumes(Liquid liquid) {
        return liquid == Liquids.water
            || liquid == Liquids.cryofluid;
    }

    /**
     * Визначає активну охолоджувальну рідину.
     *
     * Змішування поки не є механікою першої версії.
     * Якщо якимось способом обидві рідини вже опинились
     * усередині, кріоген має пріоритет.
     */
    public Liquid selected(Building build) {
        if (build.liquids.get(Liquids.cryofluid) > EPSILON) {
            return Liquids.cryofluid;
        }

        if (build.liquids.get(Liquids.water) > EPSILON) {
            return Liquids.water;
        }

        return null;
    }

    public boolean usingCryofluid(Building build) {
        return selected(build) == Liquids.cryofluid;
    }

    public float consumptionMultiplier(Liquid liquid) {
        return liquid == Liquids.cryofluid
            ? CRYO_CONSUMPTION
            : 1f;
    }

    @Override
    public float efficiency(Building build) {
        Liquid liquid = selected(build);

        if (liquid == null) {
            return 0f;
        }

        float edelta =
            build.edelta() * build.efficiencyScale();

        if (edelta <= 0.00000001f) {
            return 0f;
        }

        float required =
            amount
            * consumptionMultiplier(liquid)
            * edelta
            * multiplier.get(build);

        if (required <= 0.00000001f) {
            return 0f;
        }

        return Math.min(
            build.liquids.get(liquid) / required,
            1f
        );
    }

    @Override
    public void update(Building build) {
        Liquid liquid = selected(build);

        if (liquid == null) {
            return;
        }

        float required =
            amount
            * consumptionMultiplier(liquid)
            * build.edelta()
            * multiplier.get(build);

        build.liquids.remove(liquid, required);
    }

    @Override
    public void display(Stats stats) {
        // Water = vanilla consumption.
        stats.add(
            Stat.booster,
            Liquids.water,
            amount * 60f,
            true
        );

        // Cryofluid = 2x water consumption.
        stats.add(
            Stat.booster,
            Liquids.cryofluid,
            amount * CRYO_CONSUMPTION * 60f,
            true
        );
    }
            }
