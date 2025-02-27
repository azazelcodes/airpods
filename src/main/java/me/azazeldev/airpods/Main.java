package me.azazeldev.airpods;

import me.ryanhamshire.GriefPrevention.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Logger;

public final class Main extends JavaPlugin implements Listener {
    public DataStore gpds;
    public FileConfiguration config;
    public final Logger logger = Logger.getLogger("Minecraft");
    public HashMap<List<UUID>, List<Integer>> teams = new HashMap<>();
    public List<UUID> connected = new ArrayList<>();
    public List<Claim> createdclaims = new ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        config = this.getConfig();
        gpds = GriefPrevention.instance.dataStore;

        getServer().getPluginManager().registerEvents(this, this);


        for(String rawData : getConfig().getStringList("teams")) {

            String[] raw = rawData.split(" : ");

            String[] strusers = raw[0].split(",");
            List<UUID> users = new ArrayList<>();
            for (String struser : strusers) users.add(UUID.fromString(struser));

            String[] strcoords = raw[1].split(",");
            List<Integer> coords = new ArrayList<>();
            for (String strcoord : strcoords) coords.add(Integer.valueOf(strcoord));

            teams.put(users, coords);

        }

        populateclaims();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        for (UUID uuid : connected) {
            if (teams.containsKey(uuid) && !teams.get(uuid).equals(teams.get(p))) {
                for (Claim c : createdclaims) {
                    gpds.deleteClaim(c);
                    createdclaims.remove(c);
                }
            }
        }
    }

    @EventHandler
    void onLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        boolean rivalry = false;

        for (UUID uuid : connected) {
            if (teams.containsKey(uuid) && !teams.get(uuid).equals(teams.get(p))) {
                rivalry = true;
            }
        }

        if (!rivalry) populateclaims();
    }


    void populateclaims() {
        long i = 0L;
        for (List<UUID> lu : teams.keySet()) {
            CreateClaimResult cr = gpds.createClaim(
                    getServer().getWorld("world"),
                    teams.get(lu).get(0), teams.get(lu).get(1), 0, 1000, teams.get(lu).get(2), teams.get(lu).get(3),
                    lu.removeFirst(),
                    null,
                    i,
                    null
            );

            if (cr.succeeded) createdclaims.add(cr.claim);

            for (UUID u : lu) {
                gpds.getClaim(i).setPermission(u.toString(), ClaimPermission.Access);
            }

            i += 1;
        }
    }
}
