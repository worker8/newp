package beepbeep.pixels.feed

import beepbeep.pixels.feed.FeedViewModel.Companion.reduce
import beepbeep.pixels.feed.list.FeedListViewModel
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

class FeedReducerTest {

    lateinit var initial: FeedViewModel

    @Before
    fun before() {
        initial = FeedViewModel()
    }

    @Test
    fun `pass set action, next state has the correct number of items`() {
        val next = reduce(initial, SetAction(createViewModel0()))

        assertThat(next, equalTo(FeedViewModel(false, createViewModel0())))

        val next2 = reduce(next, SetAction(createViewModel3()))

        assertThat(next2, equalTo(FeedViewModel(false, createViewModel3())))
    }

    @Test
    fun `pass load action, next state should represent loading state`() {
        var next = reduce(initial, LoadAction)

        assertThat(next, equalTo(FeedViewModel(true, emptyList())))

        next = FeedViewModel(true, createViewModel3())

        val next2 = reduce(next, LoadAction)

        assertThat(next2, equalTo(FeedViewModel(true, createViewModel3())))
    }

    private fun createViewModel0() = emptyList<FeedListViewModel>()

    private fun createViewModel3() = listOf(
            FeedListViewModel("123"),
            FeedListViewModel("456"),
            FeedListViewModel("789")
    )
}