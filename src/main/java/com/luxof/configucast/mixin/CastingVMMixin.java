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

import com.luxof.configucast.MathException;
import com.luxof.configucast.MethInterpreter;

import static com.luxof.configucast.Configucast.LOGGER;

import java.util.ArrayList;
import java.util.List;

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

        var formula = MethInterpreter.getMathFormulaFor(getId(PatternRegistryManifest.matchPattern(
            ((PatternIota)result.getCast()).getPattern(),
            env,
            false
        )));
        if (formula == null) return result;

        long originalAmount = 0;
        List<OperatorSideEffect> sideEffects = new ArrayList<>();
        for (OperatorSideEffect sideEffect : result.getSideEffects()) {
            if (sideEffect instanceof OperatorSideEffect.ConsumeMedia cm)
                originalAmount += cm.getAmount();
            else
                sideEffects.add(sideEffect);
        }

        try {
            sideEffects.add(new OperatorSideEffect.ConsumeMedia(
                MethInterpreter.interpretMath(formula, originalAmount, env, img)
            ));
        } catch (MathException me) {
            LOGGER.error("Encountered a MathException!", me);
        } catch (Exception e) {
            LOGGER.error("Whoops! Error found (likely a bug in Configu-cast)!", e);
        }

        return result.copy(
            result.getCast(),
            result.getContinuation(),
            result.getNewData(),
            sideEffects,
            result.getResolutionType(),
            result.getSound()
        );
    }

    private static Identifier getId(PatternShapeMatch psm) {
		if (psm instanceof PatternShapeMatch.Normal n) return n.key.getValue();
		else if (psm instanceof PatternShapeMatch.PerWorld pw) return pw.key.getValue();
		else if (psm instanceof PatternShapeMatch.Special s) return s.key.getValue();
		else return null;
	}
}
