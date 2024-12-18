package de.cscherr.schafspion.modules;

import de.cscherr.schafspion.SchafSpion;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.PacketListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Unique;

import java.util.Set;

/**
 * Adds the ability to capture network packets
 */
// disable useless java warnings
@SuppressWarnings({"unchecked", "rawtypes"})
public class NetSnoop extends Module {
    @Unique
    private static final Logger LOG = SchafSpion.LOG;
    private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
    public final Setting<Boolean> toFile = sgGeneral.add(new BoolSetting.Builder().name("log to file").description("log network packets to files in the .minecraft directory").defaultValue(false).build());
    public final Setting<Boolean> logRX = sgGeneral.add(new BoolSetting.Builder().name("log RX").description("log received").defaultValue(true).build());
    public final Setting<Boolean> logTX = sgGeneral.add(new BoolSetting.Builder().name("log TX").description("log transmitted").defaultValue(true).build());
    public final Setting<Verbosity> verbosity;
    private final Setting<Set<Class<? extends Packet<?>>>> relevantPackets = sgGeneral.add(new PacketListSetting.Builder()
        .name("Relevant packets")
        .description("The packets to be logged")
        .build()
    );


    /**
     * Formats the packet description String
     *
     * @param packet the packet in question
     * @return description for that packet
     */
    @Unique
    private String getPacketInfo(Packet<?> packet) {
        String information;
        switch (this.verbosity.get()) {
            case Minimal -> information = packet.getClass().getSimpleName();
            case All -> information = String.format("%s:\n%s", packet.getClass().getSimpleName(), getPacketFields(packet));
            default -> throw new IllegalStateException("Unexpected value: " + this.verbosity.get());
        }
        return information;
    }

    /**
     * Formats additional information depending on packet class
     * @param packet the packet in question
     * @return additional information depending on the packet class
     */
    @Unique
    private static String getPacketFields(Packet<?> packet) {
        String fieldInfo;
        //noinspection SwitchStatementWithTooFewBranches
        switch (packet.getClass().getSimpleName()) {
            case "PositionAndOnGround" -> {
                PlayerMoveC2SPacket pmPacket = (PlayerMoveC2SPacket)packet;
                fieldInfo = String.format("X: %f | Y: %f | Z: %f\n" +
                    "Ground: %b", pmPacket.getX(0), pmPacket.getY(0), pmPacket.getZ(0), pmPacket.isOnGround());
            }
            case "Full" -> {
                PlayerMoveC2SPacket.Full pmPacket = (PlayerMoveC2SPacket.Full) packet;
                fieldInfo = String.format("X: %f | Y: %f | Z: %f\n" +
                    "Ground: %b", pmPacket.getX(0), pmPacket.getY(0), pmPacket.getZ(0), pmPacket.isOnGround());
            }
            default -> fieldInfo = "<None>";
        }
        return fieldInfo;
    }

    public enum Verbosity {
        Minimal, All
    }

    public enum Side {
        RX,TX
    }

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

    /** Should this packet be ignored?
     *
     * @param packet the packet in question
     * @return True for those packets that should be ignored while logging, otherwise false
     */
    public boolean ignorePacket(Packet<?> packet) {
        return !relevantPackets.get().contains(packet.getClass());
    }

    /** Logs a packet
     * @param packet the packet to be logged
     * @param side receiving or transmitting side?
     */
    public void logPacket(Packet<?> packet, Side side) {
        switch (side) {
            case TX:
                if (!logTX.get()) {
                    return;
                }
                break;
            case RX:
                if (!logRX.get()) {
                return;
            }
            break;
        }

        if (isActive() && !ignorePacket(packet)) {
            LOG.info(String.format("%s Package: %s", side, getPacketInfo(packet)));
        }
    }
}
