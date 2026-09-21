# Splash Screen Update

This plan updates the splash screen to use the "Collabbit" brand color (primary) and logo.

## Proposed Changes

### [res/values/colors.xml](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/res/values/colors.xml)

#### [MODIFY] [colors.xml](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/res/values/colors.xml)
- Add the primary brand color `#3461FD` to the XML resources so it can be used in the splash screen theme.

### [res/values/SplashScreen.xml](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/res/values/SplashScreen.xml)

#### [MODIFY] [SplashScreen.xml](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/res/values/SplashScreen.xml)
- Update `windowSplashScreenBackground` to use `@color/primary`.
- Update `windowSplashScreenAnimatedIcon` to use `@drawable/collabit`.
- Update `windowSplashScreenIconBackgroundColor` to use `@color/primary`.

## Verification Plan

### Manual Verification
- Deploy the app to a device or emulator.
- Observe the splash screen during startup to ensure the background is blue (`#3461FD`) and the logo is the Collabbit logo.
