package life.steeze.simplehcf;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.awt.*;

public class Selection {
    private final Player player;
    private Point start,end;
    private World world;

    public Selection(Player p){
        this.player = p;
    }

    public Point pos1(){
        return this.start;
    }
    public Point pos2(){
        return this.end;
    }
    public World getWorld(){
        return this.world;
    }

    public Player getPlayer(){
        return this.player;
    }

    public void setPos1(Location l){
        Point p = new Point(l.getBlockX(),l.getBlockZ());
        this.start = p;
        this.world = l.getWorld();
    }
    public void setPos2(Location l){
        Point p = new Point(l.getBlockX(), l.getBlockZ());
        this.end = p;
        this.world = l.getWorld();
    }
    public boolean canCreateClaim(){
        if(start != null && end != null){
            if(start.distance(end) <= 1) return false;
            return true;
        }
        return false;
    }
}
