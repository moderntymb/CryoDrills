package example;

import arc.scene.ui.layout.Table;

import mindustry.content.Liquids;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.consumers.ConsumeLiquidBase;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;
import mindustry.world.meta.StatValue;

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

        block.liquidFilter[
            Liquids.water.id
        ] = true;

        block.liquidFilter[
            Liquids.cryofluid.id
        ] = true;
    }

    @Override
    public boolean consumes(Liquid liquid) {
        return liquid == Liquids.water
            || liquid == Liquids.cryofluid;
    }

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

        final float waterPerSecond =
            amount * 60f;

        final float cryoPerSecond =
            amount
            * CRYO_CONSUMPTION
            * 60f;

        stats.add(
            Stat.booster,
            (StatValue)table -> {

                /*
                 * WATER
                 */
                addLiquidInfo(
                    table,
                    Liquids.water,
                    waterPerSecond,
                    "1x speed"
                );

                table.row();

                /*
                 * CRYOFLUID
                 */
                addLiquidInfo(
                    table,
                    Liquids.cryofluid,
                    cryoPerSecond,
                    "2x water speed"
                );
            }
        );
    }

    private static void addLiquidInfo(
        Table table,
        Liquid liquid,
        float perSecond,
        String description
    ) {

        /*
         * Іконка рідини.
         */
        table.image(liquid.uiIcon)
            .size(32f)
            .padRight(8f);

        /*
         * Назва рідини.
         */
        table.table(info -> {

            info.left();

            info.add(
                liquid.localizedName
            ).left();

            info.row();

            /*
             * Витрата + опис режиму.
             */
            info.add(
                formatAmount(perSecond)
                    + "/sec"
                    + " | "
                    + description
            )
            .left()
            .color(
                arc.graphics.Color.lightGray
            );
        })
        .left()
        .padBottom(4f);
    }

    private static String formatAmount(
        float value
    ) {

        /*
         * 7.0 -> "7"
         * 3.5 -> "3.5"
         * 4.8 -> "4.8"
         */
        if (
            Math.abs(
                value - Math.round(value)
            ) < 0.001f
        ) {
            return Integer.toString(
                Math.round(value)
            );
        }

        return String.format(
            java.util.Locale.ROOT,
            "%.1f",
            value
        );
    }
}
