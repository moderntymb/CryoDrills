package example;

import arc.util.Log;
import mindustry.mod.Mod;

public class ExampleJavaMod extends Mod {

    public ExampleJavaMod() {
        Log.info("CryoDrills loaded.");
    }

    @Override
    public void init() {
        CryoDrillLogic.prepare();

        Log.info(
            "CryoDrills: initialization complete."
        );
    }
}
