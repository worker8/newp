package beepbeep.pixels.home

import io.reactivex.subjects.PublishSubject
import org.junit.Assert.assertEquals
import org.junit.Test

class HomePresenterTest {
    var _isConnectedToInternet = false
    var _loadMore = PublishSubject.create<Any>()
    var _refresh = PublishSubject.create<Any>()
    var _retry = PublishSubject.create<Unit>()

    val input = object : HomeContract.Input {
        override fun isConnectedToInternet() = _isConnectedToInternet
        override val loadMore = _loadMore.hide()
        override val refresh = _refresh.hide()
        override val retry = _retry.hide()
    }

    val presenterUT = HomePresenter(input)
    @Test
    fun firstTest() {
        assertEquals(4, (2 + 2).toLong())
    }
}