package example;

import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import arc.util.Strings;

import mindustry.content.Liquids;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.blocks.production.Drill;
import mindustry.world.consumers.ConsumeLiquidBase;
import mindustry.world.meta.Stat;
import mindustry.world.meta.Stats;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValue;

public class CryoLiquidBoost
    extends ConsumeLiquidBase {

    public static final float CRYO_CONSUMPTION = 2f;
    public static final float CRYO_SPEED_MULTIPLIER = 2f;
    public static final float EPSILON = 0.001f;

    private final Drill drill;

    public CryoLiquidBoost(
        Drill drill,
        float waterAmount
    ) {
        super(waterAmount);

        this.drill = drill;

        /*
         * Має залишатися optional consumer,
         * але НЕ vanilla booster.
         *
         * Інакше Drill.setStats() замінить
         * нашу власну статистику.
         */
        optional(true, false);
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

        float waterPerSecond =
            amount * 60f;

        float cryoPerSecond =
            waterPerSecond
            * CRYO_CONSUMPTION;

        /*
         * Vanilla Drill показує фінальний бонус
         * як liquidBoostIntensity².
         *
         * Тому він автоматично відрізняється
         * для різних бурів.
         */
        float waterSpeed =
            drill.liquidBoostIntensity
            * drill.liquidBoostIntensity;

        /*
         * Наш Cryofluid дає рівно 2x
         * фінальної швидкості Water.
         */
        float cryoSpeed =
            waterSpeed
            * CRYO_SPEED_MULTIPLIER;

        stats.add(
            Stat.booster,
            (StatValue)table -> {

                table.row();

                addBooster(
                    table,
                    Liquids.water,
                    waterPerSecond,
                    waterSpeed
                );

                addBooster(
                    table,
                    Liquids.cryofluid,
                    cryoPerSecond,
                    cryoSpeed
                );
            }
        );
    }

    private static void addBooster(
        Table table,
        Liquid liquid,
        float perSecond,
        float speed
    ) {

        table.table(
            Styles.grayPanel,
            panel -> {

                /*
                 * ICON
                 */
                panel.image(liquid.uiIcon)
                    .size(40f)
                    .pad(10f);

                /*
                 * NAME + CONSUMPTION
                 */
                panel.table(info -> {

                    info.add(
                        liquid.localizedName
                    )
                    .left();

                    info.row();

                    info.add(
                        Strings.autoFixed(
                            perSecond,
                            2
                        )
                        + StatUnit
                            .perSecond
                            .localized()
                    )
                    .left()
                    .color(Color.lightGray);
                });

                /*
                 * SPEED
                 */
                panel.table(speedTable -> {

                    speedTable.right();

                    speedTable.add(
                        "[stat]"
                        + Strings.autoFixed(
                            speed,
                            2
                        )
                        + "[lightgray]"
                        + StatUnit
                            .timesSpeed
                            .localized()
                    );

                })
                .right()
                .grow()
                .pad(10f)
                .padRight(15f);
            }
        )
        .growX()
        .pad(5f)
        .row();
    }
}
