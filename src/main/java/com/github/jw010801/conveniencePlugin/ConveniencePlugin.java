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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ConveniencePlugin extends JavaPlugin {
    
    private final Map<UUID, Location> playerHomes = new HashMap<>();
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        getLogger().info("ConveniencePlugin이 활성화되었습니다!");
        
        try {
            // 데이터베이스 관리자 초기화
            databaseManager = new DatabaseManager(this);
            databaseManager.initialize();
            
            // 기존 YAML 데이터가 있다면 마이그레이션
            migrateFromYamlIfNeeded();
            
            // DB에서 모든 홈 데이터 로드
            playerHomes.putAll(databaseManager.loadAllHomes());
            
            getLogger().info("데이터베이스 연동 완료! (SQLite + HikariCP)");
            
        } catch (Exception e) {
            getLogger().severe("플러그인 초기화 실패: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        // 데이터베이스 연결 종료
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
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
            case "delhome" -> {
                handleDelHomeCommand(player);
                yield true;
            }
            case "status" -> {
                handleStatusCommand(player);
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
        UUID playerUuid = player.getUniqueId();
        
        // 메모리와 데이터베이스에 동시 저장
        playerHomes.put(playerUuid, currentLocation);
        databaseManager.savePlayerHome(playerUuid, currentLocation);
        
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
    
    private void handleDelHomeCommand(Player player) {
        UUID playerId = player.getUniqueId();
        
        if (!playerHomes.containsKey(playerId)) {
            player.sendMessage("§c삭제할 집이 없습니다.");
            return;
        }
        
        // 메모리와 데이터베이스에서 모두 삭제
        playerHomes.remove(playerId);
        databaseManager.deletePlayerHome(playerId);
        
        player.sendMessage("§a집이 성공적으로 삭제되었습니다!");
        getLogger().info("플레이어 " + player.getName() + "이(가) 집을 삭제했습니다.");
    }
    
    private void handleStatusCommand(Player player) {
        boolean isDbConnected = databaseManager.isConnected();
        int totalHomes = playerHomes.size();
        
        // DB에서 직접 플레이어 홈 조회 (getPlayerHome 메서드 활용)
        Location dbHome = databaseManager.getPlayerHome(player.getUniqueId());
        boolean hasHomeInDb = (dbHome != null);
        
        player.sendMessage("§6=== ConveniencePlugin 상태 ===");
        player.sendMessage("§7데이터베이스 연결: " + (isDbConnected ? "§a정상" : "§c오류"));
        player.sendMessage("§7전체 집 개수: §e" + totalHomes + "개");
        player.sendMessage("§7내 집 상태: " + (hasHomeInDb ? "§a설정됨" : "§c없음"));
        
        if (player.hasPermission("convenienceplugin.admin")) {
            player.sendMessage("§7플러그인 버전: §e1.0-SNAPSHOT");
            player.sendMessage("§7데이터베이스: §eSQLite + HikariCP");
        }
    }
    
    /**
     * 기존 YAML 데이터가 있다면 SQLite 데이터베이스로 마이그레이션합니다
     */
    private void migrateFromYamlIfNeeded() {
        File homeDataFile = new File(getDataFolder(), "homes.yml");
        
        if (!homeDataFile.exists()) {
            getLogger().info("기존 YAML 데이터 파일이 없습니다. 새로운 데이터베이스로 시작합니다.");
            return;
        }
        
        getLogger().info("기존 YAML 데이터를 발견했습니다. 데이터베이스로 마이그레이션을 시작합니다...");
        
        try {
            FileConfiguration homeData = YamlConfiguration.loadConfiguration(homeDataFile);
            int migratedCount = 0;
            
            for (String key : homeData.getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(key);
                    String worldName = homeData.getString(key + ".world");
                    
                    if (worldName == null) {
                        getLogger().warning("플레이어 " + key + "의 월드 정보가 없습니다. 건너뜁니다.");
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
                        databaseManager.savePlayerHome(playerId, location);
                        migratedCount++;
                    } else {
                        getLogger().warning("플레이어 " + key + "의 월드 '" + worldName + "'을(를) 찾을 수 없습니다. 건너뜁니다.");
                    }
                } catch (Exception e) {
                    getLogger().warning("플레이어 " + key + "의 데이터 마이그레이션 중 오류 발생: " + e.getMessage());
                }
            }
            
            getLogger().info("YAML → SQLite 마이그레이션 완료! (총 " + migratedCount + "개 데이터 이전)");
            
            // 마이그레이션 완료 후 백업 파일로 이름 변경
            File backupFile = new File(getDataFolder(), "homes.yml.backup");
            if (homeDataFile.renameTo(backupFile)) {
                getLogger().info("기존 YAML 파일을 homes.yml.backup으로 백업했습니다.");
            }
            
        } catch (Exception e) {
            getLogger().severe("YAML 데이터 마이그레이션 실패: " + e.getMessage());
        }
    }
}
