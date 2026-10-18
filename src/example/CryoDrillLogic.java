
package example;

import arc.util.Log;
import mindustry.content.Blocks;
import mindustry.world.Block;
import mindustry.world.blocks.production.Drill;

public class CryoDrillLogic {

    public static void prepare() {
        Log.info("CryoDrills: preparing drills.");

        replaceBoost(Blocks.pneumaticDrill);
    }

    private static void replaceBoost(Block block) {
        if (!(block instanceof Drill)) {
            Log.err("CryoDrills: @ is not a Drill.", block.name);
            return;
        }

        Drill drill = (Drill) block;

        Log.info(
            "CryoDrills: found @, original boost intensity @.",
            drill.name,
            drill.liquidBoostIntensity
        );
    }
}
