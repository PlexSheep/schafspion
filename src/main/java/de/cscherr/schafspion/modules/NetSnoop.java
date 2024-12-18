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

import java.lang.reflect.Field;
import java.util.Arrays;
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
     */
    @Unique
    private String getPacketInfo(Packet<?> packet) {
        String mappedName = SchafSpion.getMappedClassName(packet.getClass());

        switch (this.verbosity.get()) {
            case Minimal -> {
                return mappedName;
            }
            case All -> {
                return String.format("%s:\n%s", mappedName, getPacketFields(packet));
            }
            default -> throw new IllegalStateException("Unexpected value: " + this.verbosity.get());
        }
    }

    /**
     * Formats field information
     */
    @Unique
    public String getPacketFields(Packet<?> packet) {
        StringBuilder fieldInfo = new StringBuilder();

        try {
            // Handle PlayerMoveC2SPacket specially
            if (packet instanceof PlayerMoveC2SPacket movePacket) {
                fieldInfo.append(String.format("position: %.2f, %.2f, %.2f%n",
                    movePacket.getX(0),
                    movePacket.getY(0),
                    movePacket.getZ(0)));
                fieldInfo.append(String.format("onGround: %b", movePacket.isOnGround()));

                if (packet instanceof PlayerMoveC2SPacket.Full fullMovePacket) {
                    fieldInfo.append(String.format("%nyaw: %.2f, pitch: %.2f",
                        fullMovePacket.getYaw(0),
                        fullMovePacket.getPitch(0)));
                }
                return fieldInfo.toString();
            }

            // For other packets, use reflection with mapped names
            Field[] fields = packet.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (field.getName().startsWith("$")) continue; // Skip synthetic fields

                field.setAccessible(true);
                String mappedFieldName = SchafSpion.getMappedFieldName(
                    packet.getClass(),
                    field.getName()
                );

                Object value = field.get(packet);
                if (value != null) {
                    String formattedValue = formatFieldValue(value);
                    fieldInfo.append(String.format("%s: %s%n", mappedFieldName, formattedValue));
                }
            }
        } catch (Exception e) {
            LOG.error("Error getting packet fields", e);
            return "<Error getting fields>";
        }

        return !fieldInfo.isEmpty() ? fieldInfo.toString().trim() : "<None>";
    }

    /**
     * Format field values in a readable way
     */
    @Unique
    private static String formatFieldValue(Object value) {
        if (value == null) return "null";

        // Handle arrays
        if (value.getClass().isArray()) {
            switch (value) {
                case Object[] objects -> {
                    return Arrays.toString(objects);
                }
                case int[] ints -> {
                    return Arrays.toString(ints);
                }
                case byte[] bytes -> {
                    return Arrays.toString(bytes);
                }
                default -> {
                }
            }
            // Add other array types as needed
        }

        // For other types, just use toString
        return value.toString();
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
