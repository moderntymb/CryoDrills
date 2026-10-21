
package example;

import arc.math.Mathf;
import arc.util.Log;
import mindustry.content.Blocks;
import mindustry.content.Liquids;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.blocks.production.Drill;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeLiquidBase;

import java.util.Arrays;

public class CryoDrillLogic {

    public static void prepare() {
        configure(Blocks.pneumaticDrill);
        configure(Blocks.laserDrill);
        configure(Blocks.blastDrill);
    }

    private static void configure(Block block) {
        if (!(block instanceof Drill)) return;

        Drill drill = (Drill) block;
        ConsumeLiquidBase old = null;

        for (Consume consumer : drill.consumers) {
            if (consumer instanceof ConsumeLiquidBase
                && consumer.booster
                && ((ConsumeLiquidBase) consumer).consumes(Liquids.water)) {
                old = (ConsumeLiquidBase) consumer;
                break;
            }
        }

        if (old == null) {
            Log.err("CryoDrills: water booster not found for @", drill.name);
            return;
        }

        CryoLiquidBoost replacement = new CryoLiquidBoost(old.amount);
        replacement.update = true;
        replacement.apply(drill);

        drill.consumers = replace(drill.consumers, old, replacement, true);
        drill.optionalConsumers = replace(
            drill.optionalConsumers, old, replacement, true
        );
        drill.nonOptionalConsumers = replace(
            drill.nonOptionalConsumers, old, replacement, false
        );
        drill.updateConsumers = replace(
            drill.updateConsumers, old, replacement, true
        );

        drill.hasConsumers = drill.consumers.length > 0;

        final Drill target = drill;
        drill.buildType = () -> target.new DrillBuild() {
            @Override
            public void updateTile() {
                if (timer(timerDump, dumpTime / timeScale)) {
                    dump(dominantItem != null && items.has(dominantItem)
                        ? dominantItem : null);
                }

                if (dominantItem == null) return;

                timeDrilled += warmup * delta();

                float delay = getDrillTime(dominantItem);

                if (items.total() < itemCapacity
                    && dominantItems > 0 && efficiency > 0) {

                    float intensity =
                        liquids.get(Liquids.cryofluid) > 0.001f
                        ? target.liquidBoostIntensity * Mathf.sqrt(2f)
                        : target.liquidBoostIntensity;

                    float speed = Mathf.lerp(
                        1f, intensity, optionalEfficiency
                    ) * efficiency;

                    lastDrillSpeed =
                        (speed * dominantItems * warmup) / delay;

                    warmup = Mathf.approachDelta(
                        warmup, speed, target.warmupSpeed
                    );

                    progress += delta() * dominantItems * speed * warmup;

                    if (Mathf.chanceDelta(target.updateEffectChance * warmup)) {
                        target.updateEffect.at(
                            x + Mathf.range(target.size * 2f),
                            y + Mathf.range(target.size * 2f)
                        );
                    }
                } else {
                    lastDrillSpeed = 0f;
                    warmup = Mathf.approachDelta(
                        warmup, 0f, target.warmupSpeed
                    );
                    return;
                }

                if (dominantItems > 0 && progress >= delay
                    && items.total() < itemCapacity) {

                    int amount = (int)(progress / delay);

                    for (int i = 0; i < amount; i++) {
                        offload(dominantItem);
                    }

                    progress %= delay;

                    if (wasVisible && Mathf.chanceDelta(
                        target.drillEffectChance * warmup
                    )) {
                        target.drillEffect.at(
                            x + Mathf.range(target.drillEffectRnd),
                            y + Mathf.range(target.drillEffectRnd),
                            dominantItem.color
                        );
                    }
                }
            }
        };

        Log.info("CryoDrills: configured @", drill.name);
    }

    private static Consume[] replace(
        Consume[] source,
        Consume old,
        Consume replacement,
        boolean addReplacement
    ) {
        int count = 0;

        for (Consume consumer : source) {
            if (consumer != old) count++;
        }

        Consume[] result = Arrays.copyOf(source,
            count + (addReplacement ? 1 : 0));

        int index = 0;

        for (Consume consumer : source) {
            if (consumer != old) result[index++] = consumer;
        }

        if (addReplacement) result[index] = replacement;

        return result;
    }
}
