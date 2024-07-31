package cn.ksmcbrigade.al.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

import static cn.ksmcbrigade.al.AutoLogin.config;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Shadow public abstract void close();

    @Inject(method = {"send(Lnet/minecraft/network/protocol/Packet;)V"},at = @At("HEAD"))
    public void send(Packet<?> p_104956_, CallbackInfo ci) throws IOException {

        String con = "";
        if(p_104956_ instanceof ServerboundChatPacket cp){
            con = cp.message();
        }
        if(p_104956_ instanceof ServerboundChatCommandPacket cp){
            con = cp.command();
        }

        if(con.isEmpty()) return;

        Minecraft MC = Minecraft.getInstance();

        if(MC.isSingleplayer()) return;
        if(MC.getConnection()==null) return;
        if(MC.getConnection().getServerData()==null) return;
        if(config.data.has(MC.getConnection().getServerData().ip)) return;

        String context = con;
        String command = "";

        if(context.toLowerCase().startsWith("login") || context.toLowerCase().startsWith("l ")){
            command = context;
        }
        if(context.toLowerCase().startsWith("register") || context.toLowerCase().startsWith("reg ")){
            try {
                command = "login " + context.split(" ")[1];
            }
            catch (Exception e){
                e.printStackTrace();
                if(MC.player!=null) MC.player.sendSystemMessage(Component.nullToEmpty("Can't record the first time login command: "+e.getMessage()));
            }
        }

        if(!command.isEmpty()){
            config.data.addProperty(MC.getConnection().getServerData().ip,command);
            config.save();
        }
    }
}
