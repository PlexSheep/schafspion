package de.cscherr.schafspion;

import de.cscherr.schafspion.commands.CommandExample;
import de.cscherr.schafspion.hud.HudExample;
import de.cscherr.schafspion.modules.ModuleExample;
import com.mojang.logging.LogUtils;
import de.cscherr.schafspion.modules.NetSnoop;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class SchafSpion extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final String NAME = "Schafspion";
    public static final Category CATEGORY = new Category(NAME);
    public static final HudGroup HUD_GROUP = new HudGroup(NAME);
    public static NetSnoop netsnoop = new NetSnoop();

    private static final de.cscherr.schafspion.MappingLoader MAPPINGS = new MappingLoader();

    /**
     * Gets the mapped name for a class
     */
    public static String getMappedClassName(Class<?> clazz) {
        return MAPPINGS.getMappedClassName(clazz.getName());
    }

    /**
     * Get mapped name for a field
     */
    public static String getMappedFieldName(Class<?> clazz, String fieldName) {
        return MAPPINGS.getMappedFieldName(clazz.getName(), fieldName);
    }

    @Override
    public void onInitialize() {
        LOG.info("Initializing SchafSpion Addon");

        // Load mappings
        MAPPINGS.loadMappings("mappings.tiny");
        LOG.info("Test mapping - class_2775 maps to: " +
            MAPPINGS.getMappedClassName("net/minecraft/class_2775"));

        // Modules
        Modules.get().add(new ModuleExample());
        Modules.get().add(netsnoop);

        // Commands
        Commands.add(new CommandExample());

        // HUD
        Hud.get().register(HudExample.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "de.cscherr.schafspion";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("PlexSheep", "schafspion");
    }
}
