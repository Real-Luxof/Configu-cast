package com.luxof.configucast.advancements;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;

import com.google.gson.JsonObject;

import com.luxof.configucast.meth.EquationParser;
import com.luxof.configucast.meth.MathException;
import com.luxof.configucast.meth.MethInterpreter;
import com.luxof.configucast.meth.equationparts.EquationPart;

import static com.luxof.configucast.Configucast.LOGGER;
import static com.luxof.configucast.Configucast.id;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PatternCastedWithFunnyMethInterpreterAdvTrigger extends AbstractCriterion<PatternCastedWithFunnyMethInterpreterAdvTrigger.Conditions> {
    public static final Map<String, List<EquationPart>> cachedEquations = new HashMap<>();
    public static final Identifier id = id("pattern_casted_with_funny_meth_interpreter");

    @Override public Identifier getId() { return id; }

    @Override
    protected Conditions conditionsFromJson(
        JsonObject obj,
        LootContextPredicate playerPredicate,
        AdvancementEntityPredicateDeserializer predicateDeserializer
    ) {
        return new Conditions(
            playerPredicate,
            obj.has("pattern_id") ? obj.get("pattern_id").getAsString() : "",
            obj.get("funny_meth").getAsString()
        );
    }

    public void trigger(
        ServerPlayerEntity player,
        String executedPattern,
        long originalAmount,
        CastingEnvironment env,
        CastingImage img
    ) {
        super.trigger(
            player,
            conditions -> conditions.test(executedPattern, originalAmount, env, img)
        );
    }

    public static class Conditions extends AbstractCriterionConditions {
        public final String patternId;
        public final String funnyMeth;

        public Conditions(
            LootContextPredicate entity,
            String patternId,
            String funnyMeth
        ) {
            super(id, entity);
            this.patternId = patternId;
            this.funnyMeth = funnyMeth;
            cachedEquations.computeIfAbsent(
                funnyMeth,
                EquationParser::loadMathEquationFromString
            );
        }

        public boolean test(
            String executedPattern,
            long originalAmount,
            CastingEnvironment env,
            CastingImage img
        ) {
            try {
                return (patternId.isEmpty() || patternId.equals(executedPattern))
                    && Math.abs(MethInterpreter.simplifyToNum(
                        cachedEquations.get(funnyMeth),
                        originalAmount,
                        env,
                        img,
                        Map.of(
                            "executedPattern", executedPattern
                        )
                    )) > 0.0001;
            } catch (MathException me) {
                LOGGER.error(
                    "Encountered a MathException in advancement that requires "
                    + (patternId.isEmpty() ? "no pattern" : patternId)
                    + " with equation " + funnyMeth + "!",
                    me
                );
                return false;
            } catch (Exception e) {
                LOGGER.error(
                    "Whoops! Error found (likely a bug in Configu-cast) in advancement that requires "
                    + (patternId.isEmpty() ? "no pattern" : patternId)
                    + " with equation " + funnyMeth + "!",
                    e
                );
                return false;
            }
        }
    }
}
