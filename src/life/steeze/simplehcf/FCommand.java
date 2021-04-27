package life.steeze.simplehcf;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;


public class FCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if(command.getName().equals("faction")){
            if(commandSender instanceof Player) {
                Player p = (Player) commandSender;
                switch (args.length) {
                    case 0:
                        commandSender.sendMessage(ChatColor.WHITE + "/f create <name>" + ChatColor.YELLOW + " - Create a faction");
                        commandSender.sendMessage(ChatColor.WHITE + "/f desc <your description here>" + ChatColor.YELLOW + " - Tell everyone what you're about");
                        commandSender.sendMessage(ChatColor.WHITE + "/f invite <name>" + ChatColor.YELLOW + " - Invite your friends");
                        commandSender.sendMessage(ChatColor.WHITE + "/f kick <name>" + ChatColor.YELLOW + " - Kick someone");
                        commandSender.sendMessage(ChatColor.WHITE + "/f setleader <name>" + ChatColor.YELLOW + " - Relinquish your power");
                        commandSender.sendMessage(ChatColor.WHITE + "/f leave" + ChatColor.YELLOW + " - Leave your faction");
                        commandSender.sendMessage(ChatColor.WHITE + "/f info [name]" + ChatColor.YELLOW + " - View a faction's info");
                        commandSender.sendMessage(ChatColor.WHITE + "/f setcolor" + ChatColor.YELLOW + " - Set your faction color");
                        commandSender.sendMessage(ChatColor.WHITE + "/f list" + ChatColor.YELLOW + " - View all factions");
                        commandSender.sendMessage(ChatColor.WHITE + "/f sethome" + ChatColor.YELLOW + " - Set your faction's home");
                        commandSender.sendMessage(ChatColor.WHITE + "/f home" + ChatColor.YELLOW + " - Go to your faction's home");
                        commandSender.sendMessage(ChatColor.WHITE + "/pos1, /pos2" + ChatColor.YELLOW + " - Select your faction's claim manually");
                        commandSender.sendMessage(ChatColor.WHITE + "/f claim" + ChatColor.YELLOW + " - Obtain a wand to make your faction's claim");
                        commandSender.sendMessage(ChatColor.WHITE + "/f unclaim" + ChatColor.YELLOW + " - Undo your claim");
                        return true;
                    case 1:

                        // /f home, /f sethome, /f claim.
                        switch (args[0]) {
                            case "info":
                                if(HCFMain.Fplayers.get(p) != null){
                                    Faction infoFac = HCFMain.Fplayers.get(p);
                                    infoFac.showInfo(p);
                                } else {
                                    p.sendMessage(ChatColor.RED + "You must be in a faction or use /f info [team]");
                                }
                                return true;
                            case "accept":
                                if(HCFMain.Fplayers.get(p) == null || !HCFMain.Fplayers.containsKey(p)){
                                    if(HCFMain.invites.get(p) != null){
                                        HCFMain.invites.get(p).addPlayer(p.getUniqueId());
                                    } else {
                                        p.sendMessage(ChatColor.RED + "You don't have any invites!");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "You're already in a faction!");
                                }
                                return true;
                            case "leave":
                                Faction f = HCFMain.Fplayers.get(p);
                                if(f != null){
                                    if(f.getLeader().equals(p.getUniqueId())){
                                        f.disband();
                                        p.sendMessage(ChatColor.RED + "Your faction has been disbanded!");
                                        return true;
                                    } else {
                                        f.removePlayer(p.getUniqueId());
                                        p.sendMessage(ChatColor.RED + "You have left your faction.");
                                        return true;
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "You're not in a faction");
                                }
                                return true;

                            case "list":
                                p.sendMessage(ChatColor.YELLOW + "----==== Factions List ====----");
                                for(Faction listed : HCFMain.factions){
                                    p.sendMessage(listed.color + listed.getName() + ChatColor.YELLOW + " | " + ChatColor.WHITE + (listed.getMembers().size() + 1) + " members.");
                                }
                                return true;

                            case "home":
                                Faction faction = HCFMain.Fplayers.get(p);
                                if(faction != null){
                                    faction.tpHome(p);
                                } else {
                                    p.sendMessage(ChatColor.RED + "You're not in a faction");
                                }
                                return true;

                            case "sethome":
                                Faction sethomefaction = HCFMain.Fplayers.get(p);
                                if(sethomefaction != null){
                                    if(sethomefaction.getLeader().equals(p.getUniqueId())){
                                        sethomefaction.setHome(p);
                                    } else {
                                        p.sendMessage(ChatColor.RED + "You must be the leader to set the faction home.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "You're not in a faction");
                                }
                                return true;
                            case "unclaim":
                                Faction unclaimFac = HCFMain.Fplayers.get(p);
                                if(unclaimFac != null){
                                    if(unclaimFac.getLeader().equals(p.getUniqueId())){
                                        if(unclaimFac.hasClaim()){
                                             p.sendMessage(ChatColor.YELLOW + "You have successfully unclaimed your land. Be careful");
                                             unclaimFac.setClaim(null);
                                            } else {
                                            p.sendMessage(ChatColor.RED + "You don't have a claim");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "You must be leader of your faction");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "You're not in a faction");
                                }

                                return true;
                            case "claim":
                                Selection selection = null;
                                for(Selection s : HCFMain.positions){
                                    if(s.getPlayer().equals(p)){
                                        selection = s;
                                        break;
                                    }
                                }
                                if(selection != null && selection.pos1() != null && selection.pos2() != null){
                                    if(selection.pos1().distance(selection.pos2()) <= HCFMain.maximumClaimCornerDistance) {
                                        Claim c = new Claim(selection);
                                        if(c.getBounds().getWidthZ() >= HCFMain.minimumClaimWidth && c.getBounds().getWidthX() >= HCFMain.minimumClaimWidth) {
                                            if(!c.overlapsExisting()) {
                                                if (HCFMain.Fplayers.get(p) != null && HCFMain.Fplayers.get(p).getLeader().equals(p.getUniqueId())) {
                                                    if (HCFMain.Fplayers.get(p).hasClaim()) {
                                                        p.sendMessage(ChatColor.RED + "Your team already has a claim!");
                                                        return true;
                                                    }
                                                    HCFMain.Fplayers.get(p).setClaim(c);
                                                    p.sendMessage(ChatColor.YELLOW + "Success!");
                                                } else {
                                                    p.sendMessage(ChatColor.RED + "You must be the leader of a faction!");
                                                }
                                            } else {
                                                p.sendMessage(ChatColor.RED + "Land is already claimed.");
                                            }
                                        } else {
                                            p.sendMessage(ChatColor.RED + "Claim too small or narrow");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "Claim too big.");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "You must make a selection with /pos1 & /pos2, or use this claiming wand!");
                                    p.getInventory().addItem(ClaimWand.getWand());
                                }
                                return true;
                            case "setcolor":
                                if(HCFMain.Fplayers.get(p) != null){
                                    if(HCFMain.Fplayers.get(p).getLeader().equals(p.getUniqueId())){

                                        p.openInventory(ColorGUI.colors);

                                    } else {
                                        p.sendMessage(ChatColor.RED + "You must be the leader of your faction.");
                                        return true;
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "You must be the leader of a faction.");
                                    return true;
                                }
                                return true;
                            default:
                                commandSender.sendMessage(ChatColor.WHITE + "/f create <name>" + ChatColor.YELLOW + " - Create a faction");
                                commandSender.sendMessage(ChatColor.WHITE + "/f desc <your description here>" + ChatColor.YELLOW + " - Tell everyone what you're about");
                                commandSender.sendMessage(ChatColor.WHITE + "/f invite <name>" + ChatColor.YELLOW + " - Invite your friends");
                                commandSender.sendMessage(ChatColor.WHITE + "/f kick <name>" + ChatColor.YELLOW + " - Kick someone");
                                commandSender.sendMessage(ChatColor.WHITE + "/f setleader <name>" + ChatColor.YELLOW + " - Relinquish your power");
                                commandSender.sendMessage(ChatColor.WHITE + "/f leave" + ChatColor.YELLOW + " - Leave your faction");
                                commandSender.sendMessage(ChatColor.WHITE + "/f info [name]" + ChatColor.YELLOW + " - View a faction's info");
                                commandSender.sendMessage(ChatColor.WHITE + "/f setcolor" + ChatColor.YELLOW + " - Set your faction color");
                                commandSender.sendMessage(ChatColor.WHITE + "/f list" + ChatColor.YELLOW + " - View all factions");
                                commandSender.sendMessage(ChatColor.WHITE + "/f sethome" + ChatColor.YELLOW + " - Set your faction's home");
                                commandSender.sendMessage(ChatColor.WHITE + "/f home" + ChatColor.YELLOW + " - Go to your faction's home");
                                commandSender.sendMessage(ChatColor.WHITE + "/pos1, /pos2" + ChatColor.YELLOW + " - Select your faction's claim manually");
                                commandSender.sendMessage(ChatColor.WHITE + "/f claim" + ChatColor.YELLOW + " - Obtain a wand to make your faction's claim");
                                commandSender.sendMessage(ChatColor.WHITE + "/f unclaim" + ChatColor.YELLOW + " - Undo your claim");
                                return true;
                        }

                    case 2:
                        // /f info faction
                        switch (args[0]) {
                            case "create":

                                if(HCFMain.Fplayers.containsKey(p)){
                                    p.sendMessage(ChatColor.RED + "Nice try");
                                    return true;
                                }

                                Pattern pattern = Pattern.compile("[^a-zA-Z]");
                                boolean hasBadChar = pattern.matcher(args[1]).find();
                                if(!hasBadChar){
                                        Faction newFac = new Faction(args[1], p);
                                        HCFMain.factions.add(newFac);
                                        Bukkit.broadcastMessage(ChatColor.YELLOW + "Faction " + ChatColor.WHITE + args[1] + ChatColor.YELLOW + " has been founded!");
                                } else {
                                    p.sendMessage(ChatColor.RED + "Try another name with A-Z only");
                                }
                                return true;

                            case "invite":
                                Player target = Bukkit.getPlayer(args[1]);
                                if (target != null) {

                                    if(target.equals(p)){
                                        p.sendMessage(ChatColor.RED + "You can't invite yourself");
                                        return true;
                                    }

                                    if(HCFMain.Fplayers.get(p) != null){
                                        if(HCFMain.Fplayers.get(p).getLeader().equals(p.getUniqueId())) {
                                            if (HCFMain.Fplayers.get(target) == null || !HCFMain.Fplayers.containsKey(target)) {
                                                if(HCFMain.invites.get(target) == null || !HCFMain.invites.containsKey(target)) {
                                                    p.sendMessage(ChatColor.YELLOW + "Invite sent!");
                                                    target.sendMessage(ChatColor.YELLOW + "You have been invited to join " + HCFMain.Fplayers.get(p).color + HCFMain.Fplayers.get(p).getName() + ChatColor.YELLOW + ". Do you accept? " + ChatColor.WHITE + "/f accept");
                                                    HCFMain.invites.put(target, HCFMain.Fplayers.get(p));
                                                    new BukkitRunnable() {
                                                        @Override
                                                        public void run() {
                                                            HCFMain.invites.remove(target);
                                                        }
                                                    }.runTaskLaterAsynchronously(HCFMain.inst, 600);
                                                } else {
                                                    p.sendMessage(ChatColor.RED + "That person already has an invitation.");
                                                }
                                            } else {
                                                p.sendMessage(ChatColor.RED + "Player is already in a faction!");
                                            }
                                        } else {
                                            p.sendMessage(ChatColor.RED + "You must be leader to invite!");
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "You must be in a faction!");
                                    }
                                } else {
                                    commandSender.sendMessage(ChatColor.RED + "Player not found");
                                    return true;
                                }
                                return true;


                            case "setleader":
                                if(HCFMain.Fplayers.get(p) != null){
                                    if(HCFMain.Fplayers.get(p).getLeader().equals(p.getUniqueId())){
                                        if(Bukkit.getOfflinePlayer(args[1]).isOnline()){
                                            UUID newLeader = Bukkit.getPlayer(args[1]).getUniqueId();
                                            if(HCFMain.Fplayers.get(p).getMembers().contains(newLeader)){
                                                HCFMain.Fplayers.get(p).setLeader(newLeader);
                                            } else {
                                                p.sendMessage(ChatColor.RED + "Player is not a part of your faction!");
                                            }
                                        } else {
                                            if(Bukkit.getOfflinePlayer(args[1]).hasPlayedBefore()) {
                                                UUID newLeader = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
                                                if(HCFMain.Fplayers.get(p).getMembers().contains(newLeader)){
                                                    HCFMain.Fplayers.get(p).setLeader(newLeader);
                                                } else {
                                                    p.sendMessage(ChatColor.RED + "Player is not a part of your faction!");
                                                }
                                            } else {
                                                p.sendMessage(ChatColor.RED + "Player not found");
                                            }
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "You must be the leader to set the leader!");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "You must be in a faction");
                                }
                                return true;
                            case "info":
                                if(Bukkit.getPlayer(args[1]) != null){
                                    Player infotarget = Bukkit.getPlayer(args[1]);
                                    if(HCFMain.Fplayers.get(infotarget) != null){
                                        Faction infoFac = HCFMain.Fplayers.get(infotarget);
                                        infoFac.showInfo(p);
                                    } else {
                                        p.sendMessage(ChatColor.RED + "Player is not in a faction");
                                    }
                                } else if(HCFMain.getFacByName(args[1]) != null){
                                    //found faction by this name
                                    Faction infoFac = HCFMain.getFacByName(args[1]);
                                    infoFac.showInfo(p);
                                } else {
                                    p.sendMessage(ChatColor.RED + "No player or faction found by that name.");
                                }
                                return true;
                            case "kick":
                                if(HCFMain.Fplayers.get(p) != null){
                                    Faction f = HCFMain.Fplayers.get(p);
                                    if(HCFMain.Fplayers.get(p).getLeader().equals(p.getUniqueId())){
                                        if(Bukkit.getOfflinePlayer(args[1]).isOnline()){
                                            UUID id = Bukkit.getPlayer(args[1]).getUniqueId();
                                            if(f.getMembers().contains(id)){
                                                f.removePlayer(id);
                                            } else {
                                                p.sendMessage(ChatColor.RED + "Player is not in your faction.");
                                            }
                                        } else {
                                            UUID id = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
                                            if(f.getMembers().contains(id)){
                                                f.removePlayer(id);
                                            } else {
                                                p.sendMessage(ChatColor.RED + "Player is not in your faction.");
                                            }
                                        }
                                    } else {
                                        p.sendMessage(ChatColor.RED + "You must be the leader!");
                                    }
                                } else {
                                    p.sendMessage(ChatColor.RED + "You're not in a faction!");
                                    return true;
                                }
                                return true;


                            case "desc":
                                Faction f = HCFMain.Fplayers.get(p);
                                if(f != null){
                                    if(args[1].toCharArray().length <= HCFMain.maxDescriptionLength){
                                        p.sendMessage(ChatColor.YELLOW + "Success!");
                                        f.setDescription(args[1]);
                                        return true;
                                    } else {
                                        p.sendMessage(ChatColor.RED + "Description exceeds maximum length.");
                                        return true;
                                    }

                                } else {
                                    p.sendMessage(ChatColor.RED + "You're not in a faction");
                                    return true;
                                }
                            default:
                                commandSender.sendMessage(ChatColor.WHITE + "/f create <name>" + ChatColor.YELLOW + " - Create a faction");
                                commandSender.sendMessage(ChatColor.WHITE + "/f desc <your description here>" + ChatColor.YELLOW + " - Tell everyone what you're about");
                                commandSender.sendMessage(ChatColor.WHITE + "/f invite <name>" + ChatColor.YELLOW + " - Invite your friends");
                                commandSender.sendMessage(ChatColor.WHITE + "/f kick <name>" + ChatColor.YELLOW + " - Kick someone");
                                commandSender.sendMessage(ChatColor.WHITE + "/f setleader <name>" + ChatColor.YELLOW + " - Relinquish your power");
                                commandSender.sendMessage(ChatColor.WHITE + "/f leave" + ChatColor.YELLOW + " - Leave your faction");
                                commandSender.sendMessage(ChatColor.WHITE + "/f info [name]" + ChatColor.YELLOW + " - View a faction's info");
                                commandSender.sendMessage(ChatColor.WHITE + "/f setcolor" + ChatColor.YELLOW + " - Set your faction color");
                                commandSender.sendMessage(ChatColor.WHITE + "/f list" + ChatColor.YELLOW + " - View all factions");
                                commandSender.sendMessage(ChatColor.WHITE + "/f sethome" + ChatColor.YELLOW + " - Set your faction's home");
                                commandSender.sendMessage(ChatColor.WHITE + "/f home" + ChatColor.YELLOW + " - Go to your faction's home");
                                commandSender.sendMessage(ChatColor.WHITE + "/pos1, /pos2" + ChatColor.YELLOW + " - Select your faction's claim manually");
                                commandSender.sendMessage(ChatColor.WHITE + "/f claim" + ChatColor.YELLOW + " - Obtain a wand to make your faction's claim");
                                commandSender.sendMessage(ChatColor.WHITE + "/f unclaim" + ChatColor.YELLOW + " - Undo your claim");
                                return true;
                        }
                    default:
                        if(args[0].equals("desc")){
                            String[] arguments = new String[args.length - 1];
                            for(int i = 0; i < args.length; i++){
                                if(i > 0){
                                    arguments[i - 1] = args[i];
                                }
                            }
                            Faction descFac = HCFMain.Fplayers.get(p);
                            if(descFac != null){
                                if(Arrays.toString(arguments).toCharArray().length <= HCFMain.maxDescriptionLength) {
                                    descFac.setDescription(String.join(" ", arguments));
                                    p.sendMessage(ChatColor.YELLOW + "Success!");
                                    return true;
                                } else {
                                    p.sendMessage(ChatColor.RED + "Description exceeds maximum length.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "You're not in a faction!");
                                return true;
                            }
                        }
                        commandSender.sendMessage(ChatColor.RED + "Too many arguments.");
                        return true;
                }
            }
        }
        return true;
    }
}
