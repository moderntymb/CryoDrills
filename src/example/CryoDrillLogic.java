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
     * У звичайного Drill liquidBoostIntensity фактично
     * впливає на швидкість двічі:
     *
     * final boost ~= intensity^2
     *
     * Тому для 2x КІНЦЕВОГО boost:
     *
     * cryoIntensity = waterIntensity * sqrt(2)
     */
    private static final float CRYO_INTENSITY_MULTIPLIER =
        Mathf.sqrt(2f);

    private static final float EPSILON =
        CryoLiquidBoost.EPSILON;

    public static void prepare() {
        configure(Blocks.pneumaticDrill);
        configure(Blocks.laserDrill);
        configure(Blocks.blastDrill);

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

        /*
         * ConsumeLiquid normally updates every tick.
         * Ми замінюємо вже ініціалізований consumer,
         * тому явно залишаємо його update-consumer'ом.
         */
        replacement.update = true;
        replacement.apply(drill);

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

        installBuildType(drill);

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
             * Дуже важлива перевірка booster.
             *
             * Через це Impact Drill не буде випадково
             * змінений, адже його вода є звичайним
             * consumption, а booster там інша рідина.
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
                 * Перша версія:
                 * вода і кріоген не змішуються.
                 *
                 * Якщо всередині вже є одна рідина,
                 * друга не приймається.
                 *
                 * Коли додамо аварії, саме це місце
                 * змінимо для Laser/Blast Drill.
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

                    if (
                        timer(
                            timerDump,
                            dumpTime / timeScale
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

                    timeDrilled += warmup * delta();

                    float delay =
                        getDrillTime(dominantItem);

                    if (
                        items.total() < itemCapacity
                        && dominantItems > 0
                        && efficiency > 0
                    ) {

                        /*
                         * Water:
                         * target.liquidBoostIntensity
                         *
                         * Cryofluid:
                         * intensity * sqrt(2)
                         *
                         * Оскільки Drill дає приблизно
                         * intensity^2 фінального boost,
                         * це дає рівно 2x результат води.
                         */
                        float intensity =
                            hasCryofluid()
                                ? target.liquidBoostIntensity
                                    * CRYO_INTENSITY_MULTIPLIER
                                : target.liquidBoostIntensity;

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
                                target.updateEffectChance
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
                        && items.total() < itemCapacity
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
                                target.drillEffectChance
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
                count + (addReplacement ? 1 : 0)
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
