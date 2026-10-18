package example;

import arc.util.Log;
import mindustry.mod.Mod;
import mindustry.content.Blocks;
import mindustry.content.Liquids;
import mindustry.world.blocks.production.Drill;

public class CryoDrillLogic {

    public static void prepare() {
        Log.info("CryoDrills: checking vanilla drills.");

        check(Blocks.mechanicalDrill);
        check(Blocks.pneumaticDrill);
        check(Blocks.laserDrill);
        check(Blocks.blastDrill);
    }

    private static void check(Drill drill) {
        if (drill == null) return;

        Log.info("CryoDrills: found drill @, water boost @.",
            drill.name, drill.liquidBoostIntensity);
    }
  }
