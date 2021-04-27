package life.steeze.simplehcf;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FactionsFile {
    private static File factionsfile;
    private static FileConfiguration factionsdata;
    public static FileConfiguration getData(){
        return factionsdata;
    }
    public static void loadFactions(){
        factionsfile = new File(Bukkit.getPluginManager().getPlugin("SimplyHCF").getDataFolder(),"factions.yml");
        if(!factionsfile.exists()){
            factionsfile.getParentFile().mkdir();
            try{
                factionsfile.createNewFile();
            } catch(IOException e){
                e.printStackTrace();
            }
        }
        factionsdata = YamlConfiguration.loadConfiguration(factionsfile);

        if(!factionsdata.getKeys(false).isEmpty()){

            for(String key : factionsdata.getKeys(false)){
                Faction loadedFac = Faction.deserialize(factionsdata.getConfigurationSection(key).getValues(false));

                HCFMain.factions.add(loadedFac);
            }
        }

    }

    public static void saveFactions(){
        List<String> savedFacs = new ArrayList<>();
        for(Faction f : HCFMain.factions){

            factionsdata.set(f.getName(), f.serialize());
            savedFacs.add(f.getName());
        }

        try {
            factionsdata.save(factionsfile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(String key : factionsdata.getKeys(false)){
            if(!savedFacs.contains(key)){
                factionsdata.set(key, null);
            }
        }

        try {
            factionsdata.save(factionsfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
