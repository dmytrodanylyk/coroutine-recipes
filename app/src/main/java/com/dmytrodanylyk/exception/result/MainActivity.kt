@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.dmytrodanylyk.exception.result

import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import com.dmytrodanylyk.R
import com.dmytrodanylyk.getThreadMessage
import com.dmytrodanylyk.logd
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.util.*
import kotlin.coroutines.experimental.CoroutineContext

class MainActivity : AppCompatActivity(), MainView {

    private lateinit var presenter: MainPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        presenter = MainPresenter(this, DataProvider())
        presenter.startPresenting()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.stopPresenting()
    }

    override fun showData(data: String?) {
        logd("showData $data" + getThreadMessage())
    }

    override fun showLoading() {
        logd("showLoading" + getThreadMessage())
    }
}

class DataProvider : DataProviderAPI {

    override fun loadData(params: String): Result<String> {
        SystemClock.sleep(5000)
        try {
            mayThrowException()
        } catch (e: RuntimeException) {
            return Result(error = e)
        }
        return Result(success = "data for $params")
    }

    private fun mayThrowException() {
        if (Random().nextBoolean()) {
            throw IllegalStateException("Ooops you are unlucky")
        }
    }

}

data class Result<out T>(val success: T? = null, val error: Throwable? = null)

interface DataProviderAPI {

    fun loadData(params: String): Result<String>
}

interface MainView {
    fun showData(data: String?)
    fun showLoading()
}

/**
 * handle exceptions inside DataProvider, return Result object
 */
class MainPresenter(private val view: MainView,
                    private val dataProvider: DataProviderAPI,
                    private val uiContext: CoroutineContext = UI,
                    private val bgContext: CoroutineContext = CommonPool) {

    private var job: Job? = null

    fun startPresenting() {
        job = loadData()
    }

    fun stopPresenting() {
        job?.cancel()
    }

    private fun loadData() = launch(uiContext) {
        view.showLoading() // ui thread

        val task = async(bgContext) { dataProvider.loadData("Task") }
        val result: Result<String> = task.await() // non ui thread, suspend until the task is finished

        if (result.success != null) {
            view.showData(result.success) // ui thread
        } else if (result.error != null) {
            result.error.printStackTrace()
        }
    }

}