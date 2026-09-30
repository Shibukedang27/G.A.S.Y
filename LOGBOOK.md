# G.A.S.Y build log

This is the running log. I am writing it like I actually worked through it, because that is what happened. Some things worked, some things broke, and a few things looked finished before they were actually finished.

## 2026-09-30 — where we are now

The Android app is installed on the OnePlus 6 and the Mac can control it through ADB. The screen branding is now **GASY** and the wake phrase in the code is **GASY**. Microphone permission is granted and AccessibilityService is enabled from the Mac.

The phone screen comes up and stays alive. The native Qwen2.5 0.5B GGUF model loaded successfully on the real phone after I packaged the missing native libraries. The current model output is still not reliably strict JSON, so I am not pretending the LLM-to-action path is done.

## 2026-09-30 — offline STT step

I changed the speech recognizer request to explicitly prefer offline recognition and added separate offline-ready/error/result logs. This means the app will ask the phone’s installed recognition service to keep recognition on-device instead of silently depending on cloud recognition.

Important honest bit: this is the Android offline-recognition path, not a bundled neural STT model yet. If the OnePlus does not have an English offline language pack installed, Android will still fail or fall back according to the phone’s service. The next check is to run GASY on the phone with internet disabled and see the actual result.

## 2026-10-01 — bundled offline STT completed

The Android offline-preference route was not enough, so I added a real local Vosk engine with the small English model bundled in the app. It unpacks into private phone storage on first use and reads microphone audio locally through AudioRecord.

I installed the new APK on the OnePlus, granted microphone and Accessibility permissions over ADB, started the GASY listener, and got the real phone log: `Vosk offline STT ready`. The app stayed alive with no crash or ANR. This is the first proper offline STT milestone.

Next things after this step: verify that Vosk returns the word GASY from the actual microphone, then connect the recognized command text to the action router and only after that connect the local LLM JSON parser.

## 2026-10-01 — command path expansion

The offline STT is now connected to the existing command path instead of stopping at transcription. GASY detection hands the remaining speech to the router, and the router can now handle app opening, home/back, screenshots, settings, volume, media controls, scrolling, and web search. The app still validates the action before AccessibilityService executes it.

## 2026-10-01 — strict action parser started

Added the first strict JSON boundary for the local model. It accepts only an `actions` array, limits the number of actions, maps types to the approved `Action.Type` enum, and rejects malformed or unknown output before AccessibilityService sees it. The native Qwen planner still needs to be wired into this parser next.

## 2026-10-01 — local Qwen action planning wired

Unknown speech commands now go to the bundled Qwen model on-device, not to a cloud service. The model output passes through the strict JSON parser and only then reaches the action executor. The simple command router remains the fast path; Qwen is the fallback for commands it does not know yet.

## 2026-10-01 — foreground listening completed

Added `GasyListeningService` with a persistent low-priority notification and `START_STICKY`. The Vosk microphone loop now runs from the foreground service, so the UI can leave the foreground without being the only owner of the listener.

Verified on the OnePlus through ADB: Android reported `isForeground=true`, foreground notification ID `42`, the app process stayed alive, and the phone logged `Vosk offline STT ready`.

Also increased retry backoff to five seconds so repeated empty/error microphone cycles do not spin hard and waste battery. The listener still remains available for the next real phrase.

## 2026-10-01 — confirmation boundary added

Added a central action-confirmation policy. Calls, SMS, and destructive-content action types are now classified as sensitive and are rejected at the execution boundary until an explicit confirmation UI is connected. The model cannot silently perform those actions just by producing JSON.

The confirmation boundary is now a real dialog: sensitive actions show Cancel and Confirm buttons, and only Confirm reaches AccessibilityService.

## 2026-10-01 — local voice enrollment added

Added local voice-profile storage and an enrollment button. GASY records a short microphone sample and stores only a small local fingerprint made from amplitude and zero-crossing features. This is a lightweight gate, not a full neural speaker-embedding system yet; the heavier speaker model remains a later upgrade.

The foreground service now checks the stored profile after a wake phrase before sending the wake event to the UI. If no profile exists it keeps compatibility and allows the wake; after enrollment a non-matching sample is dropped locally.

## 2026-10-01 — release build and locked-screen verification

The first release build was rejected by lint because target API 30 is expired. I moved the project to target API 35 and added the microphone foreground-service permission. The unsigned APK also correctly failed installation because it had no certificate, so I signed the local release artifact with the development keystore for device verification.

The signed release APK is about 550 MB because it contains both the Vosk model and Qwen model. It installed on the OnePlus, the app process stayed alive, and Android reported `GasyListeningService isForeground=true` after the listener was started and the screen was locked.

## 2026-10-01 — typed and ADB command controls

Added a command text box and `SEND COMMAND` button to the GASY HUD. Added the `com.agenthitler.COMMAND` activity action so the Mac can send a command directly with ADB using `--es text "open YouTube"`. This gives us a deterministic test path while microphone testing is still being diagnosed.

TTS now prefers an available local US-English female voice to make the response closer to a Siri-like voice. Apple’s proprietary Siri voice itself cannot be installed through Android APIs.

The Mac-speaker end-to-end test still did not produce a GASY transcript; the HUD stayed at `IDLE — waiting for GASY`. I added a six-second bounded Vosk window and final-transcript logging so the next attempt cannot hang silently. This remains an open microphone verification failure, not a claimed success.

The new ADB command path was tested on-device. `settings` successfully opened `com.android.settings/.homepage.SettingsHomepageActivity`. `open YouTube` reached the executor and returned `App not installed: com.google.android.youtube`, which is the correct controlled failure for this phone rather than a silent no-op. The command screen and received-command logging are now part of the test path.

Found and fixed a local-Qwen prompt bug: the JSON example was being sent with literal escape characters, making strict parsing less likely to succeed. The planner now sends a clean JSON schema prompt, retries once on malformed output, and the validator checks required arguments for app, URL, search, and scroll actions.

## 2026-10-01 — quiet ADB-only testing

Paused microphone and speaker testing while the user is asleep. Used only ADB text commands. The command `settings` reached the rule router and opened `com.android.settings/.homepage.SettingsHomepageActivity`; the GASY process stayed alive and there was no crash or ANR. Vosk now logs microphone sample count, average level, peak, and final transcript for a later non-disruptive microphone test.

TTS is configured to prefer an available local US-English female voice. If the phone has no such installed voice, Android keeps its default local voice; Apple’s Siri voice cannot be copied through Android APIs.

Quiet ADB testing showed the microphone is receiving real signal (`avg=81`, `peak=2052` in one run), but Vosk produced `huh` and empty final transcripts instead of GASY. This proves the problem is recognition accuracy/endpointing, not a dead microphone. The Qwen unknown-command path reached the model but returned invalid JSON; raw model output logging is now added for the next fix.

The next ADB Qwen run proved the native model loads (`LOCAL_MODEL_LOAD=true`, `bytes=491400032`) and returns a short 58-character response, but the parser still rejects it. Logging now escapes newlines so the exact response is visible in the next device run.

The next raw response was visible: `[ {"type":"OPEN_APP"}, {"type":"OPEN_URL"}, {"type":"HOME"} ]`. The model ignored the requested object schema and omitted required arguments. The parser now understands array-shaped output for diagnostics, while the prompt explicitly forbids arrays and requires arguments; validation still rejects incomplete OPEN_APP/OPEN_URL actions.

Added deterministic mappings for common phone commands: calculator, phone/dialer, and messages/SMS. These go through the normal package-installed check and return a controlled error if that app is absent instead of sending the request to the weak local planner.

Wake matching now accepts the controlled Vosk variants `gasy`, `gasi`, `gassy`, and `gassie`, while still requiring the whole recognized text to start with one of those phrases. This handles common STT spelling variants without opening the gate for arbitrary speech.

## 2026-10-01 — latest release artifact

Built and signed the latest release APK for device verification. Size is about 551 MB because both local speech and Qwen models are bundled. Installed it on the OnePlus, granted microphone permission, enabled AccessibilityService, started the listener from the UI, and verified Android reports `GasyListeningService isForeground=true` with the process alive.

## What I built

- Native Android project targeting Android 11 / API 30.
- Mac-to-phone workflow using ADB.
- AccessibilityService foundation for phone actions.
- Test command path and custom HUD screen.
- Android speech recognizer wrapper.
- GASY wake-phrase gate.
- Native llama.cpp JNI bridge.
- Qwen2.5 0.5B GGUF local model path.
- Memory/database foundation and modular interfaces for STT, TTS, wake phrase, LLM, and actions.

## Failures and what happened

### Termux llama-server crashed

I first tried to use Termux as a local model server. The Android binary crashed because of a missing NDK linker symbol (`__NDK...hash_memory...`). That route was not stable on this phone. I stopped depending on it and embedded the llama.cpp runtime in the Android app instead.

### Native model failed to load at first

The first APK was missing `libomp.so`. The app installed, but the native model library could not load. I added the OpenMP library to the APK, rebuilt, and then the model loaded on the actual OnePlus.

### Wake listener caused the app to stop

The first wake-listener retry logic restarted SpeechRecognizer immediately inside its callback. When Android returned an error, it created an endless callback/restart loop and eventually caused an ANR/crash. I changed it to use delayed, bounded retries. After that the app stayed alive.

### Android speech recognition was unavailable

The phone reported speech recognition as unavailable even though the Google recognition package existed. The code was hiding that error and just returning to idle. I removed the incorrect early availability gate and added logging around recognizer startup, errors, and results. The listener can now be diagnosed instead of silently pretending everything is fine.

### Mac speaker test did not trigger the phone

I granted permissions, started the GASY listener over ADB, and played “GASY” from the Mac speakers. The app stayed alive, but the phone did not detect the word. This was a real test failure, not a success: the phone may not have been close enough to the speakers, and Android speech recognition may still need a working network/service configuration.

## 2026-10-01 — grammar-constrained Vosk wake path

Vosk API inspection showed grammar-constrained recognition is available. Added a local grammar containing GASY variants plus supported command words and `[unk]`, so wake words are no longer competing against the full English vocabulary. This is intended to address the observed `huh` transcript while keeping command words available after wake.

Installed the grammar-constrained debug APK on the OnePlus and started the listener from the UI over ADB. Verified `GasyListeningService isForeground=true`, process alive, and `Vosk offline STT ready` with no crash or ANR.

Added a boot receiver so the opted-in GASY foreground listener starts again after device reboot. This closes the restart/background lifecycle gap; Android still requires the user to have granted microphone and Accessibility access beforehand.

Fixed multi-action execution: validated plans now execute sequentially, stop immediately on the first failure, and show one confirmation dialog if any action in the plan is sensitive. Previously only the first action was executed.

Added a user-controlled battery optimization flow. The HUD now has `ALLOW GASY BACKGROUND BATTERY`, which opens Android’s per-app battery exemption screen, with a settings fallback if the direct intent is unavailable.

## What is not finished yet

- Real low-power neural wake-word model. Current GASY detection uses Android SpeechRecognizer, so it is not yet an always-on offline keyword engine.
- Speaker matching/enrollment.
- Strict JSON extraction/validation from the local model.
- Full command vocabulary and reliable end-to-end voice action testing.
- Background/foreground-service hardening for long-running listening.

## Next sensible test

Put the phone close to the Mac speaker, start **START GASY WAKE LISTENER**, say “GASY”, and check whether the screen changes to `WAKE_DETECTED gasy`. If it does not, capture the recognizer error and fix that layer before adding more LLM features.
# 2026-10-01 — JSON parser unit-test failure and fix

- First local test run failed: `8 tests completed, 2 failed` in the new parser tests.
- Root cause was two separate things: array-shaped model output was not being detected by the parser, and Android's platform `org.json` methods are not mocked in plain JVM unit tests.
- Fixed parser boundary detection for both `{...}` and `[...]` output and kept validation at the execution boundary.
- Reworked the JVM tests to cover the pure validator (safe plan accepted, missing required argument rejected). Full `gradle test --no-daemon` now passes: debug and release unit tests successful.
- Honest limitation: raw Android `org.json` parsing still needs an on-device/instrumented test; this local run does not pretend to prove it.

# 2026-10-01 — Quiet ADB smoke check

- OnePlus ADB serial `fc8f4a02` was online.
- Debug APK assembled, installed with `adb install -r`, and returned `Success`.
- Typed ADB command `open settings` launched `com.agenthitler/.MainActivity`; no microphone or speaker test was run while the user was sleeping.

# 2026-10-01 — Qwen native output and accessibility verification

- Correctly quoted ADB text reached the app as the full command `launch app`.
- The bundled 0.5B Qwen model loaded on-device (`LOCAL_MODEL_LOAD=true`, `491400032` bytes).
- Its native output was prompt-echoed, but the final approved fragment was `{"action":"RECENTS"}`. The parser now accepts that single-action form only when it is a known approved action; no retry was triggered.
- The action did not execute because the phone's Accessibility service was disabled/crashed (`AccessibilityService unavailable, sir.` and Android reported the service in `Crashed services`). This is a real device blocker, not marked as success. It needs repair/re-enable before full phone-action verification.
