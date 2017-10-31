@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package com.dmytrodanylyk.library

import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import com.dmytrodanylyk.R
import com.dmytrodanylyk.logd
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.rx2.asSingle
import java.util.*
import kotlin.coroutines.experimental.CoroutineContext

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userProvider: UserProviderAPI = UserProvider()
        val userTask = userProvider.loadUser(UUID.randomUUID().toString())

        coroutine(userTask) // api usage via coroutine
        callback(userTask) // api usage via callback
        observable(userTask) // api usage via rxjava
    }

    private fun observable(userTask: Task<User?>) {
        userTask.observe()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { user, e ->
                    logd("observable " + user) // ui thread
                }
    }

    private fun callback(userTask: Task<User?>) {
        userTask.execute(UI) {
            // deliver result in ui thread
            logd("callback " + it) // ui thread
        }
    }

    private fun coroutine(userTask: Task<User?>) = launch(UI) {
        val user = userTask.execute().await() // background thread
        logd("coroutine " + user) // ui thread
    }
}

// core api

interface Task<out T> {

    fun observe(): Single<out T?>

    fun execute(): Deferred<T?>

    fun execute(callbackContext: CoroutineContext = CommonPool, block: (result: T?) -> Unit): Job?
}

// concrete api

data class User(val id: String)

interface UserProviderAPI {

    fun loadUser(id: String): Task<User?>
}

class UserProvider(val executionContext: CoroutineContext = CommonPool) : UserProviderAPI {

    override fun loadUser(id: String) = object : Task<User?> {

        override fun execute(callbackContext: CoroutineContext,
                             block: (result: User?) -> Unit) = launch(executionContext) {
            val user = execute().await()
            block.invoke(user)
        }

        override fun execute() = async(executionContext) { findUserById(id) }

        override fun observe() = execute().asSingle(executionContext)

    }

    private fun findUserById(id: String): User? {
        SystemClock.sleep(5000)
        return User(id)
    }

}