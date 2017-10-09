package com.dmytrodanylyk

import com.dmytrodanylyk.cancel.DataProviderAPI
import com.dmytrodanylyk.cancel.MainPresenter
import com.dmytrodanylyk.cancel.MainView
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import kotlin.coroutines.experimental.EmptyCoroutineContext
import org.mockito.Mockito.`when` as whenMockito

class MainPresenterTest {

    private val MOCK_RESULT = "MOCK_RESULT"

    @Test
    fun startPresenting() {
        // mock
        val mockView = mock(MainView::class.java)
        val mockDataProvider = mock(DataProviderAPI::class.java)
        whenMockito(mockDataProvider.loadData(ArgumentMatchers.anyString())).thenReturn(MOCK_RESULT)

        val presenter = MainPresenter(mockView, mockDataProvider, EmptyCoroutineContext, EmptyCoroutineContext)

        // test
        presenter.startPresenting()

        // verify
        verify(mockView).showLoading()
        verify(mockDataProvider).loadData("Task")
        verify(mockView).showData(MOCK_RESULT)
    }
}
