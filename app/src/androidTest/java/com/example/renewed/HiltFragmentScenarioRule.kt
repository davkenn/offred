package com.example.renewed

import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle

import dagger.hilt.EntryPoints
import org.junit.rules.ExternalResource
import kotlin.reflect.KClass

class HiltFragmentScenarioRule<F : Fragment>(private val fragmentClass: KClass<F>) :
        ExternalResource() {

        var fragmentScenario: HiltFragmentScenario<F>? = null
        private val postLaunchCallbacks = mutableListOf<(F) -> Unit>()

        /**
         * Launches the fragment in [Lifecycle.State.INITIALIZED] state with the given arguments.
         * Ensure you set your application theme as [themeId] to prevent runtime resource issues.
         */
        fun launchFragment(
            @StyleRes themeId: Int,
            args: Bundle? = null,
            initialState: Lifecycle.State = Lifecycle.State.INITIALIZED
        ): HiltFragmentScenario<F> =
            HiltFragmentScenario.
            launchFragmentInHiltContainer(
                fragmentClass = fragmentClass,
                fragmentArgs = args,
                themeResId = themeId,
                initialState = initialState
            ).apply { postLaunchActions() }

        /**
         * Launches the dialog fragment in [Lifecycle.State.INITIALIZED] state with the given arguments.
         * Ensure you set your application theme as [themeId] to prevent runtime resource issues.
         */
        fun launchDialogFragment(
            args: Bundle? = null,
            @StyleRes themeId: Int = R.style.Theme_Renewed,
            initialState: Lifecycle.State = Lifecycle.State.INITIALIZED
        ): HiltFragmentScenario<F> =
            HiltFragmentScenario.launchFragmentInHiltContainer(
                fragmentClass = fragmentClass,
                fragmentArgs = args,
                themeResId = themeId,
                initialState = initialState,
                containerViewId = 0
            ).apply { postLaunchActions() }

        private fun HiltFragmentScenario<F>.postLaunchActions() {
            fragmentScenario?.tearDown()
            fragmentScenario = this
            postLaunchCallbacks.forEach { callback -> callback(fragment ?: error("fragment not launched")) }
        }

        /**
         * Registers a callback that is executed right after the Fragment is initially launched.
         *
         * This is useful for stubbing that has to happen before the Fragment is actually started. Be sure to
         * launch the fragment in the proper initial state, such as [Lifecycle.State.INITIALIZED]
         */
        fun registerPostLaunchCallback(callback: (F) -> Unit) {
            require(fragmentScenario == null) { "Fragment was already launched" }
            postLaunchCallbacks.add(callback)
        }

        /**
         * Returns an Dagger Hilt entry point to retrieve a dependency that is available in Hilt's
         * FragmentComponent or above. Usage:
         *
         * ```
         * @EntryPoint
         * @InstallIn(FragmentComponent::class)
         * internal interface SomeDepEntryPoint {
         *     val dep: SomeDep
         * }
         * ...
         * scenarioRule.getFragmentEntryPoint<SomeDepEntryPoint>().dep
         * ```
         */
        inline fun <reified E : Any> getFragmentEntryPoint(): E =
            EntryPoints.get(
                fragmentScenario?.fragment ?: error("fragment not launched"),
                E::class.java
            )

        /**
         * Use this method to run this rule's after() code manually when JUnit rule execution is not possible
         */
        fun tearDown() {
            after()
        }

        override fun after() {
            fragmentScenario?.tearDown()
            fragmentScenario = null
            // inline mocking in Mockito is prone to leak memory, so we clean out big mocked objects in UI tests
            // see https://github.com/mockito/mockito/pull/1619 for a complete discussion

        }

        private fun HiltFragmentScenario<*>.tearDown() {
            moveToState(Lifecycle.State.DESTROYED)
            activityScenario.close()
        }
    }
