package com.example.renewed.Screen2

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.renewed.PostsAdapter
import com.example.renewed.Screen1.Subscreen.BlankFragment
import com.example.renewed.Screen1.Subscreen.PostFragment
import com.example.renewed.models.PartialViewState
import com.example.renewed.models.ViewStateT3
import com.example.renewed.models.isVideoPost
import com.google.android.exoplayer2.Player
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

class FavoritesListAdapter(private val fragment: FavoritesListFragment): FragmentStateAdapter(fragment) {
    var postIds: MutableList<String> = mutableListOf<String>()
    var fragList: MutableList<PostFragment?> = arrayOfNulls<PostFragment>(10).toMutableList()
    var a = arrayOfNulls<PostFragment>(10).toMutableList()

    override fun getItemCount(): Int = postIds.size
    override fun getItemId(position: Int): Long = postIds[position].hashCode().toLong()
    override fun containsItem(itemId: Long): Boolean = postIds.any { it.hashCode().toLong() == itemId }

    fun replaceList(idList:List<String>){
        postIds.clear()
        postIds.addAll(idList)
        notifyDataSetChanged()
    }

    fun removeFirst(){
        Timber.d("WELLSBEFORE $postIds")
        Timber.d("WELLSBEFORE2 ${fragList.map{it?.state?.name}}")
        var copy = postIds.toMutableList()
        copy.removeAt(0)
        replaceList(copy)
        var copy2 = fragList.toMutableList()
        copy2.removeAt(0)
        fragList=copy2

        Timber.d("WELLSAFTER $postIds")
        Timber.d("WELLSAFTER2  ${fragList.map{it?.state?.name}}")
    }

    /**  used to always get this error for short lists before I watched only longer emissions.
     * this is less than ten so I'm still getting errors. If I filtered only 10 sized emissions
     * I might have trouble when I have less than 10 in db in swapping
     *  2022-12-13 13:28:20.646 26619-26619/com.example.offred E/AndroidRuntime: FATAL EXCEPTION: main
    Process: com.example.offred, PID: 26619
    java.lang.IndexOutOfBoundsException: Index: 8, Size: 7
    at java.util.ArrayList.add(ArrayList.java:483)
    at com.example.renewed.Screen2.FavoritesListAdapter.createFragment(FavoritesListAdapter.kt:49)
    at androidx.viewpager2.adapter.FragmentStateAdapter.ensureFragment(FragmentStateAdapter.java:268)
    at androidx.viewpager2.adapter.FragmentStateAdapter.onBindViewHolder(FragmentStateAdapter.java:175)
    at androidx.viewpager2.adapter.FragmentStateAdapter.onBindViewHolder(FragmentStateAdapter.java:67)
    at androidx.recyclerview.widget.RecyclerView$Adapter.onBindViewHolder(RecyclerView.java:7254)
    at androidx.recyclerview.widget.RecyclerView$Adapter.bindViewHolder(RecyclerView.java:7337)
    at androidx.recyclerview.widget.RecyclerView$Recycler.tryBindViewHolderByDeadline(RecyclerView.java:6194)
    at androidx.recyclerview.widget.RecyclerView$Recycler.tryGetViewHolderForPositionByDeadline(RecyclerView.java:6460)
    at androidx.recyclerview.widget.RecyclerView$Recycler.getViewForPosition(RecyclerView.java:6300)
    at androidx.recyclerview.widget.RecyclerView$Recycler.getViewForPosition(RecyclerView.java:6296)
    at androidx.recyclerview.widget.LinearLayoutManager$LayoutState.next(LinearLayoutManager.java:2330)
    at androidx.recyclerview.widget.LinearLayoutManager.layoutChunk(LinearLayoutManager.java:1631)
    at androidx.recyclerview.widget.LinearLayoutManager.fill(LinearLayoutManager.java:1591)
    at androidx.recyclerview.widget.LinearLayoutManager.onLayoutChildren(LinearLayoutManager.java:668)
    at androidx.recyclerview.widget.RecyclerView.dispatchLayoutStep2(RecyclerView.java:4309)
    at androidx.recyclerview.widget.RecyclerView.dispatchLayout(RecyclerView.java:4012)
    at androidx.recyclerview.widget.RecyclerView.onLayout(RecyclerView.java:4578)
    at android.view.View.layout(View.java:23694)
    at android.view.ViewGroup.layout(ViewGroup.java:6413)
    at androidx.viewpager2.widget.ViewPager2.onLayout(ViewPager2.java:527)
    at android.view.View.layout(View.java:23694)
    at android.view.ViewGroup.layout(ViewGroup.java:6413)
    at androidx.constraintlayout.widget.ConstraintLayout.onLayout(ConstraintLayout.java:1873)
    at android.view.View.layout(View.java:23694)
    at android.view.ViewGroup.layout(ViewGroup.java:6413)
    at android.widget.FrameLayout.layoutChildren(FrameLayout.java:332)
    at android.widget.FrameLayout.onLayout(FrameLayout.java:270)
    at android.view.View.layout(View.java:23694)
    at android.view.ViewGroup.layout(ViewGroup.java:6413)
    at android.widget.FrameLayout.layoutChildren(FrameLayout.java:332)
    at android.widget.FrameLayout.onLayout(FrameLayout.java:270)
    at android.view.View.layout(View.java:23694)
    at android.view.ViewGroup.layout(ViewGroup.java:6413)
    at android.widget.LinearLayout.setChildFrame(LinearLayout.java:1829)
    at android.widget.LinearLayout.layoutVertical(LinearLayout.java:1673)
    at android.widget.LinearLayout.onLayout(LinearLayout.java:1582)
    at android.view.View.layout(View.java:23694)
    at android.view.ViewGroup.layout(ViewGroup.java:6413)
    at android.widget.FrameLayout.layoutChildren(FrameLayout.java:332)
    at android.widget.FrameLayout.onLayout(FrameLayout.java:270)
    at android.view.View.layout(View.java:23694)
    at android.view.ViewGroup.layout(ViewGroup.java:6413)
    at android.widget.LinearLayout.setChildFrame(LinearLayout.java:1829)
    at android.widget.LinearLayout.layoutVertical(LinearLayout.java:1673)
    at android.widget.LinearLayout.onLayout(LinearLayout.java:1582)
    at android.view.View.layout(View.java:23694)
    at android.view.ViewGroup.layout(ViewGroup.java:6413)
    at android.widget.FrameLayout.layoutChildren(FrameLayout.java:332)
    at android.widget.FrameLayout.onLayout(FrameLayout.java:270)
    at android.view.View.layout(View.java:23694)
    at android.view.ViewGroup.layout(ViewGroup.java:6413)
    at android.widget.LinearLayout.setChildFrame(LinearLayout.java:1829)
    at android.widget.LinearLayout.layoutVertical(LinearLayout.java:1673)
    at android.widget.LinearLayout.onLayout(LinearLayout.java:1582)
    2022-12-13 13:28:20.646 26619-26619/com.example.offred E/AndroidRuntime:     at android.view.View.layout(View.java:23694)
    at android.view.ViewGroup.layout(ViewGroup.java:6413)
    at android.widget.FrameLayout.layoutChildren(FrameLayout.java:332)
    at android.widget.FrameLayout.onLayout(FrameLayout.java:270)
    at com.android.internal.policy.DecorView.onLayout(DecorView.java:797)
    at android.view.View.layout(View.java:23694)
    at android.view.ViewGroup.layout(ViewGroup.java:6413)
    at android.view.ViewRootImpl.performLayout(ViewRootImpl.java:3911)
    at android.view.ViewRootImpl.performTraversals(ViewRootImpl.java:3298)
    at android.view.ViewRootImpl.doTraversal(ViewRootImpl.java:2286)
    at android.view.ViewRootImpl$TraversalRunnable.run(ViewRootImpl.java:8948)
    at android.view.Choreographer$CallbackRecord.run(Choreographer.java:1231)
    at android.view.Choreographer$CallbackRecord.run(Choreographer.java:1239)
    at android.view.Choreographer.doCallbacks(Choreographer.java:899)
    at android.view.Choreographer.doFrame(Choreographer.java:832)
    at android.view.Choreographer$FrameDisplayEventReceiver.run(Choreographer.java:1214)
    at android.os.Handler.handleCallback(Handler.java:942)
    at android.os.Handler.dispatchMessage(Handler.java:99)
    at android.os.Looper.loopOnce(Looper.java:201)
    at android.os.Looper.loop(Looper.java:288)
    at android.app.ActivityThread.main(ActivityThread.java:7898)
    at java.lang.reflect.Method.invoke(Native Method)
    at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:548)
    at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:936)**/
    override fun createFragment(position: Int): Fragment {
        val name = postIds[position]
        val fragment = PostFragment()
        fragment.arguments = Bundle().apply {
            putString("key", name)
            putBoolean("isSubscreen",false)
        }
     //   fragList.add(position,fragment)
        fragList[position] = fragment
        return fragment

    }
    fun addFragment(t3: ViewStateT3){
        var copy = postIds.toMutableList()
        t3.name?.let { copy.add(it) }

        replaceList(copy)
  notifyDataSetChanged()

    }
    fun startVideoAtPosition(position: Int) {
        if (position <0|| position >=  fragList.size) return
        fragList[position]?.loadVideo()
    }
}