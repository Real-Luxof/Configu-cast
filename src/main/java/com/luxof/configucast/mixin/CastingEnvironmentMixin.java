package com.luxof.configucast.mixin;

import at.petrak.hexcasting.api.casting.PatternShapeMatch;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;

import com.luxof.configucast.mishap.MishapIDunnoThat;
import com.luxof.configucast.mishap.MishapPlayerlessGated;

import static com.luxof.configucast.ConfigucastDatapackLoader.playerGates;
import static com.luxof.configucast.ConfigucastDatapackLoader.playerlessDisallowed;

import net.minecraft.advancement.Advancement;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CastingEnvironment.class, remap = false)
public abstract class CastingEnvironmentMixin {

    @Shadow
    protected abstract Identifier actionKey(PatternShapeMatch psm);

    @Shadow
    public abstract LivingEntity getCastingEntity();

    @Inject(method = "precheckAction", at = @At("HEAD"))
    public void configucast$gateAction(PatternShapeMatch match, CallbackInfo ci) {
        Identifier key = actionKey(match);
        ServerPlayerEntity caster = getCastingEntity() instanceof ServerPlayerEntity sp ? sp : null;

        Identifier advancementId = playerGates.get(key);

        if (caster == null && playerlessDisallowed.contains(key))
            throw new MishapPlayerlessGated();
        else if (advancementId == null)
            return;

        Advancement advancement = caster.getServer().getAdvancementLoader().get(advancementId);
        if (!caster.getAdvancementTracker().getProgress(advancement).isDone())
            throw new MishapIDunnoThat();
    }
}
