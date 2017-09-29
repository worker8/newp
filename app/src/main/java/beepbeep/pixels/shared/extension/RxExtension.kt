package beepbeep.pixels.shared.extension

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

fun Disposable.addTo(composite: CompositeDisposable) = composite.add(this)

fun <T> Flowable<T>.upScheduler(scheduler: Scheduler): Flowable<T> {
    return subscribeOn(scheduler)
}

fun <T> Flowable<T>.downScheduler(scheduler: Scheduler): Flowable<T> {
    return observeOn(scheduler)
}

fun <T> Observable<T>.upScheduler(scheduler: Scheduler): Observable<T> {
    return subscribeOn(scheduler)
}

fun <T> Observable<T>.downScheduler(scheduler: Scheduler): Observable<T> {
    return observeOn(scheduler)
}

fun <T> Observable<T>.upBackgroundThread(): Observable<T> {
    return subscribeOn(Schedulers.io())
}

fun <T> Observable<T>.upMainUiThread(): Observable<T> {
    return subscribeOn(AndroidSchedulers.mainThread())
}

fun <T> Observable<T>.downMainUiThread(): Observable<T> {
    return observeOn(AndroidSchedulers.mainThread())
}

fun <T> Observable<T>.downBackgroundThread(): Observable<T> {
    return observeOn(Schedulers.io())
}

fun <T> Flowable<T>.upBackgroundThread(): Flowable<T> {
    return subscribeOn(Schedulers.io())
}

fun <T> Flowable<T>.upMainUiThread(): Flowable<T> {
    return subscribeOn(AndroidSchedulers.mainThread())
}

fun <T> Flowable<T>.downMainUiThread(): Flowable<T> {
    return observeOn(AndroidSchedulers.mainThread())
}

fun <T> Flowable<T>.downBackgroundThread(): Flowable<T> {
    return observeOn(Schedulers.io())
}
