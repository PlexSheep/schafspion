package de.cscherr.schafspion.mixin;

import com.mojang.logging.LogUtils;
import de.cscherr.schafspion.SchafSpion;
import de.cscherr.schafspion.modules.NetSnoop;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.slf4j.Logger;

/**
 * Example Mixin class.
 * For more resources, visit:
 * <ul>
 * <li><a href="https://fabricmc.net/wiki/tutorial:mixin_introduction">The FabricMC wiki</a></li>
 * <li><a href="https://github.com/SpongePowered/Mixin/wiki">The Mixin wiki</a></li>
 * <li><a href="https://github.com/LlamaLad7/MixinExtras/wiki">The MixinExtras wiki</a></li>
 * <li><a href="https://jenkins.liteloader.com/view/Other/job/Mixin/javadoc/allclasses-noframe.html">The Mixin javadoc</a></li>
 * <li><a href="https://github.com/2xsaiko/mixin-cheatsheet">The Mixin cheatsheet</a></li>
 * </ul>
 */
@Mixin(ClientConnection.class)
public abstract class NetSnoopMixin {
    @Unique
    private static final NetSnoop netsnoop = SchafSpion.netsnoop;
    @Unique
    private static final Logger LOG = LogUtils.getLogger();

    /**
     * Formats the packet description String
     *
     * @param packet the packet in question
     * @return description for that packet
     */
    @Unique
    private static String getPacketInfo(Packet<?> packet) {
        String information;
        switch (netsnoop.verbosity.get()) {
            case Minimal -> information = packet.getClass().getSimpleName();
            case All -> information = String.format("%s:\n%s", packet.getClass().getSimpleName(), getPacketFields(packet));
            default -> throw new IllegalStateException("Unexpected value: " + netsnoop.verbosity.get());
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
            default -> fieldInfo = "<None>";
        }
        return fieldInfo;
    }

    /**
     * Inject to the method that sends packets. Must not be static for some reason
     *
     * @param packet    the Network packet to be sent
     * @param callbacks magical fabric mixin stuff
     * @param ci        magical fabric mixin stuff
     */
    @Inject(method = "sendImmediately", at = @At("HEAD"))
    public void logTXPacket(Packet<?> packet, @Nullable PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
        if (netsnoop.isActive() &&netsnoop.logTX.get() && !NetSnoop.ignorePacket(packet)) {
            LOG.info(String.format("TX Package: %s", getPacketInfo(packet)));
        }
    }

    /**
     * Inject to the method that receives and handles packets.
     * Must be static for some reason
     *
     * @param packet   the Network packet to be sent
     * @param listener i don't know what this does
     * @param ci       magical fabric mixin stuff
     */
    @Inject(method = "handlePacket", at = @At("HEAD"))
    private static void logRXPacket(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        if (netsnoop.isActive() &&netsnoop.logRX.get() && !NetSnoop.ignorePacket(packet)) {
            LOG.info(String.format("RX Package: %s", getPacketInfo(packet)));
        }
    }
}
