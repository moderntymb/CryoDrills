package example;

import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import arc.util.Strings;

import mindustry.content.Liquids;
import mindustry.world.blocks.production.Drill;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.StatValue;

public class CryoDrillStats {

    public static void install(
        Drill drill,
        float waterAmount
    ) {
        /*
         * Drill.setStats() уже створив vanilla booster.
         * Прибираємо його повністю.
         */
        drill.stats.remove(Stat.booster);

        /*
         * Додаємо наш власний опис.
         */
        drill.stats.add(
            Stat.booster,
            (StatValue)table -> {
                addLiquid(
                    table,
                    Liquids.water,
                    waterAmount * 60f,
                    1f,
                    "water speed"
                );

                table.row();

                addLiquid(
                    table,
                    Liquids.cryofluid,
                    waterAmount
                        * CryoLiquidBoost.CRYO_CONSUMPTION
                        * 60f,
                    2f,
                    "water speed"
                );
            }
        );
    }

    private static void addLiquid(
        Table table,
        mindustry.type.Liquid liquid,
        float perSecond,
        float speed,
        String speedText
    ) {
        table.table(info -> {

            /*
             * Іконка рідини.
             */
            info.image(liquid.uiIcon)
                .size(40f)
                .padRight(10f);

            /*
             * Назва + споживання.
             */
            info.table(text -> {

                text.add(liquid.localizedName)
                    .left();

                text.row();

                text.add(
                    Strings.autoFixed(
                        perSecond,
                        2
                    )
                    + StatUnit.perSecond.localized()
                )
                .color(Color.lightGray)
                .left();
            })
            .left();

            /*
             * Множник швидкості.
             */
            info.table(speedTable -> {

                speedTable.add(
                    "[stat]"
                    + Strings.autoFixed(
                        speed,
                        2
                    )
                    + "x[] [lightgray]"
                    + speedText
                    + "[]"
                );

            })
            .right()
            .growX()
            .padLeft(20f);

        })
        .growX()
        .pad(5f);
    }

    private CryoDrillStats() {
    }
}
