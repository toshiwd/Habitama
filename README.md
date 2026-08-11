# ハビタマ

ハビタマは、結果目標ではなく「今日できる行動」を記録し、できた分を成長へ変換するAndroidアプリです。

v0.7.0では、行動目標を最大10件まで登録できます。行動カレンダーは前月・翌月へ自由に移動でき、移動した月ごとに端末へ同期済みのGoogleカレンダーなどの予定を読み取り専用で表示します。アプリ内のハビタマとランチャーアイコンは、クリーム色の殻に丸いカラースポットを配したオリジナルデザインへ統一しました。

設定画面の「アプリの更新」から最新版を確認できます。更新がある場合はAndroid標準のダウンロード管理でAPKを取得し、SHA-256検証に成功したファイルだけをインストール画面へ渡します。ブラウザでGitHubのリンクを開く必要はありません。

## 技術構成

- Android / Kotlin / Jetpack Compose / Material 3
- Room / Repository / ViewModel / StateFlow
- 日本語表示はAndroid標準のNoto系サンセリフを使用
- UIアイコンはApache License 2.0のMaterial Iconsを使用
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

- `Habitama-0.7.0.apk`
- `version.json`
- `Habitama-0.7.0.sha256`
- `Habitama-0.7.0-cert.txt`

## GitHub Release

公開前にテスト結果、エミュレーター動作、署名鍵のバックアップを確認します。

```powershell
.\scripts\publish-release.ps1
.\scripts\verify-release.ps1
```

公開先は[`toshiwd/Habitama`](https://github.com/toshiwd/Habitama)、今回のタグは`v0.7.0`です。公開時はAPKと`version.json`を必ず同じReleaseへ添付します。

詳細仕様は[開発仕様書 v0.5](docs/ハビタマ_Androidアプリ開発仕様書_v0.5.md)を参照してください。旧仕様は履歴として保持しています。
