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

## What is not finished yet

- Real low-power neural wake-word model. Current GASY detection uses Android SpeechRecognizer, so it is not yet an always-on offline keyword engine.
- Speaker matching/enrollment.
- Strict JSON extraction/validation from the local model.
- Full command vocabulary and reliable end-to-end voice action testing.
- Background/foreground-service hardening for long-running listening.

## Next sensible test

Put the phone close to the Mac speaker, start **START GASY WAKE LISTENER**, say “GASY”, and check whether the screen changes to `WAKE_DETECTED gasy`. If it does not, capture the recognizer error and fix that layer before adding more LLM features.
