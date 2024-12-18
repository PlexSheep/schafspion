package de.cscherr.schafspion.modules;

import de.cscherr.schafspion.SchafSpion;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;

/**
 * Adds the ability to capture network packets
 */
public class Netsnoop extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    private final SettingGroup sgRender = this.settings.createGroup("Render");

    /**
     * Example setting.
     * The {@code name} parameter should be in kebab-case.
     * If you want to access the setting from another class, simply make the setting {@code public}, and use
     * {@link meteordevelopment.meteorclient.systems.modules.Modules#get(Class)} to access the {@link Module} object.
     */
    private final Setting<Boolean> to_file = sgGeneral.add(new BoolSetting.Builder().name("log to file").description("log network packets to files in the .minecraft directory").defaultValue(false).build());

    /**
     * The {@code name} parameter should be in kebab-case.
     */
    public Netsnoop() {
        super(SchafSpion.CATEGORY, "Netsnoop", "An example module that highlights the center of the world.");
    }
}
