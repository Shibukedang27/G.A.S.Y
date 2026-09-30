# G.A.S.Y

This is my Android phone-assistant build for the OnePlus 6. Mac is the control/build machine and the phone is the runtime device. No root.

Right now the app has the basic phone-control foundation, AccessibilityService, voice-listening path, a custom screen, and a local Qwen 0.5B model path. The model is kept out of GitHub because it is too large; it gets copied into the app during the local build.

The honest status: the app launches and the native model loads on the actual phone. The wake listener is wired to Android’s offline-preferred speech recognizer and the phrase is **GASY**. I still need to confirm that the OnePlus has the required offline language pack and finish the reliable speech-to-action path.

See [LOGBOOK.md](LOGBOOK.md) for what happened, including the failures. I am keeping the failures here because hiding them makes debugging slower.
