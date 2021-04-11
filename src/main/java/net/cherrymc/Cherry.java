package net.cherrymc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
}
