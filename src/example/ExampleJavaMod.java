package example;

import arc.util.*;
import mindustry.mod.*;

public class ExampleJavaMod extends Mod {

    public ExampleJavaMod() {
        Log.info("CryoDrills loaded.");
    }

    @Override
    public void loadContent() {
        Log.info("CryoDrills: preparing drill modifications.");
    }
}
