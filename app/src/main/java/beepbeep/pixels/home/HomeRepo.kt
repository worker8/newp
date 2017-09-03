package beepbeep.pixels.home

import beepbeep.pixels.auth.RedditClientRepo.Companion.createGuestRedditClient
import io.reactivex.Observable
import net.dean.jraw.models.Listing
import net.dean.jraw.models.Submission
import net.dean.jraw.paginators.SubredditPaginator

class HomeRepo : HomeRepoInterface {
    private val redditClient = createGuestRedditClient()
    private val subredditPaginator = redditClient.map { SubredditPaginator(it) }

    override fun getSubmissions(): Observable<Listing<Submission>> {
        return subredditPaginator.map { it.next() }
    }
}

interface HomeRepoInterface {
    fun getSubmissions(): Observable<Listing<Submission>>
}