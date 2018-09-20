package com.dmytrodanylyk.examples

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dmytrodanylyk.R
import kotlinx.android.synthetic.main.fragment_button.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.Main
import java.util.*
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlin.coroutines.experimental.CoroutineContext

class ExceptionHandlerFragment : Fragment() {

    private val uiDispatcher: CoroutineDispatcher = Dispatchers.Main
    private val dataProvider = DataProvider()
    private lateinit var job: Job

    companion object {
        const val TAG = "ExceptionHandlerFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_button, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button.setOnClickListener { loadData() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        job.cancel()
    }

    private val exceptionHandler: CoroutineContext = CoroutineExceptionHandler { _, throwable ->
        showText(throwable.message ?: "")
        hideLoading()
        job = Job() // exception handler cancels job
    }

    // we can attach CoroutineExceptionHandler to parent context
    private fun loadData() = GlobalScope.launch(uiDispatcher + exceptionHandler + job) {
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

    class DataProvider(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) {

        suspend fun loadData(): String = withContext(dispatcher) {
            delay(2, TimeUnit.SECONDS) // imitate long running operation
            mayThrowException()
            "Data is available: ${Random().nextInt()}"
        }

        private fun mayThrowException() {
            if (Random().nextBoolean()) {
                throw IllegalArgumentException("Ooops exception occurred")
            }
        }
    }
}