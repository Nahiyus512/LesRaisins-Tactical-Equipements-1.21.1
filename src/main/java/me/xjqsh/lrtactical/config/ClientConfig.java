package me.xjqsh.lrtactical.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    public static ModConfigSpec SPEC;
    public static ModConfigSpec.BooleanValue BLACK_FLASH;
    public static ModConfigSpec.DoubleValue EXPLODE_SCREEN_SHAKE_MULTIPLIER;
    public static ModConfigSpec.EnumValue<HudStyle> HUD_STYLE;

    public enum HudStyle {
        RING,
        LINE
    }

    public static ModConfigSpec init() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Use black overlay instead of white when blinded by flashbang");
        BLACK_FLASH = builder.define("blackFlash", false);
        EXPLODE_SCREEN_SHAKE_MULTIPLIER = builder
                .comment("Screen shake multiplier for explosions, default is 1.0")
                .defineInRange("explodeScreenShakeMultiplier", 1.0, 0.0, 128.0);
        HUD_STYLE = builder
                .comment("HUD style for combat/use progress display")
                .defineEnum("hudStyle", HudStyle.LINE);
        SPEC = builder.build();
        return SPEC;
    }
}
