package beepbeep.pixels.home

import beepbeep.pixels.cache.submission.SubmissionCache
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import net.dean.jraw.models.Listing
import net.dean.jraw.models.Submission
import net.dean.jraw.paginators.SubredditPaginator
import org.junit.Before
import org.junit.Test

class HomePresenterTest {
    var _isConnectedToInternet = false
    var _loadMore = PublishSubject.create<Any>()
    var _refresh = PublishSubject.create<Any>()
    var _retry = PublishSubject.create<Unit>()

    var _initGuestRedditClient = PublishSubject.create<SubredditPaginator>()
    var _getPaginator = PublishSubject.create<SubredditPaginator>()
    var _isRedditClientAuthed = false
    var _data = PublishSubject.create<Pair<Boolean, Listing<Submission>>>()
    var _bindToDb = PublishSubject.create<List<SubmissionCache>>()

    val input = object : HomeContract.Input {
        override fun isConnectedToInternet() = _isConnectedToInternet
        override val loadMore = _loadMore.hide()
        override val refresh = _refresh.hide()
        override val retry = _retry.hide()
    }

    val repo = object : HomeRepoInterface {
        override fun initGuestRedditClient() = _initGuestRedditClient.hide()
        override fun getPaginator() = _getPaginator.hide()
        override fun isRedditClientAuthed() = _isRedditClientAuthed
        override fun reset() {}
        override fun loadMore() {}
        override fun data(paginatorObs: Observable<SubredditPaginator>) = _data.hide()
        override fun destroy() {}
        override fun bindToDb() = _bindToDb.toFlowable(BackpressureStrategy.BUFFER)
        override fun deleteAllFromSub() {}
        override fun getBackgroundThread() = Schedulers.trampoline()
        override fun getMainUiThread() = Schedulers.trampoline()
    }

    val presenterUT = HomePresenter(input, repo)

    @Before
    fun setup() {
        //presenterUT.onCreate()
    }

    @Test
    fun noInternetTest() {
        // arrange
        _isConnectedToInternet = false
        val testObs = presenterUT.output.showNoInternetSnackbar.test()

        // action
        presenterUT.onCreate()

        // assert
        testObs.assertNoErrors().assertValueCount(1)
    }
}