package beepbeep.pixels.home

import RedditClientRepo
import android.util.Log
import beepbeep.pixels.cache.submission.SubmissionCache
import beepbeep.pixels.shared.PixelsApplication
import io.reactivex.Flowable
import io.reactivex.Observable
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
    lateinit private var subredditPaginator: BehaviorSubject<SubredditPaginator>
    lateinit private var loadMoreSubject: BehaviorSubject<Unit>
    lateinit private var redditClientSubject: BehaviorSubject<RedditClient>
    lateinit private var dataSubject: PublishSubject<Pair<SubredditPaginator, Listing<Submission>>>
    lateinit private var disposables: CompositeDisposable

    private var redditClient: RedditClient? = null

    init {
        reset()
    }

    override fun reset() {
        loadMoreSubject = BehaviorSubject.create<Unit>()
        redditClientSubject = BehaviorSubject.create<RedditClient>()
        dataSubject = PublishSubject.create<Pair<SubredditPaginator, Listing<Submission>>>()
        disposables = CompositeDisposable()
        subredditPaginator = BehaviorSubject.create<SubredditPaginator>()
    }

    override fun isRedditClientAuthed(): Boolean {
        redditClient?.let {
            if (isNotExpire(it.oAuthData.expirationDate)) {
                return it.isAuthenticated
            }
        }
        return false
    }

    private fun isNotExpire(expireDate: Date): Boolean {
        val nowDate = Date()
        return nowDate.before(expireDate)
    }

    override fun initGuestRedditClient(): Observable<RedditClient> {
        return RedditClientRepo
                .createGuestRedditClient()
                .subscribeOn(Schedulers.io())
                .doOnNext {
                    redditClient = it
                }
                .doOnNext { subredditPaginator.onNext(SubredditPaginator(it)) }
    }

    override fun data(): Observable<Pair<Boolean, Listing<Submission>>> {
        return subredditPaginator.doOnNext { Log.d("ddw", "sub address: ${it}") }
                .map { paginator -> paginator.run { hasStarted() to next() } }
                .subscribeOn(Schedulers.io())
//        val paginatorObs = redditClientSubject
//                .map { SubredditPaginator(it) }
//                .subscribeOn(Schedulers.io())
//        return Observable.combineLatest(
//                paginatorObs,
//                loadMoreSubject,
//                BiFunction<SubredditPaginator, Unit, Pair<Boolean, Listing<Submission>>> { paginator, Unit ->
//                    paginator.run { hasStarted() to next() }
//                })
//                .subscribeOn(Schedulers.io())
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
}

interface HomeRepoInterface {
    fun initGuestRedditClient(): Observable<RedditClient>
    fun isRedditClientAuthed(): Boolean
    fun reset()
    fun loadMore()
    fun data(): Observable<Pair<Boolean, Listing<Submission>>>
    fun destroy()
    fun bindToDb(): Flowable<List<SubmissionCache>>?
    fun deleteAllFromSub()
}