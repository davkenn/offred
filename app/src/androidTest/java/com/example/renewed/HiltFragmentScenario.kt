package com.example.renewed

import android.app.Activity
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commitNow
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import okio.use
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KClass

class HiltFragmentScenario<F : Fragment> private constructor(
    private val fragmentClass: Class<F>,
    val activityScenario: ActivityScenario<HiltTestActivity>
) {
    @Suppress("UNCHECKED_CAST")
    var fragment: F?=null

        get() =
            getTheActivity<HiltTestActivity>()
            ?.supportFragmentManager
            ?.findFragmentByTag(FRAGMENT_TAG)as? F?


private fun <T:Activity> getTheActivity(): T    {
        var atomicRef :AtomicReference<T> = AtomicReference()
        activityScenario.onActivity { atomicRef::set}
        return atomicRef.get()
}


    /**
     * Moves Fragment state to a new state.
     *
     *  If a new state and current state are the same, this method does nothing. It accepts
     * [CREATED][Lifecycle.State.CREATED], [STARTED][Lifecycle.State.STARTED],
     * [RESUMED][Lifecycle.State.RESUMED], and [DESTROYED][Lifecycle.State.DESTROYED].
     * [DESTROYED][Lifecycle.State.DESTROYED] is a terminal state.
     * You cannot move to any other state after the Fragment reaches that state.
     *
     * This method cannot be called from the main thread.
     */
    fun moveToState(newState: Lifecycle.State): HiltFragmentScenario<F> {
        if (newState == Lifecycle.State.DESTROYED) {
            activityScenario.onActivity { activity ->
                val fragment = activity.supportFragmentManager
                    .findFragmentByTag("subscreen_nav_container")
                // Null means the fragment has been destroyed already.
                if (fragment != null) {
                    activity.supportFragmentManager.commitNow {
                        this as FragmentTransaction
                        remove(fragment)
                    }
                }
            }
        } else {
            activityScenario.onActivity { activity ->
                val fragment = requireNotNull(
                    activity.supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)
                ) {
                    "The fragment has been removed from the FragmentManager already."
                }
                activity.supportFragmentManager.commitNow {
                    this as FragmentTransaction
                    setMaxLifecycle(fragment, newState)
                }
            }
        }
        return this
    }

    /**
     * FragmentAction interface should be implemented by any class whose instances are intended to
     * be executed by the main thread. A Fragment that is instrumented by the FragmentScenario is
     * passed to [FragmentAction.perform] method.
     *
     * You should never keep the Fragment reference as it will lead to unpredictable behaviour.
     * It should only be accessed in [FragmentAction.perform] scope.
     */
    fun interface FragmentAction<F : Fragment> {
        /**
         * This method is invoked on the main thread with the reference to the Fragment.
         *
         * @param fragment a Fragment instrumented by the FragmentScenario.
         */
        fun perform(fragment: F)
    }

    /**
     * Runs a given [action] on the current Activity's main thread.
     *
     * Note that you should never keep Fragment reference passed into your [action]
     * because it can be recreated at anytime during state transitions.
     *
     * Throwing an exception from [action] makes the host Activity crash. You can
     * inspect the exception in logcat outputs.
     *
     * This method cannot be called from the main thread.
     */
    fun onFragment(action: FragmentAction<F>): HiltFragmentScenario<F> {
        activityScenario.onActivity { activity ->
            val fragment = requireNotNull(
                activity.supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)
            ) {
                "The fragment has been removed from the FragmentManager already."
            }
            check(fragmentClass.isInstance(fragment))
            action.perform(requireNotNull(fragmentClass.cast(fragment)))
        }
        return this
    }

    /**
     * Recreates the host Activity.
     *
     * After this method call, it is ensured that the Fragment state goes back to the same state
     * as its previous state.
     *
     * This method cannot be called from the main thread.
     */
    fun recreate(): HiltFragmentScenario<F> {
        activityScenario.recreate()
        return this
    }

    companion object {
        private const val FRAGMENT_TAG = "FragmentScenario_Fragment_Tag"

        fun <F : Fragment> launchFragmentInHiltContainer(
            fragmentClass: KClass<F>,
            fragmentArgs: Bundle? = null,
            @StyleRes themeResId: Int = R.style.Theme_Renewed,
            initialState: Lifecycle.State = Lifecycle.State.RESUMED,
            @IdRes containerViewId: Int = android.R.id.content
        ): HiltFragmentScenario<F> {
            require(initialState != Lifecycle.State.DESTROYED) {
                "Cannot set initial Lifecycle state to $initialState for FragmentScenario"
            }
            val startActivityIntent = HiltTestActivity.createIntent(
                ApplicationProvider.getApplicationContext(),
                themeResId
            )
            val scenario = HiltFragmentScenario(
                fragmentClass.java,
                ActivityScenario.launch(
                    startActivityIntent
                )
            )
            scenario.activityScenario.onActivity {activity ->
                val fragment = activity.supportFragmentManager.fragmentFactory
                    .instantiate(
                        requireNotNull(fragmentClass.java.classLoader),
                        fragmentClass.java.name

                    )

                fragment.arguments = fragmentArgs
                activity.supportFragmentManager.commitNow {
                    this as FragmentTransaction

                    add(containerViewId, fragment, FRAGMENT_TAG)
                    setMaxLifecycle(fragment, initialState)
                }
            }
            return scenario
        }
    }
}
