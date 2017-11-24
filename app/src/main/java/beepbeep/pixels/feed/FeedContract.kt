package beepbeep.pixels.feed

import beepbeep.pixels.feed.list.FeedListViewModel
import io.reactivex.Observable

interface FeedContract {

    interface Input {
        val onRefresh: Observable<Unit>
        val onLoadMore: Observable<Unit>
    }

    interface Output {
        val loadings: Observable<Boolean>
        val items: Observable<List<FeedListViewModel>>
    }
}