package life.steeze.simplehcf;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.List;

public class HCFMain extends JavaPlugin implements CommandExecutor {


    static Material[] minerKit = {Material.IRON_BOOTS, Material.IRON_LEGGINGS, Material.IRON_CHESTPLATE, Material.IRON_HELMET};
    static Material[] archerKit = {Material.LEATHER_BOOTS, Material.LEATHER_LEGGINGS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET};
    static Material[] bardKit = {Material.GOLDEN_BOOTS, Material.GOLDEN_LEGGINGS, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_HELMET};

    static String isPlayerWearingKit(Player p){
        Material[] armor = new Material[4];
        int n = 0;
        for(ItemStack i : p.getInventory().getArmorContents()){
            if(i == null){
                return null;
            }
            armor[n] = i.getType();
            n++;
        }
        if(armor[0].equals(Material.IRON_BOOTS)){
            for(int i = 1; i < 4; i++){
                if(!armor[i].equals(minerKit[i])) return null;
            }
            return "miner";
        }
        if(armor[0].equals(Material.LEATHER_BOOTS)){
            for(int i = 1; i < 4; i++){
                if(!armor[i].equals(archerKit[i])) return null;
            }
            return "archer";
        }
        if(armor[0].equals(Material.GOLDEN_BOOTS)){
            for(int i = 1; i < 4; i++){
                if(!armor[i].equals(bardKit[i])) return null;
            }
            return "bard";
        }
        return null;
    }

    public static List<Faction> factions = new ArrayList<>();
    public static Faction getFacByName(String name){
        for(Faction f : factions){
            if(f.getName().equalsIgnoreCase(name)){
                return f;
            }
        }
        return null;
    }

    public static HashMap<Player, Faction> Fplayers = new HashMap<>(), invites = new HashMap<>();
    public static ArrayList<Selection> positions = new ArrayList<>();




    public FCommand fCommand = new FCommand();
    public PosCommands posCommands = new PosCommands();
    public EventHandling eventHandling = new EventHandling();

    public static String chatFormat,noTeamFormat;
    public static int minimumClaimWidth, maximumClaimCornerDistance;
    public static int maxDescriptionLength;
    public static boolean formatChat, mobSpawnInClaim, usingKits;

    public static Plugin inst;
    @Override
    public void onEnable(){
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        this.getCommand("faction").setExecutor(fCommand);
        this.getCommand("pos1").setExecutor(posCommands);
        this.getCommand("pos2").setExecutor(posCommands);
        inst = this;
        getServer().getPluginManager().registerEvents(eventHandling, inst);
        ConfigurationSerialization.registerClass(Faction.class);
        ConfigurationSerialization.registerClass(Claim.class);

        FactionsFile.loadFactions();

        ColorGUI.initGui();
        formatChat = getConfig().getBoolean("format-chat");
        chatFormat = getConfig().getString("formatted-chat");
        noTeamFormat = getConfig().getString("formatted-chat-no-team-found-for-player");
        minimumClaimWidth = getConfig().getInt("minimumClaimWidth");
        maximumClaimCornerDistance = getConfig().getInt("maximumClaimCornerDistance");
        maxDescriptionLength = getConfig().getInt("maxDescriptionLength");
        mobSpawnInClaim = getConfig().getBoolean("aggressiveMobSpawningInClaims");
        usingKits = getConfig().getBoolean("use-kits");
        //In case of server reload add online players to Fplayers
        for(Player p : Bukkit.getOnlinePlayers()){
            for(Faction f : factions){
                if(f.getMembers().contains(p.getUniqueId()) || f.getLeader().equals(p.getUniqueId())){
                    HCFMain.Fplayers.put(p, f);
                }
            }
        }
        new BukkitRunnable(){
            @Override
            public void run() {
                for(Faction f : factions){
                    f.regen();
                }
            }
        }.runTaskTimer(inst, 20, getConfig().getLong("dtr-regen"));

        if(usingKits){
            ArrayList<Player> bards = new ArrayList<>();


            new BukkitRunnable(){
                @Override
                public void run() {
                    for(Player p : Bukkit.getOnlinePlayers()){
                        bards.clear();
                        if(isPlayerWearingKit(p) == null) return;
                        if(isPlayerWearingKit(p).equals("miner")){
                            p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 160, 1));
                            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 160, 0));
                        }
                        if(isPlayerWearingKit(p).equals("archer")){
                            p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 160, 1));
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 160, 1));
                        }
                        if(isPlayerWearingKit(p).equals("bard")){
                            bards.add(p);
                            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 160, 1));
                            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 160, 0));
                            p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 120, 0));
                            p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 160, 1));
                        }
                    }
                }
            }.runTaskTimer(inst, 60, 100);

            //BARD ABILITIES TIMER
            new BukkitRunnable(){
                @Override
                public void run() {
                    for(Player p : bards){
                        if(!Fplayers.containsKey(p)) return;
                        Material m = p.getItemInHand().getType();
                        if(m.equals(Material.SUGAR)){
                            Fplayers.get(p).applyBardAbility(PotionEffectType.SPEED, 1);
                        }
                        if(m.equals(Material.BLAZE_POWDER)){
                            Fplayers.get(p).applyBardAbility(PotionEffectType.INCREASE_DAMAGE, 0);
                        }
                    }
                }
            }.runTaskTimer(inst, 60, 10);


        }
    }
    @Override
    public void onDisable(){
        FactionsFile.saveFactions();
    }


}
