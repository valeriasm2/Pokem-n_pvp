package com.projecte;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;

public class BuildDatabase {

    public static void main(String[] args) {
        AppData db = AppData.getInstance();
        db.connect("./data/pokemon.sqlite");

        System.out.println("\nIniciando los datos de la base de datos:");
        initData(db);

        db.close();
    }

    public static void initData(AppData db) {
        // Eliminar tablas existentes
        String[] tablesToDrop = {
                "ItemReward", "ItemEffect", "BattlePokemon", "Battle",
                "ItemInventory", "Item", "TypeEffectiveness", "PokemonAttack",
                "Attack", "PlayerPokemon", "Pokemon", "GameStats"
        };

        for (String table : tablesToDrop) {
            db.update("DROP TABLE IF EXISTS " + table);
        }

        // Crear tablas
        db.update("""
                CREATE TABLE Pokemon (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    type TEXT NOT NULL,
                    image_path TEXT)
                """);

        db.update("""
                CREATE TABLE PlayerPokemon (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    pokemon_id INTEGER NOT NULL,
                    nickname TEXT,
                    max_hp INTEGER NOT NULL,
                    attack INTEGER NOT NULL,
                    stamina INTEGER NOT NULL,
                    unlocked BOOLEAN DEFAULT 0,
                    FOREIGN KEY (pokemon_id) REFERENCES Pokemon(id))
                """);

        db.update("""
                CREATE TABLE Attack (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    type TEXT NOT NULL,
                    damage INTEGER NOT NULL,
                    stamina_cost INTEGER NOT NULL)
                """);

        db.update("""
                CREATE TABLE PokemonAttack (
                    pokemon_id INTEGER NOT NULL,
                    attack_id INTEGER NOT NULL,
                    PRIMARY KEY (pokemon_id, attack_id),
                    FOREIGN KEY (pokemon_id) REFERENCES Pokemon(id),
                    FOREIGN KEY (attack_id) REFERENCES Attack(id))
                """);

        db.update("""
                CREATE TABLE TypeEffectiveness (
                    attack_type TEXT NOT NULL,
                    target_type TEXT NOT NULL,
                    multiplier REAL NOT NULL,
                    PRIMARY KEY (attack_type, target_type))
                """);

        db.update("""
                CREATE TABLE Item (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    effect_type TEXT NOT NULL,
                    effect_value INTEGER)
                """);

        db.update("""
                CREATE TABLE ItemInventory (
                    item_id INTEGER PRIMARY KEY,
                    quantity INTEGER DEFAULT 0,
                    FOREIGN KEY (item_id) REFERENCES Item(id))
                """);

        db.update("""
                CREATE TABLE GameStats (
                    id INTEGER PRIMARY KEY CHECK (id = 1),
                    total_experience INTEGER DEFAULT 0,
                    battles_played INTEGER DEFAULT 0,
                    max_win_streak INTEGER DEFAULT 0,
                    current_win_streak INTEGER DEFAULT 0)
                """);

        db.update("""
                CREATE TABLE Battle (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date TEXT NOT NULL,
                    map TEXT,
                    winner TEXT CHECK(winner IN ('Player', 'Computer')))
                """);

        db.update("""
                CREATE TABLE BattlePokemon (
                    battle_id INTEGER NOT NULL,
                    is_player BOOLEAN NOT NULL,
                    pokemon_id INTEGER NOT NULL,
                    PRIMARY KEY (battle_id, is_player, pokemon_id),
                    FOREIGN KEY (battle_id) REFERENCES Battle(id),
                    FOREIGN KEY (pokemon_id) REFERENCES PlayerPokemon(id))
                """);

        db.update("""
                CREATE TABLE ItemEffect (
                    player_pokemon_id INTEGER NOT NULL,
                    item_id INTEGER NOT NULL,
                    active BOOLEAN DEFAULT 1,
                    PRIMARY KEY (player_pokemon_id, item_id),
                    FOREIGN KEY (player_pokemon_id) REFERENCES PlayerPokemon(id),
                    FOREIGN KEY (item_id) REFERENCES Item(id))
                """);

        db.update("""
                CREATE TABLE ItemReward (
                    battle_id INTEGER NOT NULL,
                    item_id INTEGER NOT NULL,
                    quantity INTEGER DEFAULT 1,
                    PRIMARY KEY (battle_id, item_id),
                    FOREIGN KEY (battle_id) REFERENCES Battle(id),
                    FOREIGN KEY (item_id) REFERENCES Item(id))
                """);

        // Insertar datos iniciales
        db.update(
                "INSERT INTO GameStats (id, total_experience, battles_played, max_win_streak, current_win_streak) VALUES (1, 0, 0, 0, 0)");

        // Cargar datos desde JSON
        try {
            String content = new String(Files.readAllBytes(Paths.get("./data/pokemon_data.json")),
                    StandardCharsets.UTF_8);
            JSONObject data = new JSONObject(content);

            // Insertar Pokémon
            JSONArray pokemons = data.getJSONArray("Pokemon");
            for (int i = 0; i < pokemons.length(); i++) {
                JSONObject p = pokemons.getJSONObject(i);
                db.update(String.format(
                        "INSERT INTO Pokemon (name, type, image_path) VALUES ('%s', '%s', '%s')",
                        p.getString("name"),
                        p.getString("type"),
                        p.optString("image_path", "")));
                System.out.println("Añadido Pokémon: " + p.getString("name"));
            }

            // Insertar ataques
            JSONArray attacks = data.getJSONArray("Attack");
            for (int i = 0; i < attacks.length(); i++) {
                JSONObject a = attacks.getJSONObject(i);
                db.update(String.format(
                        "INSERT INTO Attack (name, type, damage, stamina_cost) VALUES ('%s', '%s', %d, %d)",
                        a.getString("name"),
                        a.getString("type"),
                        a.getInt("damage"),
                        a.getInt("stamina_cost")));
                System.out.println("Añadido ataque: " + a.getString("name"));
            }

            // Insertar efectividades de tipo - VERSIÓN CORREGIDA
            JSONArray typeEffectiveness = data.getJSONArray("TypeEffectiveness");
            for (int i = 0; i < typeEffectiveness.length(); i++) {
                JSONObject e = typeEffectiveness.getJSONObject(i);
                try {
                    // Manejo seguro del multiplicador
                    Object multiplierValue = e.get("multiplier");
                    double multiplier;

                    if (multiplierValue instanceof Number) {
                        multiplier = ((Number) multiplierValue).doubleValue();
                    } else {
                        // Si viene como String, convertimos adecuadamente
                        String multiplierStr = multiplierValue.toString().replace(",", ".");
                        multiplier = Double.parseDouble(multiplierStr);
                    }

                    // Usamos formato con Locale.US para asegurar punto decimal
                    String sql = String.format(Locale.US,
                            "INSERT INTO TypeEffectiveness (attack_type, target_type, multiplier) VALUES ('%s', '%s', %f)",
                            e.getString("attack_type"),
                            e.getString("target_type"),
                            multiplier);

                    db.update(sql);

                    System.out.printf(Locale.US, "Añadida efectividad: %s vs %s = %.1f%n",
                            e.getString("attack_type"),
                            e.getString("target_type"),
                            multiplier);
                } catch (Exception ex) {
                    System.err.println("Error insertando efectividad de tipo:");
                    System.err.println("Datos: " + e.toString());
                    System.err.println("Error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }

            // Insertar ítems
            JSONArray items = data.getJSONArray("Item");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                db.update(String.format(
                        "INSERT INTO Item (name, effect_type, effect_value) VALUES ('%s', '%s', %d)",
                        item.getString("name"),
                        item.getString("effect_type"),
                        item.getInt("effect_value")));
                System.out.println("Añadido ítem: " + item.getString("name"));
            }

            // Insertar Pokémon iniciales del jugador
            JSONArray playerPokemons = data.getJSONArray("PlayerPokemon");
            for (int i = 0; i < playerPokemons.length(); i++) {
                JSONObject pp = playerPokemons.getJSONObject(i);
                db.update(String.format(
                        "INSERT INTO PlayerPokemon (pokemon_id, nickname, max_hp, attack, stamina, unlocked) VALUES (%d, '%s', %d, %d, %d, %d)",
                        pp.getInt("pokemon_id"),
                        pp.optString("nickname", ""),
                        pp.getInt("max_hp"),
                        pp.getInt("attack"),
                        pp.getInt("stamina"),
                        pp.getBoolean("unlocked") ? 1 : 0));
                System.out.println("Añadido Pokémon del jugador: " + pp.getInt("pokemon_id"));
            }

            // Insertar relaciones Pokémon-Ataque
            JSONArray pokemonAttacks = data.getJSONArray("PokemonAttack");
            for (int i = 0; i < pokemonAttacks.length(); i++) {
                JSONObject pa = pokemonAttacks.getJSONObject(i);
                db.update(String.format(
                        "INSERT INTO PokemonAttack (pokemon_id, attack_id) VALUES (%d, %d)",
                        pa.getInt("pokemon_id"),
                        pa.getInt("attack_id")));
            }

        } catch (IOException e) {
            System.err.println("Error al leer el archivo JSON:");
            e.printStackTrace();
        }
    }
}