package com.luxof.configucast.advancements;

import at.petrak.hexcasting.mixin.accessor.CriteriaTriggersAccessor;

public class ConfigucastAdvancementTriggers {
    // shouldn't this one be a PR to hex?
    public static final PatternCastedAdvTrigger PATTERN_CASTED = new PatternCastedAdvTrigger();
    public static final PatternCastedWithFunnyMethInterpreterAdvTrigger PATTERN_CASTED_WITH_FUNNY_METH_INTERPRETER = new PatternCastedWithFunnyMethInterpreterAdvTrigger();
    public static void register() {
        CriteriaTriggersAccessor.hex$register(PATTERN_CASTED);
        CriteriaTriggersAccessor.hex$register(PATTERN_CASTED_WITH_FUNNY_METH_INTERPRETER);
    }
}
