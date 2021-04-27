package life.steeze.simplehcf;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

public class Faction implements ConfigurationSerializable {
    private final String name;

    public String getName() {
        return this.name;
    }

    private Location home;

    private String description;
    private UUID leader;

    public UUID getLeader() {
        return this.leader;
    }

    public ChatColor color = ChatColor.WHITE;

    private String leaderStr;
    private ArrayList<UUID> members = new ArrayList<>();

    public ArrayList<UUID> getMembers() {
        return this.members;
    }

    private ArrayList<String> membersStr = new ArrayList<>();
    private Claim claim;

    public Claim getClaim() {
        return this.claim;
    }

    private int dtr;

    public Player[] getOnlineMembers(){
        ArrayList<Player> members = new ArrayList<>();
        if(Bukkit.getPlayer(this.leader) != null) members.add(Bukkit.getPlayer(this.leader));
        for(UUID member : this.members){
            if(Bukkit.getPlayer(member) != null) members.add(Bukkit.getPlayer(member));
        }
        Player[] online = new Player[members.size()];
        members.toArray(online);
        return online;
    }

    public void applyBardAbility(PotionEffectType effect, int amplifier){
        for(Player p : getOnlineMembers()){
            p.addPotionEffect(new PotionEffect(effect, 20, amplifier));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Faction faction = (Faction) o;
        return name.equals(faction.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public Faction(String name, Player p) {
        this.name = name;
        this.leader = p.getUniqueId();
        this.dtr = 1;
        HCFMain.Fplayers.put(p, this);
    }

    public Faction(Map<String, Object> map) {
        this.name = (String) map.get("name");
        this.leader = UUID.fromString((String) map.get("leader"));
        for (String m : (ArrayList<String>) map.get("members")) {
            this.members.add(UUID.fromString(m));
        }
        this.description = (String) map.get("description");
        this.dtr = (int) map.get("dtr");
        this.color = ChatColor.getByChar((String) map.get("color"));
        this.home = (Location) map.get("home");
        if (map.get("claim") != null) this.claim = Claim.deserialize((MemorySection) map.get("claim"));
        else this.claim = null;

    }

    public void setDescription(String s) {
        this.description = s;
    }

    public void setLeader(UUID id) {
        this.members.add(this.leader);
        this.leader = id;
        for (UUID uuid : this.members) {
            if (Bukkit.getOfflinePlayer(uuid).isOnline()) {
                Player p = Bukkit.getPlayer(uuid);
                p.sendMessage(ChatColor.YELLOW + Bukkit.getPlayer(this.leader).getDisplayName() + ChatColor.YELLOW + " is now the leader of the faction!");
            }
        }
        if (Bukkit.getOfflinePlayer(this.leader).isOnline())
            Bukkit.getPlayer(this.leader).sendMessage(ChatColor.YELLOW + "You are now the leader of your faction!");
    }

    public void showInfo(Player p) {
        p.sendMessage(ChatColor.YELLOW + "----==== Faction info: " + this.color + this.name + ChatColor.YELLOW + " ====----");
        p.sendMessage(ChatColor.YELLOW + "Description: " + ChatColor.WHITE + this.description);
        p.sendMessage(ChatColor.YELLOW + "DTR: " + ChatColor.WHITE + this.dtr + (this.dtr <= 0 ? ChatColor.RED + "| RAIDABLE" : ""));
        p.sendMessage(ChatColor.YELLOW + "Leader: " + ChatColor.WHITE + (Bukkit.getOfflinePlayer(this.leader).isOnline() ? Bukkit.getPlayer(this.leader).getName() : Bukkit.getOfflinePlayer(this.leader).getName()));
        p.sendMessage(ChatColor.YELLOW + "Members:");
        for (UUID id : this.members) {
            String name;
            if (Bukkit.getOfflinePlayer(id).isOnline()) {
                name = ChatColor.YELLOW + Bukkit.getPlayer(id).getName();
            } else {
                name = ChatColor.RED + Bukkit.getOfflinePlayer(id).getName();
            }
            p.sendMessage(name);
        }
        if (this.hasClaim()) {
            p.sendMessage(ChatColor.GREEN + "Claim start: " + this.claim.start());
            p.sendMessage(ChatColor.GREEN + "Claim end: " + this.claim.end());
            if (this.claim.getBounds().contains(p.getLocation().toVector())) {
                this.claim.showBounds(p);
            }
        }
    }

    public boolean hasPlayer(Player p) {
        if (this.members.contains(p.getUniqueId())) {
            return true;
        }
        return p.getUniqueId().equals(this.leader);
    }

    public void disband() {
        HCFMain.Fplayers.remove(Bukkit.getPlayer(this.leader));
        for (UUID id : this.getMembers()) {
            HCFMain.Fplayers.remove(Bukkit.getPlayer(id));
        }
        HCFMain.factions.remove(this);
        for (Map.Entry<Player, Faction> entry : HCFMain.invites.entrySet()) {
            if (entry.getValue().equals(this)) {
                HCFMain.invites.remove(entry.getKey());
            }
        }
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Faction " + ChatColor.WHITE + this.name + ChatColor.YELLOW + " has been disbanded!");
    }

    public void setClaim(Claim c) {
        this.claim = c;
    }

    public boolean hasClaim() {
        if (this.claim == null) return false;
        return true;
    }

    public void regen() {
        if (this.dtr < this.members.size() + 1) {
            this.dtr += 1;
        }
    }

    public int getDtr() {
        return this.dtr;
    }

    public void loseDtr(){
            this.dtr--;

    }

    public void setHome(Player p) {
        if (this.claim == null) {
            p.sendMessage(ChatColor.RED + "You need to have a claim!");
            return;
        }
        if (this.claim.containsLocation(p.getLocation())) {
            this.home = p.getLocation();
            p.sendMessage(ChatColor.YELLOW + "Setting home...");
            p.sendMessage(ChatColor.YELLOW + "Done!");
        } else {
            p.sendMessage(ChatColor.RED + "Your home must be within your claim");
        }

    }

    public void tpHome(Player p) {
        if (this.home != null) {
            p.teleport(this.home);
        } else {
            p.sendMessage(ChatColor.RED + "Your faction does not have a home.");
        }
    }

    public void removePlayer(UUID p) {
        this.members.remove(p);
        HCFMain.Fplayers.remove(Bukkit.getPlayer(p));
        for (UUID id : this.members) {
            if(Bukkit.getOfflinePlayer(id).isOnline()) {
                Player player = Bukkit.getPlayer(id);
                player.sendMessage(ChatColor.RED + Bukkit.getPlayer(p).getName() + " has left/been kicked from the faction!");
            }
        }
        if(Bukkit.getOfflinePlayer(this.leader).isOnline()){
            Bukkit.getPlayer(this.leader).sendMessage(ChatColor.RED + Bukkit.getPlayer(p).getName() + " has left/been kicked from the faction!");
        }
    }

    public void addPlayer(UUID p) {
        this.members.add(p);
        HCFMain.Fplayers.put(Bukkit.getPlayer(p), this);
        for (UUID id : this.members) {
            if(Bukkit.getOfflinePlayer(id).isOnline()) {
                Player player = Bukkit.getPlayer(id);
                player.sendMessage(ChatColor.YELLOW + Bukkit.getPlayer(p).getName() + " has joined the faction!");
            }
        }
        if(Bukkit.getOfflinePlayer(this.leader).isOnline()){
            Bukkit.getPlayer(this.leader).sendMessage(ChatColor.YELLOW + Bukkit.getPlayer(p).getName() + " has joined the faction!");
        }
    }


    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();

        this.leaderStr = this.leader.toString();
        for (UUID i : this.members) {
            this.membersStr.add(i.toString());
        }
        map.put("name", this.name);
        map.put("color", Character.toString(this.color.getChar()));
        map.put("leader", this.leaderStr);
        map.put("members", this.membersStr);
        map.put("description", this.description);
        map.put("dtr", this.dtr);
        map.put("home", this.home);
        if (this.claim != null) map.put("claim", this.claim.serialize());
        else map.put("claim", null);

        return map;
    }

    public static Faction deserialize(Map<String, Object> map) {
        return new Faction(map);
    }

}
