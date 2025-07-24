package com.example.offred.repos

import com.example.offred.API
import com.example.offred.Room.FavoritesDAO
import com.example.offred.Room.T3DAO
import com.example.offred.Room.T5DAO
import com.example.offred.models.*
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

class FavoritesRepo(private val t5: T5DAO,
    private val t3: T3DAO,
    private val favs:FavoritesDAO,
    private val api: API
    ): BaseFavoritesRepo {

        override fun insertAll(posts: List<String>): Completable {
            // We need to map the list of strings to a list of your entity class.
            val favoriteListEntities = posts.map { CurrentFavoritesList(
                postId = it,
                displayOrder = System.currentTimeMillis()
            ) }
            return favs.insertAll(favoriteListEntities)
        }
    override fun insert(s: String): Completable {
        return favs.insert(CurrentFavoritesList(s,displayOrder=System.currentTimeMillis()))

    }
    override fun observeSavedSubreddits(): Observable<List<RoomT5>>{
        return t5.observeSavedSubreddits()
    }

    override fun getRandomPosts(name: String, number: Int): Observable<RoomT3> {
        TODO("Not yet implemented")
    }

    override fun getPostsFromSavedSubreddit(subreddit: RoomT5): Observable<RoomT3>{
        return Observable.fromIterable(t3.getPosts(subreddit.name).blockingGet())
    }

    override fun currentLength(): Observable<Int>{
        return favs.currentSize()
    }

    override fun observeCurrentPostList(): Observable<List<String>>{
        return favs.getPosts()
    }

    override fun deletePages(s:List<String>): Completable =
        t3.markAsViewed(s).andThen(favs.deleteList(s))


    override fun clearPages(): Completable {
        return favs.clearDb().startWith(t5.deleteUnsavedPosts())
    }



    private fun extractT3Field(it: Listing): T3 = it.data.children[0].data as T3

}


