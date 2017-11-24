package beepbeep.pixels.feed

import beepbeep.pixels.feed.list.FeedListViewModel
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

interface FeedDataSource {

    fun loadData(): Observable<List<FeedListViewModel>>
}

class FakeFeedRepository : FeedDataSource {

    override fun loadData(): Observable<List<FeedListViewModel>> {
        return Observable.just(listOf(
                FeedListViewModel("https://dg19s6hp6ufoh.cloudfront.net/pictures/630/medium/Picasso - Self-Portrait.jpeg"),
                FeedListViewModel("https://dg19s6hp6ufoh.cloudfront.net/pictures/1314/medium/deux bathers zervos.jpeg"),
                FeedListViewModel("https://dg19s6hp6ufoh.cloudfront.net/pictures/17640/medium/picasso_boy_with_a_pipe_126.jpeg"),
                FeedListViewModel("https://dg19s6hp6ufoh.cloudfront.net/pictures/1896/medium/Le Moulin De La Galette.jpeg"),
                FeedListViewModel("https://dg19s6hp6ufoh.cloudfront.net/pictures/611826795/medium/avignon.jpeg")
        )).delay(2000, TimeUnit.MILLISECONDS)
    }
}