package cn.ksmcbrigade.al;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod(AutoLogin.MOD_ID)
@Mod.EventBusSubscriber(modid = AutoLogin.MOD_ID,value = Dist.CLIENT)
public class AutoLogin {

    public static final String MOD_ID = "al";

    public static final Config config;

    static {
        try {
            config = new Config();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public AutoLogin() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterClientCommandsEvent event){
        event.getDispatcher().register(Commands.literal("al-config").executes(context -> {
            context.getSource().sendSystemMessage(Component.literal("Login wait second: ").append(String.valueOf(config.wait)));
            context.getSource().sendSystemMessage(Component.literal("record servers: "));
            for(String key:config.data.keySet()){
                context.getSource().sendSystemMessage(Component.literal(key).append(": ").append(config.data.get(key).getAsString()));
            }
            return 0;
        }).then(Commands.argument("wait", IntegerArgumentType.integer(0)).executes(context -> {
            config.wait = IntegerArgumentType.getInteger(context,"wait");
            try {
                config.save();
                context.getSource().sendSystemMessage(CommonComponents.GUI_DONE);
                return 0;
            } catch (IOException e) {
                e.printStackTrace();
                return 1;
            }
        }).then(Commands.argument("ip", StringArgumentType.string()).then(Commands.argument("command",StringArgumentType.string()).executes(context -> {
            config.wait = IntegerArgumentType.getInteger(context,"wait");

            String ip = StringArgumentType.getString(context,"ip");
            String command = StringArgumentType.getString(context,"command");

            if(!command.isEmpty()){
                config.data.addProperty(ip,command);
            }

            try {
                config.save();
                context.getSource().sendSystemMessage(CommonComponents.GUI_DONE);
                return 0;
            } catch (IOException e) {
                e.printStackTrace();
                return 1;
            }
        })))));

        event.getDispatcher().register(Commands.literal("al-reload").executes(context -> {
            try {
                config.reload();
                context.getSource().sendSystemMessage(CommonComponents.GUI_DONE);
                return 0;
            } catch (IOException e) {
                e.printStackTrace();
                return 1;
            }
        }));
    }

    @SubscribeEvent
    public static void onJoinWorld(EntityJoinLevelEvent event){
        new Thread(()-> {
            try {
                Thread.sleep((long) (AutoLogin.config.wait*1000F));

                Minecraft MC = Minecraft.getInstance();

                if(MC.player==null) return;
                if(MC.player.getId()!=event.getEntity().getId()) return;
                if(MC.isSingleplayer()) return;
                if(MC.getConnection()==null) return;
                if(MC.getConnection().getServerData()==null) return;
                String ip = MC.getConnection().getServerData().ip;
                if(!config.data.has(ip)) return;

                MC.getConnection().sendCommand(config.data.get(ip).getAsString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
