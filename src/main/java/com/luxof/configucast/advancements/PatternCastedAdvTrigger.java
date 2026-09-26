package com.luxof.configucast.advancements;

import com.google.gson.JsonObject;

import static com.luxof.configucast.Configucast.id;

import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

// ???                                                          vvvvvvvvvvvvvvvvvvvvvvvvvv ??????
public class PatternCastedAdvTrigger extends AbstractCriterion<PatternCastedAdvTrigger.Conditions> {
    public static final Identifier id = id("pattern_casted");

    @Override public Identifier getId() { return id; }

    @Override
    protected Conditions conditionsFromJson(
        JsonObject obj,
        LootContextPredicate playerPredicate,
        AdvancementEntityPredicateDeserializer predicateDeserializer
    ) {
        return new Conditions(
            playerPredicate,
            obj.get("pattern_id").getAsString()
        );
    }

    public void trigger(ServerPlayerEntity player, String executedPattern) {
        super.trigger(
            player,
            conditions -> conditions.test(executedPattern)
        );
    }

    public static class Conditions extends AbstractCriterionConditions {
        public final String patternId;

        public Conditions(LootContextPredicate entity, String patternId) {
            super(PatternCastedAdvTrigger.id, entity);
            this.patternId = patternId;
        }

        @Override
        public Identifier getId() { return PatternCastedAdvTrigger.id; }

        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
            JsonObject json = new JsonObject();
            
            return json;
        }

        public boolean test(String executedPattern) {
            return executedPattern.equals(patternId);
        }
    }
}
