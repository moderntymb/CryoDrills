package example;

import mindustry.content.Liquids;
import mindustry.type.Liquid;
import mindustry.world.blocks.production.Drill;

public class CryoDrillUI {

    /**
     * Встановлює UI для бура.
     *
     * Смужка автоматично показує:
     * - Water, якщо в бурі вода;
     * - Cryofluid, якщо в бурі кріоген;
     * - порожню liquid bar, якщо рідини немає.
     *
     * Колір та заповнення бере стандартна система
     * liquid bars Mindustry.
     */
    public static void install(Drill drill) {

        /*
         * Прибираємо стандартну liquid bar.
         *
         * Потім ставимо нашу динамічну,
         * яка може показувати і воду, і кріоген.
         */
        drill.removeBar("liquid");

        drill.addLiquidBar(build -> {

            float cryo =
                build.liquids.get(Liquids.cryofluid);

            float water =
                build.liquids.get(Liquids.water);

            /*
             * Змішування в CryoDrillLogic заборонене,
             * але перевіряємо обидві рідини окремо.
             *
             * Cryofluid перевіряється першим.
             */
            if (cryo > CryoLiquidBoost.EPSILON) {
                return Liquids.cryofluid;
            }

            if (water > CryoLiquidBoost.EPSILON) {
                return Liquids.water;
            }

            /*
             * Коли бур порожній, повертаємо null.
             * Стандартна liquid bar Mindustry
             * обробляє порожній стан.
             */
            return null;
        });
    }

    private CryoDrillUI() {
        // Utility class.
    }
}
