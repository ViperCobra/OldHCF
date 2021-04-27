package life.steeze.simplehcf;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public class ClaimWand{
    static final NamespacedKey key = new NamespacedKey(HCFMain.inst, "wand");
    public static ItemStack getWand(){
        ItemStack wand = new ItemStack(Material.WOODEN_AXE, 1);
        ItemMeta meta = wand.getItemMeta();
        assert meta != null;


        meta.setLore(Arrays.asList("Left Click - Position 1", "Right Click - Position 2"));
        meta.setDisplayName("Claiming Wand");
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        wand.setItemMeta(meta);
        return wand;
    }
    public static boolean isWand(ItemStack s){
        if(!s.getType().equals(Material.WOODEN_AXE)) return false;
        PersistentDataContainer container = s.getItemMeta().getPersistentDataContainer();
        return container.has(key, PersistentDataType.BYTE);
    }
}
