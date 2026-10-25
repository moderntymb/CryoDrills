package example;

import arc.Core;

import mindustry.content.Liquids;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.consumers.ConsumeLiquidBase;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.Stats;

/**
 * Liquid booster для CryoDrills.
 *
 * Water:
 * - стандартна витрата;
 * - стандартне прискорення бура.
 *
 * Cryofluid:
 * - 2x витрата відносно води;
 * - 2x кінцева швидкість буріння
 *   відносно водяного прискорення.
 */
public class CryoLiquidBoost
    extends ConsumeLiquidBase {

    public static final float CRYO_CONSUMPTION = 2f;

    public static final float EPSILON = 0.001f;

    public CryoLiquidBoost(float waterAmount) {
        super(waterAmount);
        boost();
    }

    @Override
    public void apply(Block block) {
        super.apply(block);

        /*
         * Дозволяємо буру приймати
         * Water та Cryofluid.
         */
        block.liquidFilter[Liquids.water.id] = true;

        block.liquidFilter[
            Liquids.cryofluid.id
        ] = true;
    }

    @Override
    public boolean consumes(Liquid liquid) {

        return liquid == Liquids.water
            || liquid == Liquids.cryofluid;
    }

    /**
     * Визначає активну рідину.
     *
     * Cryofluid має пріоритет лише як
     * страховка на випадок, якщо обидві
     * рідини якимось способом опиняться
     * всередині одночасно.
     *
     * Нормальна логіка CryoDrillLogic
     * змішування не дозволяє.
     */
    public Liquid selected(Building build) {

        if (
            build.liquids.get(
                Liquids.cryofluid
            ) > EPSILON
        ) {
            return Liquids.cryofluid;
        }

        if (
            build.liquids.get(
                Liquids.water
            ) > EPSILON
        ) {
            return Liquids.water;
        }

        return null;
    }

    public boolean usingCryofluid(
        Building build
    ) {

        return selected(build)
            == Liquids.cryofluid;
    }

    /**
     * Water     = 1x consumption.
     * Cryofluid = 2x consumption.
     */
    public float consumptionMultiplier(
        Liquid liquid
    ) {

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
            build.edelta()
            * build.efficiencyScale();

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
            build.liquids.get(liquid)
                / required,
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

        build.liquids.remove(
            liquid,
            required
        );
    }

    @Override
    public void display(Stats stats) {

        /*
         * WATER
         *
         * Показуємо реальну ванільну
         * витрату води для конкретного бура.
         */
        stats.add(
            Stat.booster,
            Liquids.water,
            amount * 60f,
            true
        );

        /*
         * CRYOFLUID
         *
         * Реальна витрата:
         * 2x від Water.
         */
        stats.add(
            Stat.booster,
            Liquids.cryofluid,
            amount
                * CRYO_CONSUMPTION
                * 60f,
            true
        );

        /*
         * Додатково показуємо різницю
         * між двома режимами охолодження.
         *
         * Water:
         * стандартний liquid boost.
         *
         * Cryofluid:
         * 200% швидкості Water.
         */
        stats.add(
            Stat.booster,
            Core.bundle.get(
                "liquid.water"
            )
                + ": 100% | "
                + Core.bundle.get(
                    "liquid.cryofluid"
                )
                + ": 200%"
        );
    }
}
