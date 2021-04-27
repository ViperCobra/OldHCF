package life.steeze.simplehcf;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PosCommands implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        if(!(commandSender instanceof Player)) return true;
        Player p = (Player) commandSender;

        if(command.getName().equals("pos1")){
            for(Selection s : HCFMain.positions){
                if(s.getPlayer().equals(p)){
                    s.setPos1(p.getLocation());
                    p.sendMessage(ChatColor.YELLOW + "Position 1 set.");
                    return true;
                }
            }
            Selection s = new Selection(p);
            s.setPos1(p.getLocation());
            HCFMain.positions.add(s);
            p.sendMessage(ChatColor.YELLOW + "Position 1 set.");
            return true;
        }
        if(command.getName().equals("pos2")){
            for(Selection s : HCFMain.positions){
                if(s.getPlayer().equals(p)){
                    s.setPos2(p.getLocation());
                    p.sendMessage(ChatColor.YELLOW + "Position 2 set.");
                    return true;
                }
            }
            Selection s = new Selection(p);
            s.setPos2(p.getLocation());
            HCFMain.positions.add(s);
            p.sendMessage(ChatColor.YELLOW + "Position 2 set.");
            return true;
        }

        return true;
    }
}
