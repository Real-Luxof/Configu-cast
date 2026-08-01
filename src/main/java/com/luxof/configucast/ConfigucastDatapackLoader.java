package com.luxof.configucast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static com.luxof.configucast.Configucast.LOGGER;
import static com.luxof.configucast.Configucast.MOD_ID;
import static com.luxof.configucast.Configucast.id;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class ConfigucastDatapackLoader implements SimpleSynchronousResourceReloadListener {

    public static Map<Identifier, Identifier> gates = new HashMap<>();

    public static final Identifier ID = id("datapack_loader");
    @Override public Identifier getFabricId() { return ID; }

    @Override
    public void reload(ResourceManager manager) {
        gates.clear();

        Map<Identifier, Resource> resources = manager.findResources(
            "configucast.json",
            path -> path.getPath().equals("configucast.json") &&
                (
                    !path.getNamespace().equals(MOD_ID) ||
                    FabricLoader.getInstance().isDevelopmentEnvironment()
                )
        );

        for (Identifier path : resources.keySet()) {
            Resource resource = resources.get(path);

            try {
                JsonObject file = JsonParser.parseReader(resource.getReader()).getAsJsonObject();

                // for FUCK'S sake i can't add a throws clause to it
                /*loadGates(file.getAsJsonObject("gates")).forEach(
                    (action, advancement) -> {
                        if (gates.containsKey(action)) throw new Exception(String.format("%s is already gated to another advancement (%s) by another datapack!", action.toString(), gates.get(action).toString()));
                    }
                );*/
                for (Entry<Identifier, Identifier> entry : loadGates(file.getAsJsonObject("gates")).entrySet()) {
                    Identifier action = entry.getKey();
                    Identifier advancement = entry.getValue();
                    if (gates.containsKey(action)) throw new Exception(String.format("%s is already gated to another advancement (%s) by another datapack!", action.toString(), gates.get(action).toString()));
                    gates.put(action, advancement);
                }

            } catch (Exception e) {
                LOGGER.error(
                    String.format(
                        "Datapack %s has an invalid configucast.json.", path.getNamespace()
                    ),
                    e
                );
            }

        }

    }

    public Map<Identifier, Identifier> loadGates(JsonObject json) {
        Map<Identifier, Identifier> gates = new HashMap<>();
        json.keySet().forEach(key -> gates.put(
            new Identifier(key), new Identifier(json.get(key).getAsString())
        ));
        return gates;
    }

}
