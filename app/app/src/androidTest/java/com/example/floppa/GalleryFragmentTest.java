package com.example.floppa;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

public class GalleryFragmentTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule =
        new ActivityTestRule<>(MainActivity.class);

    @Before
    public void changeActivityToFragment() {
        mActivityTestRule.getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, GalleryFragment.newInstance())
                .commit();
    }

    @Test
    public void onPhotoItemLongClickCheck() {
        // long clicking on item must change visibility of toolbar items
        onView(withId(R.id.rv_cards)).perform(RecyclerViewActions.actionOnItemAtPosition(0, longClick()));
        onView(withId(R.id.iv_add)).check(matches(not(isDisplayed()))); // check that add button turns off
        onView(withId(R.id.iv_delete)).check(matches(isDisplayed()));   // check that delete button turns on

        // after that long-clicking on other element mustn't change anything
        onView(withId(R.id.rv_cards)).perform(RecyclerViewActions.actionOnItemAtPosition(1, longClick()));
        onView(withId(R.id.iv_add)).check(matches(not(isDisplayed())));
        onView(withId(R.id.iv_delete)).check(matches(isDisplayed()));

        // same for clicking on other element or clicking at first element
        onView(withId(R.id.rv_cards)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.iv_add)).check(matches(not(isDisplayed())));
        onView(withId(R.id.iv_delete)).check(matches(isDisplayed()));

        onView(withId(R.id.rv_cards)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
        onView(withId(R.id.iv_add)).check(matches(not(isDisplayed())));
        onView(withId(R.id.iv_delete)).check(matches(isDisplayed()));

        // but secondary long-clicking on first element must change visibility of toolbar items back
        onView(withId(R.id.rv_cards)).perform(RecyclerViewActions.actionOnItemAtPosition(0, longClick()));
        onView(withId(R.id.iv_add)).check(matches(isDisplayed()));
        onView(withId(R.id.iv_delete)).check(matches(not(isDisplayed())));

    }

    @Test
    public void onAddButtonCheck() {
        onView(withId(R.id.iv_add)).perform(click());
        onView(withId(R.id.bottom_sheet_container)).check(matches(isDisplayed()));
    }
}