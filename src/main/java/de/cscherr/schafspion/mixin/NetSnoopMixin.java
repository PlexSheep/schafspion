package de.cscherr.schafspion.mixin;

import de.cscherr.schafspion.SchafSpion;
import de.cscherr.schafspion.modules.NetSnoop;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static de.cscherr.schafspion.modules.NetSnoop.Side;

/**
 * Snoop Network Packets of Minecraft by injecting code into minecraft methods
 * Works together with the NetSnoop class (for configuration)
 */
@Mixin(ClientConnection.class)
public abstract class NetSnoopMixin {
    @Unique
    private static final NetSnoop netsnoop = SchafSpion.netsnoop;

    /**
     * Inject to the method that sends packets. Must not be static for some reason
     *
     * @param packet    the Network packet to be sent
     * @param callbacks magical fabric mixin stuff
     * @param ci        magical fabric mixin stuff
     */
    @Inject(method = "sendImmediately", at = @At("HEAD"))
    public void logTXPacket(Packet<?> packet, @Nullable PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
        netsnoop.logPacket(packet, Side.TX);
    }

    /**
     * Inject to the method that receives and handles packets.
     * Must be static for some reason
     *
     * @param packet   the Network packet to be sent
     * @param listener I don't know what this does
     * @param ci       magical fabric mixin stuff
     */
    @Inject(method = "handlePacket", at = @At("HEAD"))
    private static void logRXPacket(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
        netsnoop.logPacket(packet, Side.RX);
    }
}
