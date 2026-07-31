package me.xjqsh.lrtactical.api.event;

import me.xjqsh.lrtactical.api.melee.MeleeAction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.Event;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class MeleePreAttackEvent extends Event {
    private final int playerId;
    private final MeleeAction state;
    private final int actionCount;
    private final ResourceLocation animationId;

    public MeleePreAttackEvent(int playerId, MeleeAction state, int actionCount, @Nullable ResourceLocation animationId) {
        this.playerId = playerId;
        this.state = state;
        this.actionCount = actionCount;
        this.animationId = animationId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public MeleeAction getState() {
        return state;
    }

    public int getActionCount() {
        return actionCount;
    }

    @Nullable
    public ResourceLocation getAnimationId() {
        return animationId;
    }
}
