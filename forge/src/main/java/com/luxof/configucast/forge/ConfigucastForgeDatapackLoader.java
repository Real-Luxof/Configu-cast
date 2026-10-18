package com.luxof.configucast.forge;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.luxof.configucast.ConfigucastDatapackLoader;

import java.util.Map;

import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

public class ConfigucastForgeDatapackLoader extends JsonDataLoader {
    private static final Gson GSON = new Gson();

    public ConfigucastForgeDatapackLoader() { super(GSON, ""); }

    @Override
    protected void apply(
        Map<Identifier, JsonElement> prepared,
        ResourceManager manager,
        Profiler profiler
    ) {
        ConfigucastDatapackLoader.reload(manager);
    }
}
