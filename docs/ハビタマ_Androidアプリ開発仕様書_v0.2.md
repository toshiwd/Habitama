# ハビタマ Androidアプリ 開発仕様書 v0.2

- 更新日: 2026-08-10
- 対象: Android / Kotlin / Jetpack Compose
- Phase 0: 目標1件、当日入力、部分達成、超過、累積エネルギー、7日履歴、端末保存

## 1. プロダクト概要

ハビタマは、結果目標ではなく、結果につながる「今日の行動」を実行・記録するアプリである。二択評価を避け、できた分だけを成長へ変換する。他人とは比較せず、空白や復帰を罰しない。

## 2. Phase 0の範囲

### 実装する

- 行動目標1件の作成
- 非負整数による当日実績入力
- 部分達成と目標超過の評価
- 獲得・累積エネルギー表示
- 同日記録の更新
- 直近7暦日の履歴
- 目標変更の翌日予約
- Roomによる端末内保存
- 署名済みAPKとGitHub Release配布

### 実装しない

- 偏差値、調子、5ステータス、チケット、シーズン
- 朝夜通知、過去日入力、複数目標
- 鳥キャラクター、巣、世代交代
- Health連携、AI、ログイン、クラウド同期
- アプリ内アップデート確認

## 3. 画面と遷移

1. 初回の目標作成
   - 行動名、目標値、単位を入力する。
   - 「6,000歩」「20分」「10回」の例から入力を補助できる。
2. ホーム兼実績入力
   - 今日の目標、累積エネルギー、当日記録を表示する。
   - 今日できた分を入力し、保存または更新する。
   - 目標変更と7日履歴へ移動できる。
3. 結果
   - 実績、表示達成率、獲得エネルギー、累積エネルギーを表示する。
   - 未達は責めず、できた分が成長になったことを示す。
4. 直近7日履歴
   - 今日を含む7暦日を新しい順に表示する。
   - 未報告日は「記録なし」と表示し、失敗や連続切れとして扱わない。

目標編集には目標作成画面を再利用する。編集内容は翌日の目標版として保存し、当日目標と既存記録を変更しない。同日に再編集した場合は翌日予約を置き換える。

## 4. 入力契約

- 行動名: 前後空白を除いて1〜40文字
- 目標値: `1..999999999`の整数
- 実績値: `0..999999999`の整数
- 単位: 前後空白を除いて1〜10文字
- 難易度: Phase 0では内部値3に固定
- 記録可能日: 端末の現在タイムゾーンにおける今日のみ
- 過去日・未来日入力、小数、負数は受け付けない

日付はISOローカル日付`YYYY-MM-DD`で保存する。アプリ起動中に日付やタイムゾーンが変わった場合は再起動後のローカル日付を正とする。

## 5. 評価計算

```text
rawProgress = actualValue / targetValue
base = min(rawProgress, 1.0)
overBonus = max(0, min(rawProgress - 1.0, 0.5)) * 0.4
evaluationScore = base + overBonus
displayPercentage = round(rawProgress * 100)
energyEarned = round(10 * evaluationScore)
```

- 評価値は最大1.2、獲得エネルギーは1日最大12。
- 表示達成率には上限を設けない。
- Kotlinの`roundToInt`を丸め規則とする。
- 累積エネルギーは全日次記録の`energyEarned`合計であり、別の可変カウンターを持たない。
- 0実績は評価0、エネルギー0。負の値は発生しない。

## 6. データ契約

### GoalEntity

- `id`: 自動採番主キー
- `title`, `targetValue`, `unit`
- `difficulty`: Phase 0では3
- `effectiveFrom`, `effectiveTo`: 目標版の有効期間

### DailyGoalRecordEntity

- `date`: ISOローカル日付の主キー
- `goalId`: 当日有効だった目標
- `actualValue`
- `targetValueSnapshot`, `titleSnapshot`, `unitSnapshot`, `difficultySnapshot`
- `evaluationScore`, `displayPercentage`, `energyEarned`
- `updatedAtEpochMillis`

同日保存は主キー`date`によるupsertとし、トランザクション内で目標取得、計算、保存を行う。更新時は行を置き換えるため、累積エネルギーを二重加算しない。

## 7. UI方針

- 背景`#F7F8FA`、文字`#202124`、主色`#2374E1`、達成`#36A269`、強調`#F3A712`
- 日常画面はフラットにし、結果画面だけエネルギーコアを強調する
- 操作領域は48dp以上
- 色だけで状態を伝えず、必ず文言を併記する
- Phase 0はライトテーマ。システム文字サイズに追従する
- 未達・未報告を責める文言、連続記録切れの演出は使用しない

## 8. 技術構成

- 単一Activity、単一`app`モジュール
- Compose / Material 3 / Navigation Compose
- ViewModel / StateFlow / Repository
- Room 1データベース、2テーブル
- 計算処理はAndroid依存のない純粋関数
- Hilt等のDIフレームワークは使用しない
- `Clock`をRepositoryへ注入可能にして日付テストを固定する

## 9. APK配布

- `applicationId`: `com.habitama.app`
- 初版: `versionName 0.1.0`、`versionCode 1`
- ハビタマ専用リリース鍵を使用し、他アプリの鍵を再利用しない
- 鍵とパスワードはGit管理外。証明書SHA-256だけを検証記録へ残す
- APKはGitへコミットせず、GitHub Release assetとして配布する
- ReleaseにはAPKと`version.json`を添付する
- 公開後にlatest `version.json`とAPKを実取得し、バージョン、URL、SHA-256、署名を照合する

`version.json`の契約:

```json
{
  "version": "0.1.0",
  "versionCode": 1,
  "apkUrl": "https://github.com/toshiwd/Habitama/releases/download/v0.1.0/Habitama-0.1.0.apk",
  "sha256": "UPPERCASE_HEX",
  "publishedAt": "ISO-8601 UTC",
  "minSdk": 26
}
```

## 10. 受入条件

- `0/6000 → 0%、評価0、エネルギー0`
- `5000/6000 → 83%、評価約0.8333、エネルギー8`
- `6000/6000 → 100%、評価1.0、エネルギー10`
- `9000/6000 → 150%、評価1.2、エネルギー12`
- `10000/6000 → 167%表示、評価1.2、エネルギー12`
- 同日更新後も記録は1件で、累積エネルギーは最新値の合計になる
- 再起動後も目標、記録、累積エネルギーが残る
- 翌日目標変更後も過去記録のスナップショットが変わらない
- 単体テスト、Roomテスト、Compose UIテスト、lint、debug/releaseビルドが成功する
- API 35エミュレーターで4画面と再起動保存を確認する
- 公開APK、latest `version.json`、SHA-256、署名証明書が一致する
