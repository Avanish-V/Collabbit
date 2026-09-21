package com.iotabuild.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This test class generates a basic startup baseline profile for the target package.
 *
 * We recommend you start with this but add important user flows to the profile to improve their performance.
 * Refer to the [baseline profile documentation](https://d.android.com/topic/performance/baselineprofiles)
 * for more information.
 *
 * You can run the generator with the "Generate Baseline Profile" run configuration in Android Studio or
 * the equivalent `generateBaselineProfile` gradle task:
 * ```
 * ./gradlew :app:generateReleaseBaselineProfile
 * ```
 * The run configuration runs the Gradle task and applies filtering to run only the generators.
 *
 * Check [documentation](https://d.android.com/topic/performance/benchmarking/macrobenchmark-instrumentation-args)
 * for more information about available instrumentation arguments.
 *
 * After you run the generator, you can verify the improvements running the [StartupBenchmarks] benchmark.
 *
 * When using this class to generate a baseline profile, only API 33+ or rooted API 28+ are supported.
 *
 * The minimum required version of androidx.benchmark to generate a baseline profile is 1.2.0.
 **/
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collect(
            packageName = InstrumentationRegistry.getArguments()
                .getString("targetAppId")
                ?: error("targetAppId not passed as instrumentation runner arg"),
            includeInStartupProfile = true
        ) {
            pressHome()
            startActivityAndWait()

            // If the app starts up on the Sign In screen, feed_list will not be present.
            // We only look for and scroll the feed if we successfully bypass the login or if it is already there.
            val hasFeed = device.wait(Until.hasObject(By.res("feed_list")), 5_000)

            if (hasFeed) {
                // Wait for real content (add testTag("feed_item") to your post items)
                device.wait(Until.hasObject(By.res("feed_item")), 5_000)

                scrollFeed(Direction.DOWN, times = 3)
                scrollFeed(Direction.UP, times = 3)
            }
        }
    }

    private fun MacrobenchmarkScope.scrollFeed(direction: Direction, times: Int) {
        repeat(times) {
            try {
                val feed = device.findObject(By.res("feed_list")) ?: return@repeat
                feed.setGestureMargin(device.displayWidth / 5)
                feed.fling(direction)
                device.waitForIdle()
            } catch (_: StaleObjectException) {
                // list changed while scrolling, retry on the next iteration
            }
        }
    }
}