package de.cscherr.schafspion.modules;

import de.cscherr.schafspion.SchafSpion;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.network.packet.Packet;

/**
 * Adds the ability to capture network packets
 */
public class NetSnoop extends Module {
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();

    public enum Verbosity {
        Minimal, All
    }

    public final Setting<Boolean> toFile = sgGeneral.add(new BoolSetting.Builder().name("log to file").description("log network packets to files in the .minecraft directory").defaultValue(false).build());
    public final Setting<Boolean> logRX = sgGeneral.add(new BoolSetting.Builder().name("log RX").description("log received").defaultValue(true).build());
    public final Setting<Boolean> logTX = sgGeneral.add(new BoolSetting.Builder().name("log TX").description("log transmitted").defaultValue(true).build());

    public final Setting<Verbosity> verbosity;

    /**
     * The {@code name} parameter should be in kebab-case.
     */
    public NetSnoop() {
        super(SchafSpion.CATEGORY, "NetSnoop", "Sniff network packets");

        EnumSetting.Builder verbosityBuilder= new EnumSetting.Builder();
        verbosityBuilder.name("Verbosity");
        verbosityBuilder.description("How verbose packets should be logged");
        verbosityBuilder.defaultValue(Verbosity.Minimal);
        this.verbosity = sgGeneral.add(verbosityBuilder.build());

    }

    /**
     * True for those packets that should be ignored while logging
     * @param packet
     * @return
     */
    public static boolean ignorePacket(Packet<?> packet) {
        return false;
    }
}
