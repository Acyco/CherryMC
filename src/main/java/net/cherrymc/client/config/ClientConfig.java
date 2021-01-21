package net.cherrymc.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.cherrymc.client.CherryClient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;

/**
 * @author Acyco
 * @create 2021-01-20 23:00
 * @url https://acyco.cn
 */
public class ClientConfig {
    private static ClientConfig config;
    protected static File configFile = new File(CherryClient.instance().getConfigDir(), "cherry/config.json");
    private static final Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.PROTECTED,Modifier.PRIVATE).setPrettyPrinting().create();

    public static boolean hideVersion = false;
    
    private ClientConfig() {
        
    }
    
    public static ClientConfig load() {
        config = new ClientConfig();
        if (!configFile.getParentFile().exists()) {
            configFile.getParentFile().mkdirs();
        }
        try {
            loadConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
        
    }
    private static void loadConfig() throws IOException {
        if (configFile.exists()) {
            try (FileReader configReader = new FileReader(configFile)){
             config =  gson.fromJson(configReader, ClientConfig.class);
            }
        }else{
            try (FileWriter writer = new FileWriter(configFile)){
                gson.toJson(config,ClientConfig.class, writer);
            }
        }
    }
    
    public static void save() {
        System.out.println("hide" +hideVersion);
        try (FileWriter writer = new FileWriter(configFile)){
            gson.toJson(config,ClientConfig.class, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   
}
