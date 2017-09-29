package beepbeep.pixels.shared.extension

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun Disposable.addTo(composite: CompositeDisposable) = composite.add(this)

/**
 * alias for subscribeOn()
 */
fun <T> Flowable<T>.upScheduler(scheduler: Scheduler): Flowable<T> {
    return subscribeOn(scheduler)
}
/**
 * alias for observeOn()
 */
fun <T> Flowable<T>.downScheduler(scheduler: Scheduler): Flowable<T> {
    return observeOn(scheduler)
}
/**
 * alias for subscribeOn()
 */
fun <T> Observable<T>.upScheduler(scheduler: Scheduler): Observable<T> {
    return subscribeOn(scheduler)
}
/**
 * alias for observeOn()
 */
fun <T> Observable<T>.downScheduler(scheduler: Scheduler): Observable<T> {
    return observeOn(scheduler)
}