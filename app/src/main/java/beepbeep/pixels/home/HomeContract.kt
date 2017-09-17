package beepbeep.pixels.home

import android.arch.lifecycle.ViewModel
import io.reactivex.Observable

class HomeContract {
    interface Input {
        fun isConnectedToInternet(): Boolean
        val loadMore: Observable<Unit>
    }

    abstract class Output : ViewModel() {
        abstract val onDataLoaded: Observable<Unit>
    }
}