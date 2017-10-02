package beepbeep.pixels.home

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import beepbeep.pixels.cache.submission.SubmissionCache
import beepbeep.pixels.shared.PixelsApplication
import beepbeep.pixels.shared.extension.addTo
import beepbeep.pixels.shared.extension.downScheduler
import beepbeep.pixels.shared.extension.upScheduler
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import net.dean.jraw.models.Listing
import net.dean.jraw.models.Submission

class HomePresenter(val input: HomeContract.Input, val repo: HomeRepoInterface = HomeRepo()) : LifecycleObserver {
    val disposables = CompositeDisposable()
    val onDataLoaded = PublishSubject.create<Unit>()
    val showNoInternetSnackbarSubject = PublishSubject.create<Unit>()
    val onCacheDataLoadedSubject = PublishSubject.create<List<SubmissionCache>>()
    val output = object : HomeContract.Output() {
        override val onCacheDataLoaded: Observable<List<SubmissionCache>>
            get() = onCacheDataLoadedSubject.hide()
        override val showNoInternetSnackbar: Observable<Unit>
            get() = showNoInternetSnackbarSubject.hide()
        override val onDataLoaded: Observable<Unit>
            get() = onDataLoaded.hide()
    }

    private fun onDataLoaded(): (Pair<Boolean, Listing<Submission>>) -> Unit {
        return { (started, submissionListing) ->
            submissionListing.forEachIndexed { index, submission ->
                PixelsApplication.pixelsCache?.submissionDao()?.insert(SubmissionCache(submission))
            }
        }
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

        // refresh flow
        input.refresh
                .publish()
                .apply {
                    upScheduler(repo.getMainUiThread())
                            .map { input.isConnectedToInternet() }
                            .filter { it }
                            .map { repo.initGuestRedditClient() }
                            .flatMap { repo.data(it) }
                            .doOnNext { deleteOldSubmission(it) }
                            .upScheduler(repo.getBackgroundThread())
                            .downScheduler(repo.getBackgroundThread())
                            .subscribe(onDataLoaded())
                            .addTo(disposables)

                    // when offline
                    upScheduler(repo.getMainUiThread())
                            .map { !input.isConnectedToInternet() }
                            .filter { it }
                            .upScheduler(repo.getBackgroundThread())
                            .downScheduler(repo.getMainUiThread())
                            .subscribe { showNoInternetSnackbarSubject.onNext(Unit) }
                }
                .connect()
                .addTo(disposables)


        Observable
                .merge(initialTrigger, input.retry, input.loadMore)
                .publish()
                .apply {
                    // Initial flow
                    map { input.isConnectedToInternet() }
                            .filter { it }
                            .filter { !repo.isRedditClientAuthed() }
                            .map { repo.initGuestRedditClient() }
                            .flatMap { repo.data(it) }
                            .doOnNext { deleteOldSubmission(it) }
                            .upScheduler(repo.getBackgroundThread())
                            .downScheduler(repo.getBackgroundThread())
                            .subscribe(onDataLoaded())

                    // load more flow
                    map { input.isConnectedToInternet() }
                            .filter { it }
                            .filter { repo.isRedditClientAuthed() }
                            .map { repo.getPaginator() }
                            .flatMap { repo.data(it) }
                            .doOnNext { (started, submissionListing) ->
                                if (!started) {
                                    repo.deleteAllFromSub()
                                }
                            }
                            .subscribe(onDataLoaded())

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