package com.luxof.configucast.mishap;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.pigment.FrozenPigment;

import java.util.List;

import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

public class MishapPlayerlessGated extends Mishap {

    @Override
    public FrozenPigment accentColor(CastingEnvironment env, Context ctx) {
        return dyeColor(DyeColor.YELLOW);
    }

    @Override
    protected Text errorMessage(CastingEnvironment env, Context ctx) {
        return Text.translatable("mishaps.configucast.playerlessmaynotcast");
    }

    @Override
    public void execute(CastingEnvironment env, Context ctx, List<Iota> stack) {}
    
}
