/*
 * Copyright (C) 2015 Karumi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.karumi.katasuperheroes;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;

import com.karumi.katasuperheroes.di.MainComponent;
import com.karumi.katasuperheroes.di.MainModule;
import com.karumi.katasuperheroes.model.SuperHero;
import com.karumi.katasuperheroes.model.SuperHeroesRepository;
import com.karumi.katasuperheroes.recyclerview.RecyclerViewInteraction;
import com.karumi.katasuperheroes.ui.view.MainActivity;
import com.karumi.katasuperheroes.ui.view.SuperHeroDetailActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import it.cosenonjaviste.daggermock.DaggerMockRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.assertion.ViewAssertions.selectedDescendantsMatch;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    @Rule
    public DaggerMockRule<MainComponent> daggerRule =
            new DaggerMockRule<>(MainComponent.class, new MainModule()).set(
                    new DaggerMockRule.ComponentSetter<MainComponent>() {
                        @Override
                        public void setComponent(MainComponent component) {
                            SuperHeroesApplication app =
                                    (SuperHeroesApplication) InstrumentationRegistry.getInstrumentation()
                                            .getTargetContext()
                                            .getApplicationContext();
                            app.setComponent(component);
                        }
                    });

    @Rule
    public IntentsTestRule<MainActivity> activityRule =
            new IntentsTestRule<>(MainActivity.class, true, false);

    @Mock
    SuperHeroesRepository repository;

    @Test
    public void doesNotShowEmptyCaseIfThereAreSuperHeroes() {
        givenThereAreSomeSuperHeroes(true);

        startActivity();

        onView(withText("¯\\_(ツ)_/¯")).check(matches(not(isDisplayed())));
    }

    @Test
    public void showsEmptyCaseIfThereAreNoSuperHeroes() {
        givenThereAreNoSuperHeroes();

        startActivity();

        onView(withText("¯\\_(ツ)_/¯")).check(matches(isDisplayed()));
    }

    @Test
    public void doesNotShowProgressBarIfThereAreSuperHeroes() {

        givenThereAreSomeSuperHeroes(false);

        startActivity();

        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())));
    }

    /*
     * It's not going to work due to Expresso way of working
     */
    /*@Test
    public void showsProgressBarIfThereAreNoSuperHeroes() {

        givenThereAreNoSuperHeroes();

        startActivity();

        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()));
    }*/

    @Test
    public void showSuperHeroesNamesIfThereAreSuperHeroes() {

        List<SuperHero> superHeroes = givenThereAreSomeSuperHeroes(true);

        startActivity();

        RecyclerViewInteraction<SuperHero> recyclerViewInteraction = RecyclerViewInteraction.
                onRecyclerView(withId(R.id.recycler_view));

        recyclerViewInteraction
                .withItems(superHeroes)
                .check(new RecyclerViewInteraction.ItemViewAssertion<SuperHero>() {
                    @Override
                    public void check(SuperHero superHero, View view, NoMatchingViewException e) {

                        matches(hasDescendant(withText(superHero.getName()))).check(view, e);
                    }
                });
    }

    @Test
    public void shouldShowAvengerBadgeWhenTheSuperheroIsAnAvenger() {

        //Can be refactored

        List<SuperHero> avengers = givenThereAreSomeSuperHeroes(true);

        startActivity();

        RecyclerViewInteraction<SuperHero> recyclerViewInteraction = RecyclerViewInteraction.
                onRecyclerView(withId(R.id.recycler_view));

        recyclerViewInteraction
                .withItems(avengers)
                .check(new RecyclerViewInteraction.ItemViewAssertion<SuperHero>() {
                    @Override
                    public void check(SuperHero avenger, View view, NoMatchingViewException e) {

                        matches(hasDescendant(allOf(withId(R.id.iv_avengers_badge), isDisplayed())))
                                .check(view, e);
                    }
                });
    }

    @Test
    public void shouldNotShowAvengerBadgeIfSuperheroIsNotAnAvenger() {

        //Can be refactored

        List<SuperHero> notAvengers = givenThereAreSomeSuperHeroes(false);

        startActivity();

        RecyclerViewInteraction<SuperHero> recyclerViewInteraction = RecyclerViewInteraction.
                onRecyclerView(withId(R.id.recycler_view));

        recyclerViewInteraction
                .withItems(notAvengers)
                .check(new RecyclerViewInteraction.ItemViewAssertion<SuperHero>() {
                    @Override
                    public void check(SuperHero avenger, View view, NoMatchingViewException e) {

                        matches(hasDescendant(allOf(withId(R.id.iv_avengers_badge),
                                withEffectiveVisibility(ViewMatchers.Visibility.GONE))))
                                .check(view, e);
                    }
                });
    }

    @Test
    public void openSuperHeroDetailActivityOnRecycler () {

        List<SuperHero> superHeroes = givenThereAreSomeSuperHeroes(true);

        startActivity();

        int indexSelected = 0;

        onView(withId(R.id.recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, ViewActions.click()));

        SuperHero selectedHero = superHeroes.get(indexSelected);
        intended(hasComponent(SuperHeroDetailActivity.class.getCanonicalName()));
        intended(hasExtra("super_hero_name_key", selectedHero.getName()));

    }

    private void givenThereAreNoSuperHeroes() {
        List<SuperHero> emptyList = new ArrayList<>();
        when(repository.getAll()).thenReturn(emptyList);
    }

    private List<SuperHero> givenThereAreSomeSuperHeroes(boolean areAvengers) {

        List<SuperHero> list = new ArrayList<>();

        for (int i = 0; i < 25; i++) {

            SuperHero superHero = new SuperHero("name " + i, null, areAvengers, "desc " + i);

            list.add(superHero);

            when(repository.getByName(superHero.getName())).thenReturn(superHero);
        }

        when(repository.getAll()).thenReturn(list);

        return list;
    }

    private MainActivity startActivity() {
        return activityRule.launchActivity(null);
    }
}