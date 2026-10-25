package example;

import arc.math.Mathf;
import arc.util.Log;

import mindustry.content.Blocks;
import mindustry.content.Liquids;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.blocks.production.Drill;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeLiquidBase;

import java.util.Arrays;

public class CryoDrillLogic {

    /*
     * У Drill прискорення фактично впливає
     * на кінцеву швидкість приблизно як intensity^2.
     *
     * Тому sqrt(2) дає кріогену приблизно
     * 2x кінцевої швидкості води.
     */
    private static final float CRYO_INTENSITY_MULTIPLIER =
        Mathf.sqrt(2f);

    private static final float EPSILON =
        CryoLiquidBoost.EPSILON;

    public static void prepare() {

        // Mechanical Drill не чіпаємо.
        configure(Blocks.pneumaticDrill);
        configure(Blocks.laserDrill);
        configure(Blocks.blastDrill);

        // Impact Drill теж не чіпаємо.

        Log.info(
            "CryoDrills: all supported vanilla drills configured."
        );
    }

    private static void configure(Block block) {

        if (!(block instanceof Drill)) {
            return;
        }

        Drill drill = (Drill)block;

        ConsumeLiquidBase oldWaterBooster =
            findWaterBooster(drill);

        if (oldWaterBooster == null) {
            Log.err(
                "CryoDrills: water booster not found for @",
                drill.name
            );
            return;
        }

        float waterAmount = oldWaterBooster.amount;

        CryoLiquidBoost replacement =
            new CryoLiquidBoost(waterAmount);

        replacement.update = true;
        replacement.apply(drill);

        /*
         * Замінюємо vanilla water booster
         * нашим CryoLiquidBoost у всіх масивах
         * consumers блока.
         */
        drill.consumers = replace(
            drill.consumers,
            oldWaterBooster,
            replacement,
            true
        );

        drill.optionalConsumers = replace(
            drill.optionalConsumers,
            oldWaterBooster,
            replacement,
            true
        );

        drill.nonOptionalConsumers = replace(
            drill.nonOptionalConsumers,
            oldWaterBooster,
            replacement,
            false
        );

        drill.updateConsumers = replace(
            drill.updateConsumers,
            oldWaterBooster,
            replacement,
            true
        );

        drill.hasConsumers =
            drill.consumers.length > 0;

        /*
         * Встановлюємо нашу логіку DrillBuild.
         */
        installBuildType(drill);

        /*
         * Встановлюємо наш UI рідини.
         */
        CryoDrillUI.install(drill);

        /*
         * ВАЖЛИВО:
         *
         * Vanilla stats були створені ще до того,
         * як ми замінили water booster.
         *
         * Тому очищаємо старі характеристики
         * і змушуємо Mindustry побудувати їх заново.
         *
         * Після цього має викликатися display()
         * нашого CryoLiquidBoost.
         */
        drill.stats = new mindustry.world.meta.Stats();
drill.setStats();

        Log.info(
            "CryoDrills: configured @ | water=@/s | cryofluid=@/s",
            drill.name,
            waterAmount * 60f,
            waterAmount
                * CryoLiquidBoost.CRYO_CONSUMPTION
                * 60f
        );
    }

    private static ConsumeLiquidBase findWaterBooster(
        Drill drill
    ) {

        for (Consume consumer : drill.consumers) {

            if (!(consumer instanceof ConsumeLiquidBase)) {
                continue;
            }

            ConsumeLiquidBase liquid =
                (ConsumeLiquidBase)consumer;

            /*
             * Шукаємо саме WATER BOOSTER.
             */
            if (
                consumer.booster
                && liquid.consumes(Liquids.water)
            ) {
                return liquid;
            }
        }

        return null;
    }

    private static void installBuildType(
        final Drill target
    ) {

        target.buildType = () ->
            target.new DrillBuild() {

                private boolean hasWater() {
                    return liquids.get(Liquids.water)
                        > EPSILON;
                }

                private boolean hasCryofluid() {
                    return liquids.get(Liquids.cryofluid)
                        > EPSILON;
                }

                /*
                 * Забороняємо змішування
                 * води та кріогену.
                 */
                @Override
                public boolean acceptLiquid(
                    Building source,
                    Liquid liquid
                ) {

                    if (liquid == Liquids.water) {

                        if (hasCryofluid()) {
                            return false;
                        }

                    } else if (
                        liquid == Liquids.cryofluid
                    ) {

                        if (hasWater()) {
                            return false;
                        }
                    }

                    return super.acceptLiquid(
                        source,
                        liquid
                    );
                }

                @Override
                public void updateTile() {

                    /*
                     * timer(0) поки залишаємо.
                     *
                     * Ця механіка вже перевірена
                     * в грі, тому зараз її не чіпаємо.
                     */
                    if (
                        timer(
                            0,
                            target.dumpTime / timeScale
                        )
                    ) {
                        dump(
                            dominantItem != null
                            && items.has(dominantItem)
                                ? dominantItem
                                : null
                        );
                    }

                    if (dominantItem == null) {
                        return;
                    }

                    timeDrilled +=
                        warmup * delta();

                    float delay =
                        target.getDrillTime(
                            dominantItem
                        );

                    if (
                        items.total()
                            < target.itemCapacity
                        && dominantItems > 0
                        && efficiency > 0
                    ) {

                        /*
                         * WATER:
                         * стандартний liquidBoostIntensity.
                         *
                         * CRYO:
                         * liquidBoostIntensity * sqrt(2).
                         */
                        float intensity =
                            hasCryofluid()
                                ? target
                                    .liquidBoostIntensity
                                    * CRYO_INTENSITY_MULTIPLIER
                                : target
                                    .liquidBoostIntensity;

                        float speed =
                            Mathf.lerp(
                                1f,
                                intensity,
                                optionalEfficiency
                            )
                            * efficiency;

                        lastDrillSpeed =
                            (
                                speed
                                * dominantItems
                                * warmup
                            )
                            / delay;

                        warmup =
                            Mathf.approachDelta(
                                warmup,
                                speed,
                                target.warmupSpeed
                            );

                        progress +=
                            delta()
                            * dominantItems
                            * speed
                            * warmup;

                        if (
                            Mathf.chanceDelta(
                                target
                                    .updateEffectChance
                                    * warmup
                            )
                        ) {

                            target.updateEffect.at(
                                x + Mathf.range(
                                    target.size * 2f
                                ),
                                y + Mathf.range(
                                    target.size * 2f
                                )
                            );
                        }

                    } else {

                        lastDrillSpeed = 0f;

                        warmup =
                            Mathf.approachDelta(
                                warmup,
                                0f,
                                target.warmupSpeed
                            );

                        return;
                    }

                    if (
                        dominantItems > 0
                        && progress >= delay
                        && items.total()
                            < target.itemCapacity
                    ) {

                        int amount =
                            (int)(progress / delay);

                        for (
                            int i = 0;
                            i < amount;
                            i++
                        ) {
                            offload(dominantItem);
                        }

                        progress %= delay;

                        if (
                            wasVisible
                            && Mathf.chanceDelta(
                                target
                                    .drillEffectChance
                                    * warmup
                            )
                        ) {

                            target.drillEffect.at(
                                x + Mathf.range(
                                    target.drillEffectRnd
                                ),
                                y + Mathf.range(
                                    target.drillEffectRnd
                                ),
                                dominantItem.color
                            );
                        }
                    }
                }
            };
    }

    /*
     * Замінює старий vanilla consumer
     * на наш CryoLiquidBoost.
     */
    private static Consume[] replace(
        Consume[] source,
        Consume oldConsumer,
        Consume replacement,
        boolean addReplacement
    ) {

        int count = 0;

        for (Consume consumer : source) {

            if (consumer != oldConsumer) {
                count++;
            }
        }

        Consume[] result =
            Arrays.copyOf(
                source,
                count
                    + (addReplacement ? 1 : 0)
            );

        int index = 0;

        for (Consume consumer : source) {

            if (consumer != oldConsumer) {
                result[index++] = consumer;
            }
        }

        if (addReplacement) {
            result[index] = replacement;
        }

        return result;
    }
}
