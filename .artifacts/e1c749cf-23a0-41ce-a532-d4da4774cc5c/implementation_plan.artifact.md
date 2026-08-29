# Implementation Plan - Share Feature with Deeplink for Skill Course

Implement a sharing feature for courses that generates a deep link, allowing users to share courses with others who can then open the app directly to that specific course.

## Proposed Changes

### [Opportunities Feature]

#### [MODIFY] [CourseDetailScreen.kt](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/Feature/Opportunities/presentation/CourseDetailScreen.kt)
- Add a `Share` icon to the `TopAppBar`.
- Implement `shareCourse` function using `Intent.ACTION_SEND`.
- Generate deep link: `https://www.campuscircle.in/course/{courseId}`.

### [Navigation & Main Activity]

#### [MODIFY] [MainActivity.kt](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/java/com/iota/campusX/MainActivity.kt)
- Update the `CourseDetail` composable in `NavHost` to support deep links using `navDeepLink<CourseDetail>`.

#### [MODIFY] [AndroidManifest.xml](file:///D:/Backend/Collabbit/finder-mobile/app/src/main/xml/AndroidManifest.xml)
- Add `<intent-filter>` to `MainActivity` to handle `https://www.campuscircle.in/course` links.

## Verification Plan

### Manual Verification
1.  **Share Feature**: Open a course, click the share icon, and verify the generated text contains the correct link.
2.  **Deeplink Handling**:
    *   Install the app.
    *   Send the link to yourself (e.g., via WhatsApp or Keep).
    *   Click the link and verify the app opens directly to the `CourseDetailScreen` for that specific course.
    *   Test both when the app is in the background and when it's closed.
