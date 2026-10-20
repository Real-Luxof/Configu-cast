package com.luxof.configucast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.luxof.configucast.meth.EquationParser;

import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import dev.architectury.platform.Platform;

import static com.luxof.configucast.Configucast.LOGGER;
import static com.luxof.configucast.Configucast.MOD_ID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class ConfigucastDatapackLoader {

    public static final Map<Identifier, Identifier> playerGates = new HashMap<>();
    public static final Set<Identifier> playerlessDisallowed = new HashSet<>();

    public static void reload(ResourceManager manager) {
        playerGates.clear();
        playerlessDisallowed.clear();
        EquationParser.costFormulae.clear();
        PatternRedirectsStorer.clear();

        LOGGER.info("Configuring your casts...");

        manager.findResources(
            "",
            path -> {
            return path.getPath().equals("configucast.json") &&
                (
                    !path.getNamespace().equals(MOD_ID) ||
                    Platform.isDevelopmentEnvironment()
                );
            }
        ).forEach((id, rec) -> {
            LOGGER.info("Configu-cast loading: " + id.toString());
            try {
                ConfigucastDatapackLoader.load(
                    JsonParser.parseReader(rec.getReader()).getAsJsonObject()
                );
            } catch (Exception e) {
                LOGGER.error(
                    "Datapack " + id.getNamespace() + " has an invalid configucast.json.",
                    e
                );
            }
        });
    }

    public static void load(JsonObject file) throws Exception {

        JsonObject playerGatesJsonObject = file.has("playergates")
            ? file.getAsJsonObject("playergates")
            : new JsonObject();
        var jsonPlayerGates = loadPlayerGates(playerGatesJsonObject);
        jsonPlayerGates.forEach(
            (action, advancement) -> {
                if (playerGates.containsKey(action)) throw new RuntimeException(String.format("%s is already gated to another advancement (%s) by another datapack!", action.toString(), playerGates.get(action).toString()));
            }
        );

        JsonArray playerlessGatesJsonArray = file.has("playerlessgates")
            ? file.getAsJsonArray("playerlessgates")
            : new JsonArray();

        JsonObject costsJsonObject = file.has("costs")
            ? file.getAsJsonObject("costs")
            : new JsonObject();
        EquationParser.costFormulae.putAll(
            EquationParser.loadMathEquationsFromJson(costsJsonObject)
        );
        
        loadPatternRedirects(file.has("pattern_redirects")
            ? file.getAsJsonObject("pattern_redirects")
            : new JsonObject()
        );

        // commit after all error-catching is done
        playerGates.putAll(jsonPlayerGates);
        playerlessDisallowed.addAll(loadPlayerlessGates(playerlessGatesJsonArray));
    }

    public static Map<Identifier, Identifier> loadPlayerGates(JsonObject json) {
        Map<Identifier, Identifier> playerGates = new HashMap<>();
        json.asMap().forEach((actionId, advId) -> playerGates.put(
            new Identifier(actionId),
            new Identifier(advId.getAsString()))
        );
        return playerGates;
    }

    public static List<Identifier> loadPlayerlessGates(JsonArray json) {
        List<Identifier> playerlessGates = new ArrayList<>();
        json.forEach(key -> playerlessGates.add(new Identifier(key.getAsString())));
        return playerlessGates;
    }

    public static void loadPatternRedirects(JsonObject json) throws RuntimeException, JsonParseException {
        json.asMap().forEach((actionId, pat) -> {
            JsonObject pattern = pat.getAsJsonObject();

            PatternRedirectsStorer.put(
                HexPattern.fromAngles(
                    pattern.get("stroke_order").getAsString(),
                    switch (pattern.get("start_dir").getAsString()) {
                        case "ne" -> HexDir.NORTH_EAST;
                        case "nw" -> HexDir.NORTH_WEST;
                        case "e" -> HexDir.EAST;
                        case "w" -> HexDir.WEST;
                        case "se" -> HexDir.SOUTH_EAST;
                        case "sw" -> HexDir.SOUTH_WEST;
                        default -> throw new JsonParseException("start_dir was not in [ne, nw, e, w, se, sw]!");
                    }
                ),
                new Identifier(actionId)
            );
        });
    }

}
