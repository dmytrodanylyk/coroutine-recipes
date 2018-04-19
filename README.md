# Learn Kotlin Coroutines for Android by example

Artcle: [Android Coroutine Recipes](https://medium.com/@dmytrodanylyk/android-coroutine-recipes-33467a4302e9)

Slides: [Android Coroutine Recipes](https://speakerdeck.com/dmytrodanylyk/android-coroutine-recipes)

![](assets/Screenshot.png)

Contains following examples:

- How to launch a coroutine [source](app/src/main/java/com/dmytrodanylyk/examples/LaunchFragment.kt)

```kotlin
fun loadData() = launch(uiContext, parent = job) {
    showLoading() // ui thread

    val result = dataProvider.loadData() // non ui thread, suspend until finished

    showText(result) // ui thread
    hideLoading() // ui thread
}
```

- How to launch coroutine with a timeout [source](app/src/main/java/com/dmytrodanylyk/examples/LaunchTimeoutFragment.kt)

```kotlin
fun loadData() = launch(uiContext, parent = job) {
    showLoading()

    val result = withTimeoutOrNull(1, TimeUnit.SECONDS) { dataProvider.loadData() }

    showText(result ?: "Timeout")
    hideLoading()
}
```

- How to launch coroutine which perform 2 background tasks sequentially [source](app/src/main/java/com/dmytrodanylyk/examples/LaunchSequentiallyFragment.kt)

```kotlin
fun loadData() = launch(uiContext, parent = job) {
    showLoading()

    val result1 = dataProvider.loadData()
    val result2 = dataProvider.loadData()

    showText("$result1\n$result2")
    hideLoading()
}
```

- How to launch coroutine which perform 2 background tasks in parallel [source](app/src/main/java/com/dmytrodanylyk/examples/LaunchParallelFragment.kt)

```kotlin
fun loadData() = launch(uiContext, parent = job) {
    showLoading()

    val result1 = async { dataProvider.loadData() }
    val result2 = async { dataProvider.loadData() }

    val data = "${result1.await()}\n${result2.await()}"
    showText(data)
    hideLoading()
}
```

- How to cancel a coroutine [source](app/src/main/java/com/dmytrodanylyk/examples/CancelFragment.kt)

```kotlin
private var job: Job = Job()

fun loadData() = launch(uiContext, parent = job) {
    ...
}

override fun onDestroyView() {
    super.onDestroyView()
    job.cancel()
}

```

- How to catch exception thrown inside coroutine (try/catch) [source](app/src/main/java/com/dmytrodanylyk/examples/ExceptionFragment.kt)

```kotlin
fun loadData() = launch(uiContext, parent = job) {
    showLoading()

    try {
        val result = dataProvider.loadData()
        showText(result)
    } catch (e: IllegalArgumentException) {
        showText(e.message ?: "")
    }

    hideLoading()
}
```

- How to catch exception thrown inside coroutine (exception handler) [source](app/src/main/java/com/dmytrodanylyk/examples/ExceptionHandlerFragment.kt)

```kotlin
val exceptionHandler: CoroutineContext = CoroutineExceptionHandler { _, throwable ->
    showText(throwable.message ?: "")
    hideLoading()
}

// we can attach CoroutineExceptionHandler to parent context
fun loadData() = launch(uiContext + exceptionHandler, parent = job) {
    ...
}
```