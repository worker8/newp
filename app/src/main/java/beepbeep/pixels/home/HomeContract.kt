package beepbeep.pixels.home

import android.arch.lifecycle.ViewModel
import beepbeep.pixels.cache.submission.SubmissionCache
import io.reactivex.Observable

class HomeContract {
    interface Input {
        fun isConnectedToInternet(): Boolean
        val loadMore: Observable<Any>
        val refresh: Observable<Any>
        val retry: Observable<Unit>
    }

    abstract class Output : ViewModel() {
        abstract val onDataLoaded: Observable<Unit>
        abstract val showNoInternetSnackbar: Observable<Unit>
        abstract val onCacheDataLoaded: Observable<List<SubmissionCache>>
    }
}