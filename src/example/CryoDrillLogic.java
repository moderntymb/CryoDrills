
package example;

import arc.util.Log;
import mindustry.content.Blocks;
import mindustry.world.Block;
import mindustry.world.blocks.production.Drill;

public class CryoDrillLogic {

    public static void prepare() {
        Log.info("CryoDrills: checking vanilla drills.");

        check(Blocks.mechanicalDrill);
        check(Blocks.pneumaticDrill);
        check(Blocks.laserDrill);
        check(Blocks.blastDrill);
    }

    private static void check(Block block) {
        if (!(block instanceof Drill)) {
            Log.info("CryoDrills: @ is not a standard Drill.", block.name);
            return;
        }

        Drill drill = (Drill) block;
        Log.info("CryoDrills: found drill @, water boost @.",
            drill.name, drill.liquidBoostIntensity);
    }
}
