# ULTRON Companion — Implementation Status

## v1.1 — All modules implemented and fully wired

### Services
- **ScreenMirrorService** — MediaProjection foreground service; JPEG frames routed via `StreamManager` (backpressure-aware, drops oldest frame on overflow instead of flooding slow connections).
- **SmsCallService** + **SmsReceiver** — SMS BroadcastReceiver + call-state foreground service; both use `UltronClient.sendSms` / `sendCallState`.
- **NotificationSyncService** — `NotificationListenerService`; forwards events via `UltronClient.sendNotification` using `MessageProtocol.NotificationMessage`.
- **FileAccessService** — SAF URI-based chunked file transfer via `UltronClient.sendFileChunk`.

### System helpers — all instantiated
- **BatteryMonitor** — polled on `auth_ok` + scheduled via `BatteryReportWorker` every 6 h.
- **DeviceInfoCollector** — sent on every `auth_ok`.
- **ClipboardSync** — wired in `UltronClient.applyClipboardSync()`, toggled from `SettingsScreen` via `PreferencesManager.clipboardSyncEnabled`; auto-starts on reconnect if the preference is on.
- **WakeWordDetector** — wired in `EyesEarsService.onCreate()` when `RECORD_AUDIO` is granted; triggers `wake_word_detected` command and updates the notification.

### Network
- **MessageProtocol** — used in `UltronClient` for `sendSms`, `sendNotification`, `sendClipboard`, `sendBatteryStatus`. `FileChunkMessage` constructor bug fixed (`val` on index/total). `BatteryMessage` now includes optional `temperatureC`.
- **StreamManager** — instantiated in `ScreenMirrorService`; `offer()` accepts an `extras` map so callers can pass `"format":"jpeg"` etc. `UltronClient.sendRaw()` is the bridge between StreamManager and the WebSocket.

### UI
- **SettingsScreen** (ui/screens/) — canonical version; duplicate local composable removed from `MainActivity`. Screen now shows Clipboard Sync toggle backed by `PreferencesManager`.
- **StreamingPreviewScreen** — unchanged; correctly wired to `ScreenMirrorState` flow.

### Workers
- **BatteryReportWorker** — scheduled in `UltronConnectionService`.
- **NotificationSyncWorker** — fixed: ensures WebSocket connection is alive (connect + 3 s wait + retry), so `NotificationSyncService` can forward events without interruption.
- **BootReceiver** — schedules `ReconnectWorker` on boot.

### Wiring fixes applied (v1.1)
| Was | Fixed |
|---|---|
| `SettingsScreen.kt` (screens/) was dead code | Replaced with full composable; local duplicate removed from `MainActivity` |
| `ClipboardSync` defined but never instantiated | Wired in `UltronClient.applyClipboardSync()`, toggled from Settings + auto on reconnect |
| `WakeWordDetector` defined but never instantiated | Wired in `EyesEarsService.onCreate()` |
| `MessageProtocol` unused; `FileChunkMessage` constructor bug | Used in `UltronClient`; `val` added to `index`/`total`; `temperatureC` added to `BatteryMessage` |
| `StreamManager` defined but never instantiated | Wired in `ScreenMirrorService`; `sendRaw()` added to `UltronClient` |
| `PreferencesManager` unused | Backing store for clipboard-sync toggle in `SettingsScreen` + checked in `UltronClient` on `auth_ok` |
| `NotificationSyncWorker` just called `connect()` with no retry | Proper connect + delay + `isConnected()` check with `Result.retry()` |

### Build note
A Gradle build was not attempted in this sandbox (services.gradle.org is unreachable). The source has been reviewed for correctness; it is ready for `./gradlew assembleDebug` in a normal dev environment.
