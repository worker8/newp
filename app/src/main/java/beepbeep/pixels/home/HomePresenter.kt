package beepbeep.pixels.home

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.util.Log
import beepbeep.pixels.cache.submission.SubmissionCache
import beepbeep.pixels.shared.PixelsApplication
import beepbeep.pixels.shared.extension.addTo
import beepbeep.pixels.shared.extension.downScheduler
import beepbeep.pixels.shared.extension.upMainUiThread
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

    val output = object : HomeContract.Output() {
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
                .upMainUiThread()
                .map { input.isConnectedToInternet() }
                .filter { it }
                .map { repo.initGuestRedditClient() }
                .flatMap { repo.data(it) }
                .doOnNext { deleteOldSubmission(it) }
                .upScheduler(repo.getBackgroundThread())
                .downScheduler(repo.getBackgroundThread())
                .subscribe(onDataLoaded())
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
                    .subscribe { listing ->
                        listing.forEachIndexed { index, submissionCache ->
                            Log.d("ddw", "[${index}] ${submissionCache.author}: ${submissionCache.title}")
                        }
                    }
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