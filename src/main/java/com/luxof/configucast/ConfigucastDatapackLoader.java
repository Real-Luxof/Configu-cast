package com.luxof.configucast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import static com.luxof.configucast.Configucast.LOGGER;
import static com.luxof.configucast.Configucast.MOD_ID;
import static com.luxof.configucast.Configucast.id;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class ConfigucastDatapackLoader implements SimpleSynchronousResourceReloadListener {

    public static final Map<Identifier, Identifier> playerGates = new HashMap<>();
    public static final Set<Identifier> playerlessDisallowed = new HashSet<>();
    public static final Map<Identifier, List<String>>

    public static final Identifier ID = id("datapack_loader");
    @Override public Identifier getFabricId() { return ID; }

    @Override
    public void reload(ResourceManager manager) {
        playerGates.clear();

        Map<Identifier, Resource> resources = manager.findResources(
            "configucast.json",
            path -> path.getPath().equals("configucast.json") &&
                (
                    !path.getNamespace().equals(MOD_ID) ||
                    FabricLoader.getInstance().isDevelopmentEnvironment()
                )
        );

        for (Identifier path : resources.keySet()) {
            LOGGER.info("Configucast loading: " + path.getNamespace());
            Resource resource = resources.get(path);

            try {

                JsonObject file = JsonParser.parseReader(resource.getReader()).getAsJsonObject();

                JsonObject playerGatesJsonObject = file.has("playergates")
                    ? file.getAsJsonObject("playergates")
                    : new JsonObject();
                var jsonPlayerGates = loadPlayerGates(playerGatesJsonObject);
                jsonPlayerGates.forEach(
                    (action, advancement) -> {
                        if (playerGates.containsKey(action)) throw new RuntimeException(String.format("%s is already gated to another advancement (%s) by another datapack!", action.toString(), playerGates.get(action).toString()));
                    }
                );
                playerGates.putAll(jsonPlayerGates);

                JsonArray playerlessGatesJsonArray = file.has("playerlessgates")
                    ? file.getAsJsonArray("playerlessgates")
                    : new JsonArray();
                playerlessDisallowed.addAll(loadPlayerlessGates(playerlessGatesJsonArray));

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

    public Map<Identifier, Identifier> loadPlayerGates(JsonObject json) {
        Map<Identifier, Identifier> playerGates = new HashMap<>();
        json.keySet().forEach(key -> playerGates.put(
            new Identifier(key), new Identifier(json.get(key).getAsString())
        ));
        return playerGates;
    }

    public List<Identifier> loadPlayerlessGates(JsonArray json) {
        List<Identifier> playerlessGates = new ArrayList<>();
        json.forEach(key -> playerlessGates.add(new Identifier(key.getAsString())));
        return playerlessGates;
    }

}
