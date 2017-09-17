package beepbeep.pixels.shared.extension

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun Disposable.addTo(composite: CompositeDisposable) = composite.add(this)