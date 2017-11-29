package beepbeep.pixels.home

import RedditClientRepo
import beepbeep.pixels.cache.submission.SubmissionCache
import beepbeep.pixels.shared.PixelsApplication
import beepbeep.pixels.shared.SubredditList
import beepbeep.pixels.shared.extension.upScheduler
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import net.dean.jraw.RedditClient
import net.dean.jraw.models.Listing
import net.dean.jraw.models.Submission
import net.dean.jraw.paginators.SubredditPaginator
import java.util.*

class HomeRepo : HomeRepoInterface {
    lateinit private var subredditPaginator: PublishSubject<SubredditPaginator>
    lateinit private var loadMoreSubject: BehaviorSubject<Unit>
    lateinit private var redditClientSubject: BehaviorSubject<RedditClient>
    lateinit private var dataSubject: PublishSubject<Pair<SubredditPaginator, Listing<Submission>>>
    lateinit private var disposables: CompositeDisposable

    private var redditClient: RedditClient? = null
    private var paginator: SubredditPaginator? = null

    init {
        reset()
    }

    override fun reset() {
        loadMoreSubject = BehaviorSubject.create<Unit>()
        redditClientSubject = BehaviorSubject.create<RedditClient>()
        dataSubject = PublishSubject.create<Pair<SubredditPaginator, Listing<Submission>>>()
        disposables = CompositeDisposable()
        subredditPaginator = PublishSubject.create<SubredditPaginator>()
    }

    override fun isRedditClientAuthed(): Boolean {
        redditClient?.let {
            return it.isAuthenticated
        }
        return false
    }

    private fun isNotExpire(expireDate: Date): Boolean {
        val nowDate = Date()
        return nowDate.before(expireDate)
    }

    override fun initGuestRedditClient(): Observable<SubredditPaginator> {
        return RedditClientRepo
                .createGuestRedditClient()
                .doOnNext { redditClient = it }
                .map {
                    SubredditPaginator(it, SubredditList.art.name)
                            .also { paginator = it }
                }
                .upScheduler(getBackgroundThread())
    }

    override fun getPaginator(): Observable<SubredditPaginator> {
        return Observable.just(paginator)
    }

    override fun data(paginatorObs: Observable<SubredditPaginator>): Observable<Pair<Boolean, Listing<Submission>>> {
        return paginatorObs
                .map { it.hasStarted() to it.next() }
                .upScheduler(getBackgroundThread())
    }

    override fun loadMore() {
        loadMoreSubject.onNext(Unit)
    }

    override fun bindToDb(): Flowable<List<SubmissionCache>>? {
        return PixelsApplication.pixelsCache?.submissionDao()?.getSubmissionCacheFlowable()
    }

    override fun destroy() {
        disposables?.dispose()
    }

    override fun deleteAllFromSub() {
        PixelsApplication.pixelsCache?.submissionDao()?.apply {
            getSubmissionCacheMaybe().subscribe { list ->
                delete(list)
            }
        }
    }

    override fun getBackgroundThread() = Schedulers.io()
    override fun getMainUiThread() = AndroidSchedulers.mainThread()
}

interface HomeRepoInterface {
    fun initGuestRedditClient(): Observable<SubredditPaginator>
    fun getPaginator(): Observable<SubredditPaginator>
    fun isRedditClientAuthed(): Boolean
    fun reset()
    fun loadMore()
    fun data(paginatorObs: Observable<SubredditPaginator>): Observable<Pair<Boolean, Listing<Submission>>>
    fun destroy()
    fun bindToDb(): Flowable<List<SubmissionCache>>?
    fun deleteAllFromSub()

    fun getBackgroundThread(): Scheduler
    fun getMainUiThread(): Scheduler
}