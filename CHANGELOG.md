# Immersive Hotbar 1.2.2 - Fabric | Forge | NeoForge
### Improvements
- Improved action bar animations for rapidly updating text.
  - Changed values now briefly flash instead of repeatedly bouncing the entire action bar.
- Improved detection between live HUD updates and new action bar messages.
- Fixed excessive action bar bouncing on servers with frequently updating HUD elements.
- Added additional XP animation customization. (Adds Suggestion [#29](https://github.com/DerpDerpling/Immersive-Hotbar/issues/29))
  - Added **XP Text Pulse Scale**.
  - Added **XP Text Pulse Speed**.
  - Added **XP Text Pulse Return Speed**.
  - Added **Level-Up Particle Amount**.
  - Added **Level-Up Particle Size**.
  - Added **XP Bar Pulse Speed**.
  - Added **XP Bar Pulse Size**.
  - Added **XP Bar Pulse Lines**.
  - Added an option to keep the XP glow tail within the XP bar.
# Immersive Hotbar 1.2.1 - NeoForge
- Fix missing lang files in config

# Immersive Hotbar 1.2.1 - Fabric
- Fixed a crash involving consumables on 1.21.2 and above. (Fixes [#30](https://github.com/DerpDerpling/Immersive-Hotbar/issues/30), [#31](https://github.com/DerpDerpling/Immersive-Hotbar/issues/31))

# Immersive Hotbar 1.2.1 - Forge
- Fixed a startup crash on Forge 1.20.1 (Fixes [#27](https://github.com/DerpDerpling/Immersive-Hotbar/issues/27))

# Immersive Hotbar 1.2

- Refactored most of the codebase to make future feature additions, maintenance, and version updates easier.
- Forge 1.20.1 is no longer in beta
- Neoforge is also no longer in beta
- Added Action Bar animations that follow the same animation style as tooltips.
- Moved Tooltip animation settings from the General tab into its own Tooltip tab.
- Added new Tooltip configuration options:
    - **Bounce on Item Change**:  controls whether the tooltip bounces when switching items.
    - **Shrink on Empty Slot**: controls whether the tooltip shrinks when switching to an empty slot.
    - **Tooltip Bounce Scale**: controls how large the tooltip grows when it appears or updates.
    - **Tooltip Animation Speed**: controls how quickly the tooltip returns to its normal size.
    - **Tooltip Shrink Speed**: controls how quickly the tooltip shrinks away.
    - **Minimum Fade Scale**: controls how small the tooltip becomes before disappearing.
- Added **XP Bar Pulse**, which creates a pulse around the entire XP bar when leveling up.
    - The level interval is configurable.
    - The pulse color can also be customized, similar to the XP bar glow.
- Changed several default configuration values.
- Updated the limits of several config options to allow for more customization. (Adds Suggestion [#26](<https://github.com/DerpDerpling/Immersive-Hotbar/issues/26>))