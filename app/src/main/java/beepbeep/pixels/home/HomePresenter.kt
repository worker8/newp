package beepbeep.pixels.home

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.util.Log
import beepbeep.pixels.cache.submission.SubmissionCache
import beepbeep.pixels.shared.PixelsApplication
import beepbeep.pixels.shared.extension.addTo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject


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

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onCreate() {
        repo.data()
                .doOnNext { (started, submissionListing) ->
                    if (!started) {
                        repo.deleteAllFromSub()
                    }
                }
                .subscribe(
                        // onNext
                        { (started, submissionListing) ->
                            submissionListing.forEachIndexed { index, submission ->
                                PixelsApplication.pixelsCache?.submissionDao()?.insert(SubmissionCache(submission))
                            }
                        },
                        // onError
                        {
                            it.printStackTrace()
                        })
                .addTo(disposables)

        Observable.merge(input.loadMore, input.retry).publish()
                .also { loadTrigger /* combined loading trigger*/ ->
                    loadTrigger.map { input.isConnectedToInternet() }
                            .filter { it }
                            .filter { repo.isRedditClientAuthed() }
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .observeOn(Schedulers.newThread())
                            .subscribe { repo.loadMore() }
                            .addTo(disposables)

                    loadTrigger.map { input.isConnectedToInternet() }
                            .filter { !it }
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .observeOn(Schedulers.newThread())
                            .subscribe { showNoInternetSnackbarSubject.onNext(Unit) }
                            .addTo(disposables)
                }
                .connect()
                .addTo(disposables)

        input.retry
                .map { input.isConnectedToInternet() }
                .filter { it }
                .filter { !repo.isRedditClientAuthed() }
                .subscribe {
                    repo.initGuestRedditClient()
                    repo.loadMore()
                }
                .addTo(disposables)

        repo.bindToDb()?.subscribe { listing ->
            listing.forEachIndexed { index, submissionCache ->
                Log.d("ddw", "[${index}] ${submissionCache.author}: ${submissionCache.title}")
            }
        }?.addTo(disposables)

        if (input.isConnectedToInternet()) {
            repo.initGuestRedditClient()
            repo.loadMore()
        } else {
            showNoInternetSnackbarSubject.onNext(Unit)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        disposables.dispose()
        repo.destroy()
    }
}