@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.dmytrodanylyk.exception.ignore

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

    override fun loadData(params: String): String {
        SystemClock.sleep(5000)
        mayThrowException()
        return "data for $params"
    }

    private fun mayThrowException() {
        if (Random().nextBoolean()) {
            throw IllegalStateException("Ooops you are unlucky")
        }
    }
}

interface DataProviderAPI {

    fun loadData(params: String): String
}

interface MainView {
    fun showData(data: String?)
    fun showLoading()
}

/**
 * ignore any exceptions
 */
class MainPresenter(private val view: MainView,
                    private val dataProvider: DataProviderAPI,
                    private val uiContext: CoroutineContext = UI,
                    private val ioContext: CoroutineContext = CommonPool) {

    private var job: Job? = null

    fun startPresenting() {
        job = loadData()
        job?.invokeOnCompletion { it: Throwable? ->
            it?.printStackTrace() // (1)
            // or
            job?.getCompletionException()?.printStackTrace() // (2)


            // difference between (1) and (2) is that (1) will NOT contain CancellationException
            // in case if job was cancelled
        }
    }

    fun stopPresenting() {
        job?.cancel()
    }

    private fun loadData() = async(uiContext) {
        view.showLoading() // ui thread

        val task = async(ioContext) { dataProvider.loadData("Task") }
        val result = task.await() // non ui thread, suspend until the task is finished

        view.showData(result) // ui thread
    }

}