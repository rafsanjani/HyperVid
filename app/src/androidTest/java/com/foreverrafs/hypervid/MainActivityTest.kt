package com.foreverrafs.hypervid

import android.os.Environment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.foreverrafs.hypervid.util.EspressoIdlingResourceRule
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import timber.log.Timber

/* Created by Rafsanjani on 21/04/2020. */

@RunWith(AndroidJUnit4ClassRunner::class)
class MainActivityTest {
    @get:Rule
    val scenario: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val idlingResourceRule = EspressoIdlingResourceRule()


    @Before
    fun dismissDialog() {
        try {
            onView(withId(android.R.id.button2)).perform(click())
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    @Test
    fun test_enableAddButtonWhenValidLinkIsTyped() {
        onView(withId(R.id.btnAddToDownloads)).check(matches(not(isEnabled())))
        onView(withHint(R.string.hint_url)).perform(typeText("https://www.facebook.com/Magraheb/videos/228399154931246/?t=0"))
        onView(withId(R.id.btnAddToDownloads)).check(matches(isEnabled()))
    }

    @Test
    fun test_enableAddButtonWhenValidLinkIsTypedMobile() {
        onView(withId(R.id.btnAddToDownloads)).check(matches(not(isEnabled())))
        onView(withHint(R.string.hint_url)).perform(typeText("https://m.facebook.com/story.php?story_fbid=2220724671544723&id=100008216348411"))
        onView(withId(R.id.btnAddToDownloads)).check(matches(isEnabled()))
    }

    @Test 
    fun test_disableAddButtonWhenInvalidLinkIsTyped() {
        onView(withId(R.id.btnAddToDownloads)).check(matches(not(isEnabled())))
        onView(withHint(R.string.hint_url)).perform(typeText("https://www.foreverrafs.com/Magraheb/videos/228399154931246/?t=0"))
        onView(withId(R.id.btnAddToDownloads)).check(matches(not(isEnabled())))
    }
}