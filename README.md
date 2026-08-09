# ハビタマ

ハビタマは、結果目標ではなく「今日できる行動」を記録し、できた分を成長へ変換するAndroidアプリです。

v0.2では、最大3件の行動、まとめて日次報告、部分達成・超過評価、5つの成長ステータス、月間カレンダー、端末内保存を提供します。未報告日は失敗として扱いません。

## 技術構成

- Android / Kotlin / Jetpack Compose / Material 3
- Room / Repository / ViewModel / StateFlow
- `applicationId`: `com.habitama.app`
- `minSdk 26` / `targetSdk 35`
- JDK 17以上、Android SDK 35

## 開発ビルド

```powershell
$env:JAVA_HOME="$env:ProgramFiles\Android\Android Studio\jbr"
$env:ANDROID_HOME="$env:LOCALAPPDATA\Android\Sdk"
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

エミュレーターが起動している場合は次も実行します。

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

## リリース署名

初回だけハビタマ専用鍵を生成します。鍵と`keystore.properties`はGit管理されません。

```powershell
.\scripts\init-release-key.ps1
```

生成後、ユーザープロファイル内の`.android\release-keys\habitama-release.jks`と`keystore.properties`を安全な別媒体へバックアップしてください。鍵またはパスワードを失うと、既存利用者へ上書き更新できません。

署名済みAPKと配布情報を作成します。

```powershell
.\scripts\build-release.ps1
```

成果物はGit管理外の`dist`に生成されます。

- `Habitama-0.2.0.apk`
- `version.json`
- `Habitama-0.2.0.sha256`
- `Habitama-0.2.0-cert.txt`

## GitHub Release

公開前にテスト結果、エミュレーター動作、署名鍵のバックアップを確認します。

```powershell
.\scripts\publish-release.ps1
.\scripts\verify-release.ps1
```

公開先は[`toshiwd/Habitama`](https://github.com/toshiwd/Habitama)、今回のタグは`v0.2.0`です。アプリ内更新確認はまだ含みません。

詳細仕様は[開発仕様書 v0.3](docs/ハビタマ_Androidアプリ開発仕様書_v0.3.md)を参照してください。v0.2仕様は初版の履歴として保持しています。
