# Walkthrough - Deep Link & Share Implementation (Custom Scheme)

I have implemented **Option B** for deep linking, using a custom URI scheme to ensure the app intercepts the share links without involving a mobile browser.

## Changes Made

### 1. Custom Scheme Implementation in `CourseDetailScreen.kt`
- Updated the `shareCourse` function to generate a link using the `finder://` scheme instead of `https://`.
- **Shared Link Format**: `finder://course/{courseId}`.
- This ensures that when the link is clicked, Android looks specifically for an app registered for the `finder` scheme, bypassing Chrome or other browsers.

### 2. Manifest Configuration in `AndroidManifest.xml`
- Registered a specialized `<intent-filter>` for the `finder` scheme.
- Set `android:scheme="finder"` and `android:host="course"` to match the generated links.

### 3. Navigation Support in `MainActivity.kt`
- Added `navDeepLink<CourseDetail>(basePath = "finder://course")` to the `NavHost`.
- The app is now fully capable of parsing these custom URIs and navigating directly to the correct course.

## Benefits of Option B
- **No Browser Redirects**: Since there is no web app, this avoids the "Page not found" or "Loading browser" step entirely.
- **Immediate Navigation**: The app opens directly to the content.

> [!NOTE]
> Some messaging apps (like SMS or simple text editors) may not automatically "blue-link" custom schemes like `finder://` as they do with `https://`. If you find the link is not clickable in a specific app, you can switch back to Option A with a verified `assetlinks.json` file.
