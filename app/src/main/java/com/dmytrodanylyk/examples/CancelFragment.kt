package com.dmytrodanylyk.examples

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dmytrodanylyk.R
import kotlinx.android.synthetic.main.fragment_cancel.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.CoroutineContext

class CancelFragment : Fragment() {

    private val uiContext: CoroutineContext = UI
    private val dataProvider = DataProvider()
    private var job: Job = Job()

    companion object {
        const val TAG = "LaunchFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_cancel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button.setOnClickListener {
            // if you want to know how coroutine was completed, attach invokeOnCompletion
            loadData().invokeOnCompletion {
                if (it is CancellationException) { // if coroutine was cancelled
                    textView.text = "Cancelled"
                    hideLoading()
                }
            }
        }
        buttonCancel.setOnClickListener {
            // cancelling parent job will cancel all attached child coroutines
            job.cancel()
            // we need to create new Job, because current is cancelled
            // new coroutine will not start, if parent Job is already cancelled
            job = Job()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job.cancel()
    }

    private fun loadData() = launch(uiContext, parent = job) {
        showLoading()

        val result = dataProvider.loadData()

        showText(result)
        hideLoading()
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
    }

    private fun showText(data: String) {
        textView.text = data
    }

    class DataProvider(private val context: CoroutineContext = CommonPool) {

        suspend fun loadData(): String = withContext(context) {
            delay(2, TimeUnit.SECONDS) // imitate long running operation
            "Data is available: ${Random().nextInt()}"
        }
    }
}