package example;

import mindustry.content.Liquids;
import mindustry.world.blocks.production.Drill;

public class CryoDrillUI {

    /**
     * Встановлює UI рідин для CryoDrills.
     *
     * Замість старої окремої полоски Water
     * використовуємо одну динамічну полоску:
     *
     * Water     -> показує воду.
     * Cryofluid -> показує кріоген.
     * Empty     -> показує "Liquid".
     */
    public static void install(Drill drill) {

        /*
         * Vanilla Mindustry створює liquid bar
         * для конкретної рідини.
         *
         * Тому прибираємо стару полоску Water.
         */
        drill.removeBar(
            "liquid-" + Liquids.water.name
        );

        /*
         * Про всяк випадок прибираємо стару
         * нашу полоску, якщо install()
         * буде викликаний повторно.
         */
        drill.removeBar("liquid");

        /*
         * Одна динамічна полоска.
         *
         * addLiquidBar сама:
         * - бере localizedName рідини;
         * - бере її стандартний колір;
         * - показує кількість рідини;
         * - враховує liquidCapacity бура.
         */
        drill.addLiquidBar(build -> {

            float cryofluid =
                build.liquids.get(
                    Liquids.cryofluid
                );

            float water =
                build.liquids.get(
                    Liquids.water
                );

            /*
             * Якщо є кріоген —
             * показуємо Cryofluid.
             */
            if (
                cryofluid
                    > CryoLiquidBoost.EPSILON
            ) {
                return Liquids.cryofluid;
            }

            /*
             * Якщо є вода —
             * показуємо Water.
             */
            if (
                water
                    > CryoLiquidBoost.EPSILON
            ) {
                return Liquids.water;
            }

            /*
             * Бур порожній.
             *
             * addLiquidBar обробить null
             * як відсутність активної рідини.
             */
            return null;
        });
    }

    private CryoDrillUI() {
        // Utility class.
    }
}
