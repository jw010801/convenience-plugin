# ConveniencePlugin

> 마인크래프트 서버 편의성을 위한 기본 명령어들을 제공하는 플러그인

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Paper](https://img.shields.io/badge/Paper-1.21.4-blue.svg)](https://papermc.io/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

## 🚀 주요 기능

### 텔레포트 명령어
- **`/spawn` (`/스폰`)** - 서버 스폰 지점으로 텔레포트
- **`/sethome` (`/집설정`)** - 현재 위치를 집으로 설정
- **`/home` (`/집`)** - 설정한 집으로 텔레포트

### 특별한 기능들
✅ **다국어 지원** - 영어/한글 명령어 모두 사용 가능  
✅ **데이터 영속성** - 서버 재시작 후에도 집 위치 유지  
✅ **안전한 에러 처리** - 사용자 친화적인 오류 메시지  
✅ **현대적 구현** - Paper API 1.21.4 기반  

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

| 영어 명령어 | 한글 명령어 | 설명 | 사용 예시 |
|------------|------------|------|----------|
| `/spawn` | `/스폰` | 스폰 지점으로 이동 | `/spawn` |
| `/sethome` | `/집설정` | 현재 위치를 집으로 설정 | `/sethome` |
| `/home` | `/집` | 설정한 집으로 이동 | `/home` |

### 사용 예시

```mcfunction
# 집 설정하기
/sethome
# 또는
/집설정

# 집으로 이동하기
/home
# 또는
/집

# 스폰으로 이동하기
/spawn
# 또는
/스폰
```

## 🏗️ 개발 정보

### 기술 스택
- **언어**: Java 21
- **빌드 도구**: Gradle
- **API**: Paper API 1.21.4
- **데이터 저장**: YAML (향후 SQLite 연동 예정)

### 프로젝트 구조
```
src/
├── main/
│   ├── java/com/github/jw010801/conveniencePlugin/
│   │   └── ConveniencePlugin.java          # 메인 플러그인 클래스
│   └── resources/
│       └── paper-plugin.yml                # 플러그인 메타데이터
├── build.gradle                            # 빌드 설정
└── README.md                              # 프로젝트 문서
```

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

## 📈 향후 계획

- [ ] **SQLite 데이터베이스 연동** - 더 효율적인 데이터 관리
- [ ] **다중 홈 지원** - 여러 집 위치 설정 가능
- [ ] **권한 시스템** - 관리자/사용자 권한 분리
- [ ] **쿨다운 시스템** - 텔레포트 남용 방지
- [ ] **GUI 인터페이스** - 인벤토리 기반 메뉴 시스템
- [ ] **MySQL 지원** - 대용량 서버를 위한 확장성

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
