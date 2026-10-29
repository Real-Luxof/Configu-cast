package com.luxof.configucast.mixin;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.math.EulerPathFinder;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.server.ScrungledPatternsSave;
import at.petrak.hexcasting.server.ScrungledPatternsSave.PerWorldEntry;
import at.petrak.hexcasting.xplat.IXplatAbstractions;

import com.luxof.configucast.APieceOfLapisworks;
import com.luxof.configucast.PatternRedirectsStorer;

import static com.luxof.configucast.Configucast.runningServer;

import java.util.HashSet;
import java.util.Map;

import kotlin.Pair;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ScrungledPatternsSave.class, remap = false)
public class ScrungledPatternsSaveMixin {
    @Shadow @Final
    private Map<String, PerWorldEntry> lookup;

    @Shadow @Final
    private Map<RegistryKey<ActionRegistryEntry>, String> reverseLookup;

    @Unique
    private HashSet<Identifier> replacedPatterns = new HashSet<>();

    @Unique
    private void recalcThatPWIfNeeded(
        String signature
    ) {
        if (runningServer == null) return;

        PerWorldEntry pwEntry = lookup.get(signature);
        RegistryKey<ActionRegistryEntry> registryKey = pwEntry.key();
        HexPattern actualPattern = PatternRedirectsStorer.get(registryKey.getValue());

        if (replacedPatterns.contains(registryKey.getValue()) && actualPattern == null) {
            actualPattern = IXplatAbstractions.INSTANCE.getActionRegistry().get(registryKey)
                .prototype();
            replacedPatterns.remove(registryKey.getValue());
        } else if (actualPattern == null)
            return;
        else
            replacedPatterns.add(registryKey.getValue());

        if (
            APieceOfLapisworks.matchShape(
                actualPattern,
                HexPattern.fromAngles(signature, pwEntry.canonicalStartDir())
            )
        ) return;
        long seed = runningServer.getOverworld().getSeed();

        HexPattern newPattern = EulerPathFinder.findAltDrawing(actualPattern, seed);

        lookup.remove(signature);
        lookup.put(
            newPattern.anglesSignature(),
            new PerWorldEntry(registryKey, newPattern.getStartDir())
        );
        reverseLookup.put(registryKey, newPattern.anglesSignature());
    }

    @Inject(
        method = "lookup",
        at = @At("HEAD")
    )
    private void configucast$recalcThatPWIfNeeded1(
        String signature,
        CallbackInfoReturnable<PerWorldEntry> cir
    ) {
        if (!lookup.containsKey(signature)) return;
        recalcThatPWIfNeeded(signature);
    }
    
    @Inject(
        method = "lookupReverse",
        at = @At("HEAD")
    )
    private void configucast$recalcThatPWIfNeeded2(
        RegistryKey<ActionRegistryEntry> key,
        CallbackInfoReturnable<Pair<String, PerWorldEntry>> cir
    ) {
        String sig = reverseLookup.get(key);
        if (sig == null) return;
        recalcThatPWIfNeeded(sig);
    }
}
