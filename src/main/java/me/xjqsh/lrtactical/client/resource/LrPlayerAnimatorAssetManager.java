package me.xjqsh.lrtactical.client.resource;

import dev.kosmx.playerAnim.api.IPlayable;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import dev.kosmx.playerAnim.minecraftApi.codec.AnimationCodecs;
import me.xjqsh.lrtactical.EquipmentMod;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Loads LRTactical player animations from normal mod resources.
 *
 * <p>TACZ 1.21.1 primarily fills its player-animation cache while scanning gun
 * packs. Depending on that cache for regular mod assets leaves these animations
 * undiscoverable, so LRTactical owns the reload listener for its own assets.</p>
 */
public final class LrPlayerAnimatorAssetManager extends
        SimplePreparableReloadListener<Map<ResourceLocation, Map<String, KeyframeAnimation>>> {
    public static final LrPlayerAnimatorAssetManager INSTANCE = new LrPlayerAnimatorAssetManager();

    private static final FileToIdConverter CONVERTER = new FileToIdConverter("player_animator", ".json");
    private final Map<ResourceLocation, Map<String, KeyframeAnimation>> animations = new HashMap<>();

    private LrPlayerAnimatorAssetManager() {
    }

    @Override
    protected @NotNull Map<ResourceLocation, Map<String, KeyframeAnimation>> prepare(
            ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, Map<String, KeyframeAnimation>> loaded = new HashMap<>();
        CONVERTER.listMatchingResources(resourceManager).forEach((file, resource) -> {
            ResourceLocation id = CONVERTER.fileToId(file);
            try (InputStream stream = resource.open()) {
                Collection<IPlayable> decoded = AnimationCodecs.deserialize("json", () -> stream);
                for (IPlayable playable : decoded) {
                    if (playable instanceof KeyframeAnimation animation
                            && animation.extraData.get("name") instanceof String name) {
                        String normalizedName = PlayerAnimationRegistry.serializeTextToString(name)
                                .toLowerCase(Locale.ENGLISH);
                        loaded.computeIfAbsent(id, ignored -> new HashMap<>())
                                .put(normalizedName, animation);
                    }
                }
            } catch (Exception exception) {
                EquipmentMod.LOGGER.error("Failed to load player animation resource {}", file, exception);
            }
        });
        return loaded;
    }

    @Override
    protected void apply(Map<ResourceLocation, Map<String, KeyframeAnimation>> loaded,
                         ResourceManager resourceManager, ProfilerFiller profiler) {
        animations.clear();
        animations.putAll(loaded);
        EquipmentMod.LOGGER.info("Loaded {} LRTactical player animation file(s)", animations.size());
    }

    public Optional<KeyframeAnimation> getAnimation(ResourceLocation id, String name) {
        return Optional.ofNullable(animations.get(id)).map(group -> group.get(name));
    }
}
