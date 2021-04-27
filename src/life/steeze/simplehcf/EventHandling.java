package life.steeze.simplehcf;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class EventHandling implements Listener {


    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        for (Faction f : HCFMain.factions) {
            if (f.hasPlayer(e.getPlayer())) {
                HCFMain.Fplayers.put(e.getPlayer(), f);
                break;
            }
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        HCFMain.Fplayers.remove(e.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (HCFMain.Fplayers.containsKey(e.getEntity())) {
            Faction f = HCFMain.Fplayers.get(e.getEntity());
            f.loseDtr();
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {

        if (!e.isCancelled()) {

            if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
                Player a = (Player) e.getEntity(), b = (Player) e.getDamager();
                if (b.hasPermission("hcf.admin")) {
                    return;
                }
                if (HCFMain.Fplayers.containsKey(a) && HCFMain.Fplayers.containsKey(b)) {
                    if (HCFMain.Fplayers.get(a).equals(HCFMain.Fplayers.get(b))) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void dropItem(PlayerDropItemEvent e){
        if(ClaimWand.isWand(e.getItemDrop().getItemStack())){
            e.getItemDrop().remove();
        }
    }

    @EventHandler
    public void armorStandChange(PlayerArmorStandManipulateEvent e) {
        if (e.getPlayer().hasPermission("hcf.admin")) {

            return;
        }
        if (!e.isCancelled()) {
            for (Faction f : HCFMain.factions) {
                if (f.hasClaim()) {
                    if (f.getClaim().containsLocation(e.getRightClicked().getLocation())) {
                        if (HCFMain.Fplayers.get(e.getPlayer()) != f && f.getDtr() > 0) {
                            e.getPlayer().sendMessage("Land is claimed by " + f.getName());
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
            }

        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if(e.getAction().equals(Action.PHYSICAL))
        if(e.getClickedBlock() == null) return;
        if(e.getAction() == Action.RIGHT_CLICK_AIR) return;

        if(e.getAction().equals(Action.LEFT_CLICK_BLOCK)){
            if(e.getMaterial().equals(Material.AIR)) return;
            if (e.getClickedBlock() == null) return;
            if(e.getItem().getItemMeta().hasLore()){
                if (e.getItem().getItemMeta().getLore().equals(Arrays.asList("Left Click - Position 1", "Right Click - Position 2"))) {
                    Player p = e.getPlayer();
                    for (Selection s : HCFMain.positions) {
                        if (s.getPlayer().equals(p)) {
                            s.setPos1(e.getClickedBlock().getLocation());
                            p.sendMessage(ChatColor.YELLOW + "Position 1 set.");
                            return;
                        }
                    }
                    Selection s = new Selection(p);
                    s.setPos1(e.getClickedBlock().getLocation());
                    HCFMain.positions.add(s);
                    p.sendMessage(ChatColor.YELLOW + "Position 1 set.");
                    return;
                }
            }
        }
        if(e.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            if(e.getMaterial().equals(Material.AIR)) return;
            if (e.getClickedBlock() == null) return;
            if(e.getItem().getItemMeta().hasLore()){
            if (e.getItem().getItemMeta().getLore().equals(Arrays.asList("Left Click - Position 1", "Right Click - Position 2"))) {
                Player p = e.getPlayer();
                for (Selection s : HCFMain.positions) {
                    if (s.getPlayer().equals(p)) {
                        s.setPos2(e.getClickedBlock().getLocation());
                        p.sendMessage(ChatColor.YELLOW + "Position 2 set.");
                        return;
                    }
                }
                Selection s = new Selection(p);
                s.setPos2(e.getClickedBlock().getLocation());
                HCFMain.positions.add(s);
                p.sendMessage(ChatColor.YELLOW + "Position 2 set.");
                return;
            }
            }
        }


        if(e.getPlayer().hasPermission("hcf.admin")) return;

        for (Faction f : HCFMain.factions) {
            if (f.hasClaim()) {
                if (f.getClaim().containsLocation(e.getClickedBlock().getLocation())) {
                    if (HCFMain.Fplayers.get(e.getPlayer()) != f && f.getDtr() > 0) {
                        e.getPlayer().sendMessage("Land is claimed by " + f.getName());
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }

    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {


        if (e.getPlayer().hasPermission("hcf.admin")) {
            return;
        }

        if (!e.isCancelled()) {
            for (Faction f : HCFMain.factions) {
                if (f.hasClaim()) {
                    if (f.getClaim().containsLocation(e.getBlock().getLocation())) {
                        if (HCFMain.Fplayers.get(e.getPlayer()) != f && f.getDtr() > 0) {
                            e.getPlayer().sendMessage("Land is claimed by " + f.getName());
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
            }

        }
    }


    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (e.getPlayer().hasPermission("hcf.admin")) {
            return;
        }
        if (!e.isCancelled()) {
            for (Faction f : HCFMain.factions) {
                if (f.hasClaim()) {
                    if (f.getClaim().containsLocation(e.getBlock().getLocation())) {
                        if (HCFMain.Fplayers.get(e.getPlayer()) != f && f.getDtr() > 0) {
                            e.getPlayer().sendMessage("Land is claimed by " + f.getName());
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
            }

        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (e.getPlayer().hasPermission("hcf.admin")) {
            return;
        }
        if (!e.isCancelled()) {
            for (Faction f : HCFMain.factions) {
                if (f.hasClaim()) {
                    if (f.getClaim().containsLocation(e.getBlock().getLocation())) {
                        if (HCFMain.Fplayers.get(e.getPlayer()) != f && f.getDtr() > 0) {
                            e.getPlayer().sendMessage("Land is claimed by " + f.getName());
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
            }

        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (e.getPlayer().hasPermission("hcf.admin")) {
            return;
        }
        if (!e.isCancelled()) {
            for (Faction f : HCFMain.factions) {
                if (f.hasClaim()) {
                    if (f.getClaim().containsLocation(e.getBlock().getLocation())) {
                        if (HCFMain.Fplayers.get(e.getPlayer()) != f && f.getDtr() > 0) {
                            e.getPlayer().sendMessage("Land is claimed by " + f.getName());
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
            }

        }
    }

    @EventHandler
    public void onBonemeal(BlockFertilizeEvent e) {
        if (e.getPlayer().hasPermission("hcf.admin")) {
            return;
        }
        if (!e.isCancelled()) {
            for (Faction f : HCFMain.factions) {
                if (f.hasClaim()) {
                    if (f.getClaim().containsLocation(e.getBlock().getLocation())) {
                        if (HCFMain.Fplayers.get(e.getPlayer()) != f && f.getDtr() > 0) {
                            e.getPlayer().sendMessage("Land is claimed by " + f.getName());
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
            }

        }
    }

    @EventHandler
    public void onIgnite(BlockIgniteEvent e) {
        if (e.getCause().equals(BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL) || e.getCause().equals(BlockIgniteEvent.IgniteCause.ARROW)) {
            if (e.getPlayer().hasPermission("hcf.admin")) {
                return;
            }
            if (!e.isCancelled()) {
                for (Faction f : HCFMain.factions) {
                    if (f.hasClaim()) {
                        if (f.getClaim().containsLocation(e.getBlock().getLocation())) {
                            if (HCFMain.Fplayers.get(e.getPlayer()) != f && f.getDtr() > 0) {
                                e.getPlayer().sendMessage("Land is claimed by " + f.getName());
                                e.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (HCFMain.formatChat) {
            Faction team = HCFMain.Fplayers.get(e.getPlayer());
            if (team != null) {
                String tempchatFormat = HCFMain.chatFormat.replaceAll("\\{name}", "%s");
                tempchatFormat = tempchatFormat.replaceAll("\\{team}", team.color + team.getName());
                e.setFormat(tempchatFormat.replaceAll("\\{message}", "%s"));
            } else {
                String tempnoTeamFormat = HCFMain.noTeamFormat.replaceAll("\\{name}", "%s");
                e.setFormat(tempnoTeamFormat.replaceAll("\\{message}", "%s"));
            }
        }
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (e.getInventory() != ColorGUI.colors) return;

        e.setCancelled(true);

        final ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        final Player p = (Player) e.getWhoClicked();
        Faction f = HCFMain.Fplayers.get(p);
        switch (e.getRawSlot()) {

            case 0:
                f.color = ChatColor.WHITE;
                break;
            case 1:
                f.color = ChatColor.GRAY;
                break;
            case 2:
                f.color = ChatColor.DARK_GRAY;
                break;
            case 3:
                f.color = ChatColor.DARK_PURPLE;
                break;
            case 4:
                f.color = ChatColor.LIGHT_PURPLE;
                break;
            case 5:
                f.color = ChatColor.BLUE;
                break;
            case 6:
                f.color = ChatColor.AQUA;
                break;
            case 7:
                f.color = ChatColor.DARK_AQUA;
                break;
            case 8:
                f.color = ChatColor.GREEN;
                break;
            case 9:
                f.color = ChatColor.DARK_GREEN;
                break;
            case 10:
                f.color = ChatColor.RED;
                break;
            case 11:
                f.color = ChatColor.YELLOW;
                break;
            default:
                return;
        }
        p.sendMessage(ChatColor.YELLOW + "Success!");
        p.closeInventory();

    }


    // Cancel dragging in our inventory
    @EventHandler
    public void onInventoryClick(final InventoryDragEvent e) {
        if (e.getInventory() == ColorGUI.colors) {
            e.setCancelled(true);
        }
    }

    public void onMobSpawn(CreatureSpawnEvent e) {
        if (!HCFMain.mobSpawnInClaim) {
            if (e.getEntity() instanceof Monster) {
                for (Faction f : HCFMain.factions) {
                    if (f.hasClaim()) {
                        if (f.getClaim().containsLocation(e.getLocation())) {
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

}
