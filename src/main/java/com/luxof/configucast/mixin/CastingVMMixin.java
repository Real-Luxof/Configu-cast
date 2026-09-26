package com.luxof.configucast.mixin;

import at.petrak.hexcasting.api.casting.PatternShapeMatch;
import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.common.casting.PatternRegistryManifest;

import com.luxof.configucast.advancements.ConfigucastAdvancementTriggers;
import com.luxof.configucast.meth.EquationParser;
import com.luxof.configucast.meth.MathException;
import com.luxof.configucast.meth.MethInterpreter;

import static com.luxof.configucast.Configucast.LOGGER;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = CastingVM.class, remap = false)
public abstract class CastingVMMixin {

    @Shadow
    public abstract CastingImage getImage();
    @Shadow
    public abstract CastingEnvironment getEnv();
    
    @ModifyVariable(
        method = "queueExecuteAndWrapIotas",
        at = @At(
            value = "INVOKE_ASSIGN",
            target = "at/petrak/hexcasting/api/casting/eval/vm/ContinuationFrame.evaluate(Lat/petrak/hexcasting/api/casting/eval/vm/SpellContinuation;Lnet/minecraft/server/world/ServerWorld;Lat/petrak/hexcasting/api/casting/eval/vm/CastingVM;)Lat/petrak/hexcasting/api/casting/eval/CastResult;"
        )
    )
    public CastResult configucast$recalculateMediaCost(
        CastResult result
    ) {
        CastingEnvironment env = getEnv();
        CastingImage img = getImage();
        if (
            result.getResolutionType() != ResolvedPatternType.EVALUATED ||
            !(result.getCast() instanceof PatternIota)
        ) return result;

        Identifier patternId = getId(
            PatternRegistryManifest.matchPattern(
                ((PatternIota)result.getCast()).getPattern(),
                env
            )
        );

        var formula = EquationParser.getMathFormulaFor(patternId);
        if (formula == null) {
            doAdvancementGating(
                env,
                patternId,
                result.getSideEffects().stream().reduce(
                    0L,
                    (l1, sideEffect2) -> {
                        return l1 + (sideEffect2 instanceof OperatorSideEffect.ConsumeMedia cm2
                                ? cm2.getAmount() : 0L);
                    },
                    (l1, l2) -> l1 + l2
                ),
                img
            );
            return result;
        }

        long originalAmount = 0;
        List<OperatorSideEffect> sideEffects = new ArrayList<>();
        for (OperatorSideEffect sideEffect : result.getSideEffects()) {
            if (sideEffect instanceof OperatorSideEffect.ConsumeMedia cm)
                originalAmount += cm.getAmount();
            else
                sideEffects.add(sideEffect);
        }

        long newCost = 0;
        try {
            newCost = MethInterpreter.simplifyToNum(formula, originalAmount, env, img);
            sideEffects.add(new OperatorSideEffect.ConsumeMedia(newCost));
        } catch (MathException me) {
            LOGGER.error("Encountered a MathException!", me);
        } catch (Exception e) {
            LOGGER.error("Whoops! Error found (likely a bug in Configu-cast)!", e);
        }

        doAdvancementGating(env, patternId, newCost, img);

        return result.copy(
            result.getCast(),
            result.getContinuation(),
            result.getNewData(),
            sideEffects,
            result.getResolutionType(),
            result.getSound()
        );
    }

    private static void doAdvancementGating(
        CastingEnvironment env,
        Identifier patternId,
        long patternCost,
        CastingImage img
    ) {
        if (env.getCastingEntity() instanceof ServerPlayerEntity player) {
            ConfigucastAdvancementTriggers.PATTERN_CASTED.trigger(
                player,
                patternId.toString()
            );
            ConfigucastAdvancementTriggers.PATTERN_CASTED_WITH_FUNNY_METH_INTERPRETER.trigger(
                player,
                patternId.toString(),
                patternCost,
                env,
                img
            );
        }
    }

    private static Identifier getId(PatternShapeMatch psm) {
		if (psm instanceof PatternShapeMatch.Normal n) return n.key.getValue();
		else if (psm instanceof PatternShapeMatch.PerWorld pw) return pw.key.getValue();
		else if (psm instanceof PatternShapeMatch.Special s) return s.key.getValue();
		else return null;
	}
}
