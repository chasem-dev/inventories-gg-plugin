package gg.inventories.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;

public class Locale {

    HashMap<String, String> translationMap = new HashMap<>();

    public Locale(Plugin plugin) {
        File languageDirectory = new File(plugin.getDataFolder(), "languages/");
        File defaultLanguageFile = new File(plugin.getDataFolder(), "languages/en_us.yml");
        if (!languageDirectory.isDirectory()) {
            languageDirectory.mkdir();
            try {
                InputStream stream = plugin.getResource("languages/en_us.json");
                FileUtils.copyInputStreamToFile(stream, defaultLanguageFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (plugin.getConfig().getString("locale") != null && plugin.getConfig().getString("locale").equals("en_us")) {
            FileConfiguration translations = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "languages/" + plugin.getConfig().getString("locale") + ".yml"));
            for (String translation : translations.getKeys(false)) {
                translationMap.put(translation, translations.getString(translation));
            }
        } else {
            try{
                Scanner scanner = new Scanner(defaultLanguageFile);
                while(scanner.hasNextLine()){
                    String line = scanner.nextLine();
                    String key = line.split(":")[0];
                    String value = line.split(":")[1];
                    translationMap.put(key,value);
                }

            }catch (Exception exc){

            }
        }

        System.out.println(translationMap.size() + " TRANSLATIONS");
    }

    public String get(String path){
//        System.out.println("Locale: " + path);

        return translationMap.getOrDefault(path, "Not Found.");
    }

}
