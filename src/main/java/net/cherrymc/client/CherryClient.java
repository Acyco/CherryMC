package net.cherrymc.client;

import net.cherrymc.client.config.ClientConfig;
import net.cherrymc.log.CherryLog;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.LoaderException;

import java.io.File;
import java.io.IOException;

/**
 * @author Acyco
 * @create 2021-01-20 22:35
 * @url https://acyco.cn
 */
public class CherryClient {
    public static Minecraft minecraft ;  
    private static CherryClient instance; 
    public static ClientConfig config;
    
    
    public static CherryClient instance() {
        if (null == instance) {
            instance = new CherryClient();
        }
        return instance;
    }
    public void init(Minecraft mc) {
        minecraft = mc;
        config = ClientConfig.load();
        
    }
    
    public File getConfigDir() {
        File configDir = new File(minecraft.mcDataDir, "config");
        try {
            
            return configDir.getCanonicalFile();
        } catch (IOException e) {
            CherryLog.log.error("Failed to resolve config directories: {}",
                    configDir.getAbsolutePath(), e);
            throw new LoaderException(e);
        }
    }
}
