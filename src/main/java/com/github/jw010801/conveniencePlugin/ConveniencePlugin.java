package com.github.jw010801.conveniencePlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ConveniencePlugin extends JavaPlugin {
    
    private final Map<UUID, Location> playerHomes = new HashMap<>();
    private File homeDataFile;
    private FileConfiguration homeData;

    @Override
    public void onEnable() {
        // 플러그인 시작 로그
        getLogger().info("ConveniencePlugin이 활성화되었습니다!");
        
        // 홈 데이터 파일 초기화
        initializeHomeData();
        loadHomeData();
    }

    @Override
    public void onDisable() {
        // 홈 데이터 저장
        saveHomeData();
        getLogger().info("ConveniencePlugin이 비활성화되었습니다!");
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                           @NotNull String label, @NotNull String @NotNull [] args) {
        
        // 플레이어만 사용 가능한 명령어들
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§c이 명령어는 플레이어만 사용할 수 있습니다.");
            return true;
        }
        
        return switch (command.getName().toLowerCase()) {
            case "spawn" -> {
                handleSpawnCommand(player);
                yield true;
            }
            case "sethome" -> {
                handleSetHomeCommand(player);
                yield true;
            }
            case "home" -> {
                handleHomeCommand(player);
                yield true;
            }
            default -> false;
        };
    }
    
    private void handleSpawnCommand(Player player) {
        World world = player.getWorld();
        Location spawnLocation = world.getSpawnLocation();
        
        player.teleport(spawnLocation);
        player.sendMessage("§a스폰 지점으로 텔레포트되었습니다!");
    }
    
    private void handleSetHomeCommand(Player player) {
        Location currentLocation = player.getLocation();
        
        // final Map이어도 put() 작업은 문제없이 작동합니다
        playerHomes.put(player.getUniqueId(), currentLocation);
        
        // 데이터 저장
        saveHomeData();
        
        player.sendMessage("§a현재 위치가 집으로 설정되었습니다!");
        getLogger().info("플레이어 " + player.getName() + "이(가) 집을 설정했습니다. (총 " + playerHomes.size() + "명의 플레이어가 집을 보유 중)");
    }
    
    private void handleHomeCommand(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (!playerHomes.containsKey(playerId)) {
            player.sendMessage("§c설정된 집이 없습니다. /sethome 명령어로 집을 설정해주세요.");
            return;
        }
        
        Location homeLocation = playerHomes.get(playerId);
        player.teleport(homeLocation);
        player.sendMessage("§a집으로 텔레포트되었습니다!");
    }
    
    private void initializeHomeData() {
        homeDataFile = new File(getDataFolder(), "homes.yml");
        if (!homeDataFile.exists()) {
            File parentDir = homeDataFile.getParentFile();
            if (parentDir != null && !parentDir.mkdirs() && !parentDir.exists()) {
                getLogger().severe("플러그인 데이터 폴더를 생성할 수 없습니다: " + parentDir.getAbsolutePath());
                return;
            }
            
            try {
                if (!homeDataFile.createNewFile()) {
                    getLogger().severe("홈 데이터 파일을 생성할 수 없습니다: " + homeDataFile.getAbsolutePath());
                    return;
                }
            } catch (IOException e) {
                getLogger().severe("홈 데이터 파일을 생성하는 중 오류 발생: " + e.getMessage());
                return;
            }
        }
        homeData = YamlConfiguration.loadConfiguration(homeDataFile);
    }
    
    private void loadHomeData() {
        for (String key : homeData.getKeys(false)) {
            try {
                UUID playerId = UUID.fromString(key);
                String worldName = homeData.getString(key + ".world");
                
                if (worldName == null) {
                    getLogger().warning("플레이어 " + key + "의 월드 정보가 없습니다.");
                    continue;
                }
                
                double x = homeData.getDouble(key + ".x");
                double y = homeData.getDouble(key + ".y");
                double z = homeData.getDouble(key + ".z");
                float yaw = (float) homeData.getDouble(key + ".yaw");
                float pitch = (float) homeData.getDouble(key + ".pitch");
                
                World world = getServer().getWorld(worldName);
                if (world != null) {
                    Location location = new Location(world, x, y, z, yaw, pitch);
                    playerHomes.put(playerId, location);
                } else {
                    getLogger().warning("플레이어 " + key + "의 월드 '" + worldName + "'을(를) 찾을 수 없습니다.");
                }
            } catch (Exception e) {
                getLogger().warning("플레이어 " + key + "의 홈 데이터를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            }
        }
    }
    
    private void saveHomeData() {
        for (Map.Entry<UUID, Location> entry : playerHomes.entrySet()) {
            String playerId = entry.getKey().toString();
            Location location = entry.getValue();
            
            homeData.set(playerId + ".world", location.getWorld().getName());
            homeData.set(playerId + ".x", location.getX());
            homeData.set(playerId + ".y", location.getY());
            homeData.set(playerId + ".z", location.getZ());
            homeData.set(playerId + ".yaw", location.getYaw());
            homeData.set(playerId + ".pitch", location.getPitch());
        }
        
        try {
            homeData.save(homeDataFile);
        } catch (IOException e) {
            getLogger().severe("홈 데이터를 저장하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
