package com.github.jw010801.conveniencePlugin;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 데이터베이스 연결 및 플레이어 홈 데이터 관리를 담당하는 클래스
 * HikariCP Connection Pool과 SQLite를 활용한 확장 가능한 구조
 */
public class DatabaseManager {
    
    private final JavaPlugin plugin;
    private final Logger logger;
    private HikariDataSource dataSource;
    
    // 테이블 생성 SQL
    private static final String CREATE_HOMES_TABLE = """
            CREATE TABLE IF NOT EXISTS player_homes (
                player_uuid TEXT PRIMARY KEY,
                world_name TEXT NOT NULL,
                x DOUBLE NOT NULL,
                y DOUBLE NOT NULL,
                z DOUBLE NOT NULL,
                yaw REAL NOT NULL,
                pitch REAL NOT NULL,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """;
    
    public DatabaseManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    /**
     * 데이터베이스 연결 초기화
     * HikariCP Connection Pool 설정
     */
    public void initialize() {
        try {
            setupDataSource();
            createTables();
            logger.info("데이터베이스 초기화 완료 (SQLite + HikariCP)");
        } catch (SQLException e) {
            logger.severe("데이터베이스 초기화 실패: " + e.getMessage());
            throw new RuntimeException("데이터베이스 초기화 실패", e);
        }
    }
    
    /**
     * HikariCP DataSource 설정
     */
    private void setupDataSource() {
        HikariConfig config = new HikariConfig();
        
        // SQLite 데이터베이스 파일 경로
        File dbFile = new File(plugin.getDataFolder(), "homes.db");
        String jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        
        config.setJdbcUrl(jdbcUrl);
        config.setDriverClassName("org.sqlite.JDBC");
        
        // Connection Pool 설정 (SQLite 특성을 고려한 설정)
        config.setMaximumPoolSize(1);           // SQLite는 단일 연결 권장
        config.setMinimumIdle(1);               // 항상 1개 연결 유지
        config.setMaxLifetime(1800000);         // 30분 (1800초)
        config.setConnectionTimeout(20000);     // 20초
        config.setIdleTimeout(600000);          // 10분
        
        // SQLite 최적화 설정
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        // Connection Pool 이름 설정 (모니터링용)
        config.setPoolName("ConveniencePlugin-DB-Pool");
        
        dataSource = new HikariDataSource(config);
        logger.info("HikariCP 데이터소스 설정 완료");
    }
    
    /**
     * 필요한 테이블들을 생성합니다
     */
    private void createTables() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(CREATE_HOMES_TABLE);
            logger.info("데이터베이스 테이블 생성 완료");
        }
    }
    
    /**
     * 플레이어의 홈 위치를 저장합니다
     */
    public void savePlayerHome(@NotNull UUID playerUuid, @NotNull Location location) {
        String sql = """
                INSERT OR REPLACE INTO player_homes
                (player_uuid, world_name, x, y, z, yaw, pitch, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        
        long currentTime = System.currentTimeMillis();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, playerUuid.toString());
            pstmt.setString(2, location.getWorld().getName());
            pstmt.setDouble(3, location.getX());
            pstmt.setDouble(4, location.getY());
            pstmt.setDouble(5, location.getZ());
            pstmt.setFloat(6, location.getYaw());
            pstmt.setFloat(7, location.getPitch());
            pstmt.setLong(8, currentTime);  // created_at
            pstmt.setLong(9, currentTime);  // updated_at
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            logger.severe("플레이어 홈 저장 실패 (UUID: " + playerUuid + "): " + e.getMessage());
        }
    }
    
    /**
     * 플레이어의 홈 위치를 불러옵니다
     */
    @Nullable
    public Location getPlayerHome(@NotNull UUID playerUuid) {
        String sql = "SELECT world_name, x, y, z, yaw, pitch FROM player_homes WHERE player_uuid = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, playerUuid.toString());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String worldName = rs.getString("world_name");
                    World world = plugin.getServer().getWorld(worldName);
                    
                    if (world == null) {
                        logger.warning("플레이어 " + playerUuid + "의 홈이 있는 월드 '" + worldName + "'을(를) 찾을 수 없습니다.");
                        return null;
                    }
                    
                    double x = rs.getDouble("x");
                    double y = rs.getDouble("y");
                    double z = rs.getDouble("z");
                    float yaw = rs.getFloat("yaw");
                    float pitch = rs.getFloat("pitch");
                    
                    return new Location(world, x, y, z, yaw, pitch);
                }
            }
            
        } catch (SQLException e) {
            logger.severe("플레이어 홈 조회 실패 (UUID: " + playerUuid + "): " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 모든 플레이어 홈 데이터를 메모리에 로드합니다
     */
    @NotNull
    public Map<UUID, Location> loadAllHomes() {
        Map<UUID, Location> homes = new HashMap<>();
        String sql = "SELECT player_uuid, world_name, x, y, z, yaw, pitch FROM player_homes";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                try {
                    UUID playerUuid = UUID.fromString(rs.getString("player_uuid"));
                    String worldName = rs.getString("world_name");
                    World world = plugin.getServer().getWorld(worldName);
                    
                    if (world != null) {
                        double x = rs.getDouble("x");
                        double y = rs.getDouble("y");
                        double z = rs.getDouble("z");
                        float yaw = rs.getFloat("yaw");
                        float pitch = rs.getFloat("pitch");
                        
                        Location location = new Location(world, x, y, z, yaw, pitch);
                        homes.put(playerUuid, location);
                    } else {
                        logger.warning("월드 '" + worldName + "'을(를) 찾을 수 없습니다. 플레이어 " + playerUuid + "의 홈을 건너뜁니다.");
                    }
                } catch (Exception e) {
                    logger.warning("홈 데이터 로드 중 오류 발생: " + e.getMessage());
                }
            }
            
        } catch (SQLException e) {
            logger.severe("전체 홈 데이터 로드 실패: " + e.getMessage());
        }
        
        logger.info("총 " + homes.size() + "개의 플레이어 홈 데이터를 로드했습니다.");
        return homes;
    }
    
    /**
     * 플레이어의 홈을 삭제합니다
     */
    public void deletePlayerHome(@NotNull UUID playerUuid) {
        String sql = "DELETE FROM player_homes WHERE player_uuid = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, playerUuid.toString());
            int affected = pstmt.executeUpdate();
            
            if (affected > 0) {
                logger.info("플레이어 " + playerUuid + "의 홈이 삭제되었습니다.");
            }
            
        } catch (SQLException e) {
            logger.severe("플레이어 홈 삭제 실패 (UUID: " + playerUuid + "): " + e.getMessage());
        }
    }
    
    /**
     * 데이터베이스 연결을 종료합니다
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("데이터베이스 연결이 정상적으로 종료되었습니다.");
        }
    }
    
    /**
     * 현재 데이터베이스 연결 상태를 확인합니다
     */
    public boolean isConnected() {
        try (Connection conn = dataSource.getConnection()) {
            return !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
