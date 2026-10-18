
package example;

import mindustry.content.Liquids;
import mindustry.gen.Building;
import mindustry.type.Liquid;
import mindustry.world.Block;
import mindustry.world.consumers.ConsumeLiquidBase;
import mindustry.world.meta.Stat;

public class CryoLiquidBoost extends ConsumeLiquidBase {

    public CryoLiquidBoost(float waterAmount) {
        super(waterAmount);
        boost();
    }

    @Override
    public void apply(Block block) {
        super.apply(block);
        block.liquidFilter[Liquids.water.id] = true;
        block.liquidFilter[Liquids.cryofluid.id] = true;
    }

    @Override
    public boolean consumes(Liquid liquid) {
        return liquid == Liquids.water
            || liquid == Liquids.cryofluid;
    }

    private Liquid selected(Building build) {
        if (build.liquids.get(Liquids.cryofluid) > 0.001f) {
            return Liquids.cryofluid;
        }
        if (build.liquids.get(Liquids.water) > 0.001f) {
            return Liquids.water;
        }
        return null;
    }

    @Override
    public float efficiency(Building build) {
        float ed = build.edelta() * build.efficiencyScale();
        if (ed <= 0.00000001f) return 0f;

        Liquid liquid = selected(build);
        if (liquid == null) return 0f;

        float rate = amount
            * (liquid == Liquids.cryofluid ? 2f : 1f)
            * ed * multiplier.get(build);

        return Math.min(build.liquids.get(liquid) / rate, 1f);
    }

    @Override
    public void update(Building build) {
        Liquid liquid = selected(build);
        if (liquid == null) return;

        float rate = amount
            * (liquid == Liquids.cryofluid ? 2f : 1f);

        build.liquids.remove(
            liquid,
            rate * build.edelta() * multiplier.get(build)
        );
    }

    @Override
    public void display(mindustry.world.meta.Stats stats) {
        stats.add(Stat.booster, Liquids.water, amount * 60f, true);
        stats.add(Stat.booster, Liquids.cryofluid, amount * 120f, true);
    }
    }
