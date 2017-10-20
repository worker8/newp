package beepbeep.pixels.home

import beepbeep.pixels.BuildConfig
import beepbeep.pixels.cache.submission.SubmissionCache
import com.nhaarman.mockito_kotlin.mock
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import net.dean.jraw.RedditClient
import net.dean.jraw.http.UserAgent
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
    var _insertDbTestOutput = PublishSubject.create<Listing<Submission>>()

    var _initGuestRedditClient = PublishSubject.create<SubredditPaginator>()
    var _getPaginator = PublishSubject.create<SubredditPaginator>()
    var _isRedditClientAuthed = false
    var _data = PublishSubject.create<Pair<Boolean, Listing<Submission>>>()
    var _bindToDb = PublishSubject.create<List<SubmissionCache>>()
    var _deleteAllFromSub = PublishSubject.create<Unit>()

    var _initAndGetData = PublishSubject.create<SubredditPaginator>()
    var _nextPair = Pair<Boolean, Listing<Submission>>(false, mock())

    val input = object : HomeContract.Input {
        override fun isConnectedToInternet() = _isConnectedToInternet
        override val loadMore = _loadMore.hide()
        override val refresh = _refresh.hide()
        override val retry = _retry.hide()
    }

    val repo = object : HomeRepoInterface {
        override fun nextPair(subredditPaginator: SubredditPaginator): Pair<Boolean, Listing<Submission>> = _nextPair

        override fun initAndGetData(): Observable<SubredditPaginator> = _initAndGetData.hide()

        override fun insertDb(listing: Listing<Submission>) = _insertDbTestOutput.onNext(listing)
        override fun initGuestRedditClient() = _initGuestRedditClient.hide()
        override fun getPaginator() = _getPaginator.hide()

        override fun isRedditClientAuthed() = _isRedditClientAuthed
        override fun reset() {}
        override fun loadMore() {}
        override fun data(paginatorObs: Observable<SubredditPaginator>) = _data.hide()
        override fun destroy() {}
        override fun bindToDb() = _bindToDb.toFlowable(BackpressureStrategy.BUFFER)
        override fun deleteAllFromSub() = _deleteAllFromSub.onNext(Unit)
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

    @Test
    fun testDeleteDbForFirstLoad_fromRefresh() {
        // arrange
        _isConnectedToInternet = true
        val insertDbTestObs = _insertDbTestOutput.test()
        val deleteDbTestObs = _deleteAllFromSub.test()
        val fakeSubmissions = mock<Listing<Submission>>()

        // action
        presenterUT.onCreate()
        _initAndGetData.onNext(createSubredditPaginator())
        _initAndGetData.onComplete()

        // assert
        insertDbTestObs.assertNoErrors().assertValueCount(1)
        deleteDbTestObs.assertNoErrors().assertValueCount(1)

        // action
        _initAndGetData = PublishSubject.create<SubredditPaginator>()

        _refresh.onNext(Unit)
        _initAndGetData.onNext(createSubredditPaginator())

        // assert
        insertDbTestObs.assertNoErrors().assertValueCount(2)
        deleteDbTestObs.assertNoErrors().assertValueCount(2)

    }

    @Test
    fun testDeleteDbForFirstLoad_thenLoadMore() {
        // arrange
        _isConnectedToInternet = true
        val insertDbTestObs = _insertDbTestOutput.test()
        val deleteDbTestObs = _deleteAllFromSub.test()

        // action
        presenterUT.onCreate()
        _initAndGetData.onNext(createSubredditPaginator())
        _initAndGetData.onComplete()

        // assert
        insertDbTestObs.assertNoErrors().assertValueCount(1)
        deleteDbTestObs.assertNoErrors().assertValueCount(1)

        // action
        _isRedditClientAuthed = true
        _nextPair = true to mock()
        _loadMore.onNext(Unit)

        // assert
        insertDbTestObs.assertNoErrors().assertValueCount(2)
        deleteDbTestObs.assertNoErrors().assertValueCount(1)

        // action
        _loadMore.onNext(Unit)
        // assert
        insertDbTestObs.assertNoErrors().assertValueCount(3)
        deleteDbTestObs.assertNoErrors().assertValueCount(1)

        // action
        _initAndGetData = PublishSubject.create<SubredditPaginator>() // because onCompleted above, this must be before _refresh.onNext()
        _nextPair = false to mock()

        _refresh.onNext(Unit)
        _initAndGetData.onNext(createSubredditPaginator())
        _initAndGetData.onComplete()

        // assert
        insertDbTestObs.assertNoErrors().assertValueCount(4)
        deleteDbTestObs.assertNoErrors().assertValueCount(2)
    }

    private fun createSubredditPaginator() =
            SubredditPaginator(
                    RedditClient(
                            UserAgent.of(
                                    "android",
                                    BuildConfig.APPLICATION_ID,
                                    BuildConfig.VERSION_NAME,
                                    BuildConfig.REDDIT_DEVELOPER_NAME)))
}