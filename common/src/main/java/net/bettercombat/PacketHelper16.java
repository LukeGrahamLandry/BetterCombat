package net.bettercombat;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

import java.io.IOException;

public class PacketHelper16 {
    // public PlayerInteractEntityC2SPacket(Entity target, boolean playerSneaking) only exists on the client in 1.16
    public static PlayerInteractEntityC2SPacket createPlayerInteractEntityC2SPacket(Entity entity, boolean sneaking) {
        PlayerInteractEntityC2SPacket packet = new PlayerInteractEntityC2SPacket();
        int entityId = entity.getEntityId();
        PlayerInteractEntityC2SPacket.InteractionType type = PlayerInteractEntityC2SPacket.InteractionType.ATTACK;

        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(entityId);
        buf.writeEnumConstant(type);
        buf.writeBoolean(sneaking);

        try {
            packet.read(buf);
        } catch (IOException e){
            e.printStackTrace();
        }

        return packet;
    }
}
