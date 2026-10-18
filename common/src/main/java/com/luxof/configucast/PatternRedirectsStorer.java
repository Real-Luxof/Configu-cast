package com.luxof.configucast;

import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.xplat.IXplatAbstractions;

import java.util.HashMap;

import net.minecraft.util.Identifier;

import org.jetbrains.annotations.Nullable;

public class PatternRedirectsStorer {
    private static HashMap<String, HexPattern> secretStrokeOrderToPatternMap = new HashMap<>();

    private static HashMap<String, Identifier> strokeOrderToId = new HashMap<>();
    private static HashMap<Identifier, String> idToStrokeOrder = new HashMap<>();

    public static boolean isInEffectForCursedMixins = false;

    public static void clear() {
        strokeOrderToId.clear();
        idToStrokeOrder.clear();
    }
    public static void put(HexPattern pattern, Identifier actionId) throws RuntimeException {
        if (!IXplatAbstractions.INSTANCE.getActionRegistry().containsId(actionId))
            throw new RuntimeException("No pattern with ID: " + actionId.toString());

        secretStrokeOrderToPatternMap.put(pattern.anglesSignature(), pattern);
        strokeOrderToId.put(pattern.anglesSignature(), actionId);
        idToStrokeOrder.put(actionId, pattern.anglesSignature());
    }
    @Nullable public static Identifier get(String strokeOrder) {
        return strokeOrderToId.get(strokeOrder);
    }
    @Nullable public static HexPattern get(Identifier actionId) {
        return secretStrokeOrderToPatternMap.get(idToStrokeOrder.get(actionId));
    }
    public static boolean hasRedirect(String strokeOrder) {
        return strokeOrderToId.containsKey(strokeOrder);
    }
    public static boolean hasRedirect(Identifier actionId) {
        return idToStrokeOrder.containsKey(actionId);
    }
}
