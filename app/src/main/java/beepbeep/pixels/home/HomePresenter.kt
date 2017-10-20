package beepbeep.pixels.home

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import beepbeep.pixels.cache.submission.SubmissionCache
import beepbeep.pixels.shared.extension.addTo
import beepbeep.pixels.shared.extension.downScheduler
import beepbeep.pixels.shared.extension.upScheduler
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import net.dean.jraw.models.Listing
import net.dean.jraw.models.Submission
import net.dean.jraw.paginators.SubredditPaginator

class HomePresenter(val input: HomeContract.Input, val repo: HomeRepoInterface = HomeRepo()) : LifecycleObserver {
    val disposables = CompositeDisposable()
    var refreshDisposable: Disposable? = null
    val onDataLoaded = PublishSubject.create<Unit>()
    val showNoInternetSnackbarSubject = PublishSubject.create<Unit>()
    val onCacheDataLoadedSubject = PublishSubject.create<List<SubmissionCache>>()

    val paginatorSubject = BehaviorSubject.create<SubredditPaginator>()

    val output = object : HomeContract.Output() {
        override val onCacheDataLoaded: Observable<List<SubmissionCache>>
            get() = onCacheDataLoadedSubject.hide()
        override val showNoInternetSnackbar: Observable<Unit>
            get() = showNoInternetSnackbarSubject.hide()
        override val onDataLoaded: Observable<Unit>
            get() = onDataLoaded.hide()
    }

    private fun deleteOldSubmission(pair: Pair<Boolean, Listing<Submission>>) {
        val (started, submissionListing) = pair
        if (!started) {
            repo.deleteAllFromSub()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        val initialTrigger = PublishSubject.create<Unit>()
        // initial flow OR refresh
        Observable.merge(input.refresh, initialTrigger)
                .publish()
                .apply {
                    upScheduler(repo.getMainUiThread())
                            .map { input.isConnectedToInternet() }
                            .filter { it }
                            .doOnNext { System.out.println("[${this}] before initAndGetData()") }
                            .flatMap { repo.initAndGetData() }
                            .doOnNext { System.out.println("[${this}] after initAndGetData()") }
                            //.doOnNext { refreshDisposable?.dispose() }
                            .upScheduler(repo.getBackgroundThread())
                            .downScheduler(repo.getBackgroundThread())
                            .subscribe(
                                    {
                                        System.out.println("[${this}] onNext1: [${it}]")
                                        paginatorSubject.onNext(it)

                                    }, {}, {
                                System.out.println("[${this}] onComplete")
                            })
                            .addTo(disposables)
//
//                    // when offline
//                    upScheduler(repo.getMainUiThread())
//                            .map { !input.isConnectedToInternet() }
//                            .filter { it }
//                            .upScheduler(repo.getBackgroundThread())
//                            .downScheduler(repo.getMainUiThread())
//                            .subscribe { showNoInternetSnackbarSubject.onNext(Unit) }
                }
                .connect()
                .addTo(disposables)

        paginatorSubject
                .map { paginator -> repo.nextPair(paginator) }
                .doOnNext { deleteOldSubmission(it) }
                .subscribe { (started, submissionListing) -> repo.insertDb(submissionListing) }
                .addTo(disposables)

        Observable
                .merge(input.retry, input.loadMore)
                .publish()
                .apply {
                    // Initial flow
                    map { input.isConnectedToInternet() }
                            .filter { it }
                            .filter { !repo.isRedditClientAuthed() }
                            .subscribe { initialTrigger.onNext(Unit) }

                    // load more flow
                    map { input.isConnectedToInternet() }
                            .filter { it }
                            .filter { repo.isRedditClientAuthed() }
                            .subscribe { paginatorSubject.onNext(paginatorSubject.value) }
//                            .map { repo.getPaginator() }
//                            .flatMap { repo.data(it) }
//                            .doOnNext { (started, submissionListing) ->
//                                if (!started) {
//                                    repo.deleteAllFromSub()
//                                }
//                            }
//                            .subscribe {
//                                (started, submissionListing) ->
//                                repo.insertDb(submissionListing)
//                            }

                    // when offline
                    map { !input.isConnectedToInternet() }
                            .filter { it }
                            .upScheduler(repo.getBackgroundThread())
                            .downScheduler(repo.getMainUiThread())
                            .subscribe { showNoInternetSnackbarSubject.onNext(Unit) }
                }
                .connect()
                .addTo(disposables)

        repo.bindToDb()?.apply {
            upScheduler(repo.getBackgroundThread())
                    .downScheduler(repo.getMainUiThread())
                    .subscribe { listing -> onCacheDataLoadedSubject.onNext(listing) }
                    .addTo(disposables)
        }

        initialTrigger.onNext(Unit)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        disposables.dispose()
        repo.destroy()
    }
}