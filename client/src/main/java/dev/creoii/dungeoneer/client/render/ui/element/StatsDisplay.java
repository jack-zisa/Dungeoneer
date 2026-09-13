package dev.creoii.dungeoneer.client.render.ui.element;

import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import dev.creoii.dungeoneer.client.Assets;
import dev.creoii.dungeoneer.client.render.ui.screen.AbstractScreen;
import dev.creoii.dungeoneer.util.stat.Stat;
import dev.creoii.dungeoneer.util.stat.StatContainer;
import org.jspecify.annotations.Nullable;

public class StatsDisplay extends Table {
    private static final int COLUMNS = 2;
    private static final NinePatchDrawable SECTION_BACKGROUND = new NinePatchDrawable(Assets.SECTION_9PATCH);

    public StatsDisplay(StatContainer stats, @Nullable StatContainer maxStats) {
        setBackground(SECTION_BACKGROUND);
        if (stats != null) refresh(stats, maxStats);
    }

    public void refresh(StatContainer stats, @Nullable StatContainer maxStats) {
        clearChildren();

        int i = 0;
        for (Stat.Type type : Stat.Type.values()) {
            StatDisplay statDisplay = switch (type) {
                case HEALTH -> new StatDisplay(stats.health(), maxStats == null ? null : maxStats.health());
                case DEFENSE -> new StatDisplay(stats.defense(), maxStats == null ? null : maxStats.defense());
                case SPEED -> new StatDisplay(stats.speed(), maxStats == null ? null : maxStats.speed());
                case DEXTERITY -> new StatDisplay(stats.dexterity(), maxStats == null ? null : maxStats.dexterity());
                case VITALITY -> new StatDisplay(stats.vitality(), maxStats == null ? null : maxStats.vitality());
            };

            if (statDisplay.getStat().base() == 0f) continue;

            add(statDisplay).width(120f).height(24f);

            if (i % COLUMNS == COLUMNS - 1) row();
            i++;
        }

        invalidateHierarchy();
    }

    public static class StatDisplay extends Container<Label> {
        private final Stat stat;

        public StatDisplay(Stat stat, @Nullable Stat maxStat) {
            this.stat = stat;
            String modifier;

            if (stat.modifier() > 0f) {
                modifier = String.format("+%,d", (int) stat.modifier());
            } else if (stat.modifier() < 0f) {
                modifier = String.format("%,d", (int) stat.modifier());
            } else modifier = "";


            String text;
            if (maxStat == null) {
                String base = String.format("%s%,d", stat.base() > 0 ? "+" : "", (int) stat.base());
                text = String.format("%s %s", base, stat.type().getPrefix());
            } else text = String.format("%s: %,d %s", stat.type().getPrefix(), (int) stat.base(), modifier.isEmpty() ? "" : String.format("(%s)", modifier));

            setActor(new Label(text, AbstractScreen.SKIN));

            left();
        }

        public Stat getStat() {
            return stat;
        }

        @Override
        public float getPrefWidth() {
            return 120f;
        }

        @Override
        public float getPrefHeight() {
            return 24f;
        }
    }
}
