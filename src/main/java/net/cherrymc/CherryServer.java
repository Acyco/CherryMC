package net.cherrymc;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

/**
 * @author Acyco
 * @create 2021-01-15 03:55
 * @url https://acyco.cn
 */
public class CherryServer {
    public static  MinecraftServer MINECRAFT_SERVER ;
    public static void init(MinecraftServer minecraftServer) {
        MINECRAFT_SERVER = minecraftServer;       
    }
}
