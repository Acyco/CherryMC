package net.cherrymc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/**
 * cherrymc
 *
 * @author Acyco
 * @create 2021-01-10 23:31
 * @url https://acyco.cn
 */
public class Cherry {
    
    public static void main(String[] args) {
        System.out.println("Hello CherryMC!!!!!!");
    }
    
    
    public static String getVersion() {
        InputStream is = Cherry.class.getResourceAsStream("/version");
        BufferedReader read = new BufferedReader(new InputStreamReader(is));
        String version = null;
        try {
             version = read.readLine();
        } catch (IOException e) {
            version = "0.0.1";
        }
        return version;
    }
    
    
    public static boolean ignoreOptifineMod(Attributes mfAttributes) {
        if (mfAttributes == null) return false;
        
        String tc = mfAttributes.getValue("TweakClass");
        
        if (tc !=  null && tc.equals("optifine.OptiFineForgeTweaker"))
            return true;
        return false;
    }

    public static boolean ignoreOptifineMod(File file) {
        JarFile  jar = null;
        try {
            jar = new JarFile(file);
            Attributes mfAttributes = jar.getManifest() == null ? null : jar.getManifest().getMainAttributes();
           return ignoreOptifineMod(mfAttributes);
        } catch (IOException e) {
            return false;
        }
       
    }
}
