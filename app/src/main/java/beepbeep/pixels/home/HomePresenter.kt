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


class HomePresenter(val input: HomeContract.Input, val repo: HomeRepoInterface) : LifecycleObserver {
    val disposables = CompositeDisposable()
    val onDataLoaded = PublishSubject.create<Unit>()

    val output = object : HomeContract.Output() {
        override val onDataLoaded: Observable<Unit>
            get() = onDataLoaded.hide()
    }

    init {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onCreate() {
        repo.data()
                .doOnNext { (started, submissionListing) ->
                    if (!started) {
                        repo.deleteAllFromSub()
                    }
                }
                .subscribe({ (started, submissionListing) ->
                    submissionListing.forEachIndexed { index, submission ->
                        PixelsApplication.pixelsCache?.submissionDao()?.insert(SubmissionCache(submission))
                    }
                }, {
                    it.printStackTrace()
                })
                .addTo(disposables)
        val loadMoreClick = input.loadMore.publish()
        loadMoreClick
                .map { input.isConnectedToInternet() }
                .filter { it }
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.newThread())
                .subscribe { repo.loadMore() }
                .addTo(disposables)
        loadMoreClick
                .map { input.isConnectedToInternet() }
                .filter { !it }
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.newThread())
                .subscribe { }
                .addTo(disposables)
        loadMoreClick.connect()

        repo.bindToDb()?.subscribe { listing ->
            listing.forEachIndexed { index, submissionCache ->
                Log.d("ddw", "[${index}] ${submissionCache.author}: ${submissionCache.title}")
            }
        }?.addTo(disposables)

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        disposables.dispose()
    }
}