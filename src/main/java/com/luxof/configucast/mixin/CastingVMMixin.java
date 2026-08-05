package com.luxof.configucast.mixin;

import at.petrak.hexcasting.api.casting.PatternShapeMatch;
import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.ExecutionClientView;
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.common.casting.PatternRegistryManifest;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import com.luxof.configucast.MethInterpreter;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = CastingVM.class, remap = false)
public abstract class CastingVMMixin {

    @Shadow
    public abstract CastingImage getImage();
    @Shadow
    public abstract CastingEnvironment getEnv();
    
    @Inject(
        method = "queueExecuteAndWrapIotas",
        at = @At(
            value = "INVOKE",
            target = "at/petrak/hexcasting/api/casting/eval/CastResult.getNewData()Lat/petrak/hexcasting/api/casting/eval/vm/CastingImage;",
            shift = At.Shift.BEFORE
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void configucast$calculateMediaCostAheadOfTime(
        List<? extends Iota> iotas,
        ServerWorld world,
        CallbackInfoReturnable<ExecutionClientView> cir,
        @Local LocalRef<CastResult> resultRef
    ) {
        CastResult result = resultRef.get();
        CastingEnvironment env = getEnv();
        CastingImage img = getImage();
        if (
            result.getResolutionType() != ResolvedPatternType.EVALUATED ||
            !(result.getCast() instanceof PatternIota)
        ) return;

        var formula = MethInterpreter.getMathFormulaFor(getId(PatternRegistryManifest.matchPattern(
            ((PatternIota)result.getCast()).getPattern(),
            env,
            false
        )));
        if (formula == null) return;

        long originalAmount = 0;
        List<OperatorSideEffect> sideEffects = new ArrayList<>();
        for (OperatorSideEffect sideEffect : result.getSideEffects()) {
            if (sideEffect instanceof OperatorSideEffect.ConsumeMedia cm)
                originalAmount += cm.getAmount();
            else
                sideEffects.add(sideEffect);
        }
        sideEffects.add(new OperatorSideEffect.ConsumeMedia(
            MethInterpreter.interpretMath(formula, originalAmount, env, img)
        ));
        resultRef.set(result.copy(
            result.getCast(),
            result.getContinuation(),
            result.getNewData(),
            sideEffects,
            result.getResolutionType(),
            result.getSound()
        ));
    }

    public static Identifier getId(PatternShapeMatch psm) {
		if (psm instanceof PatternShapeMatch.Normal n) return n.key.getValue();
		else if (psm instanceof PatternShapeMatch.PerWorld pw) return pw.key.getValue();
		else if (psm instanceof PatternShapeMatch.Special s) return s.key.getValue();
		else return null;
	}
}
