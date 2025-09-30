# LinCall 📱

LinCall is a demo VoIP calling application built on top of the **Linphone SDK**.  
It demonstrates basic SIP calling, conference calls, call history, and system integrations such as battery and bandwidth monitoring.

---

## Features 🚀
- ✅ **Linphone SDK Integration** via local `.aar` kept in the `libs/` folder.
- 🎤 **Permissions**: Uses **RECORD_AUDIO**, **INTERNET**, and **ACCESS_NETWORK_STATE**.
- 📞 **Static Linphone Accounts** used to make and receive SIP calls.
- 📊 **Bandwidth Monitoring** during calls, updating the user in real-time.
- 🔋 **Battery Percentage Check**:
  - Warns the user if the device battery is low (after 5 minutes of ongoing call).
- 🕒 **Call History**:
  - Saved in memory (`Model`) only — not persisted in a database.
- 👥 **Conference Calls**:
  - Ability to merge calls between static SIP users.
- 🔈 **Audio Settings**:
  - Microphone enabled by default.
  - Echo cancellation enabled.
  - Adaptive jitter compensation enabled.

---

## Project Structure 📂
- `libs/` → Contains the Linphone SDK `.aar`.
- `data/linphone/` → LinphoneManager (manages Core, registration, calls).
- `presentation/` → Fragments & UI for calls, history, etc.

---
## Register at Linphone.org & get credentials
SIP_USERNAME=demoUser
SIP_PASSWORD=demoPass
SIP_DOMAIN=sip.linphone.org

## Setup ⚙️
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/LinCall.git
   cd LinCall
