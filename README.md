# ConveniencePlugin

> 현대적 아키텍처와 데이터베이스 연동을 통한 마인크래프트 서버 편의성 플러그인

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Paper](https://img.shields.io/badge/Paper-1.21.4-blue.svg)](https://papermc.io/)
[![SQLite](https://img.shields.io/badge/Database-SQLite-blue.svg)](https://www.sqlite.org/)
[![HikariCP](https://img.shields.io/badge/Pool-HikariCP-green.svg)](https://github.com/brettwooldridge/HikariCP)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

## 🚀 주요 기능

### 🏠 텔레포트 명령어
- **`/spawn` (`/스폰`)** - 서버 스폰 지점으로 텔레포트
- **`/sethome` (`/집설정`)** - 현재 위치를 집으로 설정
- **`/home` (`/집`)** - 설정한 집으로 텔레포트
- **`/delhome` (`/집삭제`)** - 설정한 집을 삭제
- **`/status` (`/상태`)** - 플러그인 및 데이터베이스 상태 확인

### ✨ 특별한 기능들
✅ **다국어 지원** - 영어/한글 명령어 모두 사용 가능  
✅ **SQLite 데이터베이스** - 안정적이고 빠른 데이터 저장  
✅ **HikariCP Connection Pool** - 최적화된 데이터베이스 연결 관리  
✅ **자동 데이터 마이그레이션** - 기존 YAML 데이터 자동 이전  
✅ **확장 가능한 아키텍처** - 미래 기능 추가 용이  
✅ **현대적 Java 21** - 최신 언어 기능 활용  
✅ **완벽한 에러 처리** - 사용자 친화적인 오류 메시지  

## 📋 시스템 요구사항

- **Java**: 21 이상
- **서버 소프트웨어**: Paper 1.21.4 (또는 Spigot 1.21.4)
- **마인크래프트**: 1.21.4

## 🛠️ 설치 방법

1. **플러그인 다운로드**
   ```bash
   # 릴리스 페이지에서 최신 .jar 파일 다운로드
   # 또는 직접 빌드:
   ./gradlew build
   ```

2. **서버에 설치**
   ```bash
   # 플러그인을 서버의 plugins 폴더에 복사
   cp ConveniencePlugin-1.0.jar /your/server/plugins/
   ```

3. **서버 재시작**
   - 서버를 재시작하면 플러그인이 자동으로 로드됩니다

## 📖 사용법

### 명령어 목록

| 영어 명령어 | 한글 명령어 | 설명 | 권한 |
|------------|------------|------|------|
| `/spawn` | `/스폰` | 서버 스폰 지점으로 텔레포트 | 기본 |
| `/sethome` | `/집설정` | 현재 위치를 집으로 설정 | 기본 |
| `/home` | `/집` | 설정한 집으로 텔레포트 | 기본 |
| `/delhome` | `/집삭제` | 설정한 집을 삭제 | 기본 |
| `/status` | `/상태` | 플러그인 상태 및 통계 확인 | 기본 |

### 사용 예시

```mcfunction
# 집 관리
/sethome          # 현재 위치를 집으로 설정
/home             # 집으로 이동
/delhome          # 집 삭제

# 한글 명령어도 동일하게 작동
/집설정            # 집 설정
/집               # 집으로 이동  
/집삭제            # 집 삭제

# 기타 기능
/spawn            # 스폰으로 이동
/status           # 플러그인 상태 확인
```

### 상태 명령어 상세 정보

`/status` 명령어는 다음 정보를 제공합니다:
- 데이터베이스 연결 상태
- 전체 플레이어 집 개수
- 본인의 집 설정 상태
- 관리자 권한 시 추가 기술 정보

## 🏗️ 개발 정보

### 기술 스택
- **언어**: Java 21 (현대적 언어 기능 활용)
- **빌드 도구**: Gradle 8.8
- **API**: Paper API 1.21.4  
- **데이터베이스**: SQLite 3.46.1.3 (안정화된 JDBC 드라이버)
- **Connection Pool**: HikariCP 5.1.0 (Spring Boot 기본 채택)
- **아키텍처**: 관심사 분리 (DatabaseManager 별도 클래스)

### 프로젝트 구조
```
src/
├── main/
│   ├── java/com/github/jw010801/conveniencePlugin/
│   │   ├── ConveniencePlugin.java          # 메인 플러그인 클래스
│   │   └── DatabaseManager.java           # 데이터베이스 관리 클래스
│   └── resources/
│       └── paper-plugin.yml                # 플러그인 메타데이터
├── build.gradle                            # 빌드 설정 (HikariCP, SQLite 의존성)
├── LICENSE                                 # MIT 라이선스
└── README.md                              # 프로젝트 문서
```

### 데이터베이스 정보

#### 자동 마이그레이션
- 첫 실행 시 기존 `homes.yml` 파일을 자동으로 SQLite로 이전
- 마이그레이션 완료 후 기존 파일을 `homes.yml.backup`으로 안전하게 백업
- 데이터 손실 없이 안전한 업그레이드 보장

#### 데이터베이스 스키마
```sql
CREATE TABLE player_homes (
    player_uuid TEXT PRIMARY KEY,
    world_name TEXT NOT NULL,
    x DOUBLE NOT NULL,
    y DOUBLE NOT NULL,
    z DOUBLE NOT NULL,
    yaw REAL NOT NULL,
    pitch REAL NOT NULL,
    created_at INTEGER NOT NULL,
    updated_at INTEGER NOT NULL
);
```

#### 파일 위치
- 데이터베이스 파일: `plugins/ConveniencePlugin/homes.db`
- 백업 파일: `plugins/ConveniencePlugin/homes.yml.backup` (마이그레이션 시)

### 로컬 개발 환경 구축

```bash
# 저장소 복제
git clone https://github.com/jw010801/convenience-plugin.git
cd convenience-plugin

# 의존성 설치 및 빌드
./gradlew build

# 테스트 서버 실행 (Paper Run Task)
./gradlew runServer
```

## 📈 개발 현황 및 향후 계획

### ✅ 완료된 기능
- [x] **SQLite 데이터베이스 연동** - HikariCP Connection Pool 적용
- [x] **자동 데이터 마이그레이션** - 기존 YAML 데이터 안전한 이전
- [x] **확장 가능한 아키텍처** - DatabaseManager 클래스 분리
- [x] **상태 모니터링** - `/status` 명령어로 시스템 확인
- [x] **완전한 CRUD** - 집 생성/조회/삭제 지원

### 🔄 계획 중인 기능
- [ ] **다중 홈 지원** - 이름별 여러 집 위치 설정 (`/sethome 집1`)
- [ ] **권한 시스템** - 관리자/사용자 권한 분리 및 VIP 기능
- [ ] **쿨다운 시스템** - 텔레포트 남용 방지 (3-5초 대기)
- [ ] **GUI 인터페이스** - 인벤토리 기반 집 관리 메뉴
- [ ] **웹 API** - REST API를 통한 외부 시스템 연동
- [ ] **MySQL 지원** - 멀티 서버 환경을 위한 중앙 데이터베이스

## 🤝 기여하기

이 프로젝트는 포트폴리오 목적으로 제작되었지만, 개선 제안이나 버그 리포트는 언제나 환영입니다!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 👤 개발자

**jw010801**
- GitHub: [@jw010801](https://github.com/jw010801)
- 프로젝트 링크: [convenience-plugin](https://github.com/jw010801/convenience-plugin)

---

⭐ 이 프로젝트가 도움이 되었다면 별표를 눌러주세요!
