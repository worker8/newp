package beepbeep.pixels.feed

import beepbeep.pixels.feed.FeedViewModel.Companion.reduce
import beepbeep.pixels.feed.list.FeedListViewModel
import io.reactivex.Observable

class FeedInteractor(input: FeedContract.Input, source: FeedDataSource) : FeedContract.Output {

    private val viewModels: Observable<FeedViewModel>

    override val loadings: Observable<Boolean>
        get() = viewModels.map(FeedViewModel::isLoading)

    override val items: Observable<List<FeedListViewModel>>
        get() = viewModels.map(FeedViewModel::items)

    init {
        val loadAction = source.loadData()
                .map { SetAction(it) as FeedAction }
                .startWith(LoadAction)

        val refreshAction = input.onRefresh
                .flatMap {
                    Observable.just(LoadAction as FeedAction)
                            .concatWith(source.loadData().map { SetAction(it) })
                }

        viewModels = Observable.mergeArray(loadAction, refreshAction)
                .scan(FeedViewModel(), ::reduce)
                .skip(1)
                .replay(1)
                .autoConnect()
    }
}