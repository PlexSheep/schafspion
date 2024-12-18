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
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Field;

public class SchafSpion extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final String NAME = "Schafspion";
    public static final Category CATEGORY = new Category(NAME);
    public static final HudGroup HUD_GROUP = new HudGroup(NAME);
    public static NetSnoop netsnoop = new NetSnoop();

    private static final MappingResolver MAPPINGS = FabricLoader.getInstance().getMappingResolver();

    /**
     * Gets the mapped name for a class using Yarn mappings
     */
    @Unique
    public static String getMappedClassName(Class<?> clazz) {
        try {
            // Try to map from intermediary to named (Yarn)
            String intermediaryName = clazz.getName();
            String mappedName = MAPPINGS.mapClassName("named", intermediaryName);

            // Get the simple name
            String simpleName = mappedName.substring(mappedName.lastIndexOf('.') + 1);

            // Remove inner class notation if present
            if (simpleName.contains("$")) {
                simpleName = simpleName.substring(0, simpleName.indexOf('$'));
            }

            LOG.debug("Mapped {} to {}", intermediaryName, simpleName);
            return simpleName;
        } catch (Exception e) {
            LOG.debug("Failed to map class name: {}", e.getMessage());
            return clazz.getSimpleName();
        }
    }

    /**
     * Get mapped name for a field using Yarn mappings
     */
    @Unique
    public static String getMappedFieldName(Class<?> clazz, String fieldName, String descriptor) {
        try {
            String className = clazz.getName();

            // Try to map using named (Yarn) mappings
            String mappedName = MAPPINGS.mapFieldName("named", className, fieldName, descriptor);
            if (!mappedName.equals(fieldName)) {
                LOG.debug("Mapped field {} to {} using Yarn mappings", fieldName, mappedName);
                return mappedName;
            }

        } catch (Exception e) {
            LOG.warn("Failed to map field name: {}", fieldName);
        }

        return fieldName;
    }

    // Add this helper method to print mappings for debugging
    @Unique
    public static void debugPrintMappings(Class<?> clazz) {
        LOG.info("Debugging mappings for class: " + clazz.getName());
        try {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                String descriptor = Type.getDescriptor(field.getType());
                LOG.info("Field: {} ({})", field.getName(), descriptor);
                for (String namespace : MAPPINGS.getNamespaces()) {
                    try {
                        String mapped = MAPPINGS.mapFieldName(namespace, clazz.getName(), field.getName(), descriptor);
                        LOG.info("  {} mapping: {}", namespace, mapped);
                    } catch (Exception e) {
                        LOG.info("  {} mapping failed: {}", namespace, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to debug print mappings", e);
        }
    }


    @Override
    public void onInitialize() {
        LOG.info("Initializing SchafSpion Addon");
        LOG.info("Available mapping namespaces: " + String.join(", ", MAPPINGS.getNamespaces()));

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
