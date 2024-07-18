package com.example.a01_coroutine_launch

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.coroutines.EmptyCoroutineContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        coroutineSwitch()

    }


    /**
     * 2.协程:
     *  > 1. 首先添加依赖:文档说明如何添加依赖::https://github.com/Kotlin/kotlinx.coroutines/blob/master/README.md#using-in-your-projects
     *  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")   服务端的只要这个就可以.
     *  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0-RC")  安卓端需要这个.
     */
    private fun coroutineSwitch() {
        Log.i("TAG", "coroutineSwitch: ${Thread.currentThread().name} ")
        //协程中切线程和 线程池 切换线程很像.
        val threadPool = Executors.newCachedThreadPool()
        threadPool.execute {
            /// 这里是个Runnable 的对象.
            Log.i("TAG", "execute: ${Thread.currentThread().name}")
        }
        //Executor 本质是个线程池.

        /**
         *   CoroutineScope 比Executors 更强大,里面有个线程池.线程池只是它功能的一部分.
         *   CoroutineScope的参数是一个 CoroutineContext 对象.这个参数会提供各种上下文信息,包括使用哪个线程池.
         *   协程中 CoroutineScope 和 CoroutineContext 是非常重要的两个核心知识. 后续会慢慢展开.
         */

        val coroutineScope1 = CoroutineScope(EmptyCoroutineContext)
        //
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        /// 启动一个协程:launch {任务}  启动一个协程效果和 threadPool.execute {} 效果类似.
        coroutineScope.launch {
            /**
             * 因为我们现在学习的是JVM上的协程,所以开启一个协程实际上还是在切线程. 至于是切到哪个线程是 coroutineScope 决定的.
             * 和 Executor 一样都是 把代码块执行的任务丢给 线程池.
             */
            Log.i("TAG", "coroutineScope-launch: ${Thread.currentThread().name}")
        }
        /**
         * 打印观察线程名称:
         * 00:21:15.114  I  coroutineSwitch: main
         * 00:21:15.150  I  execute: pool-2-thread-1
         * 00:21:15.151  I  coroutineScope-launch: DefaultDispatcher-worker-2
         */

        ///以上就是协程最基本的切线程的用法和传统API里面的Executor 的用法非常相似

    }


    /**
     * 1.协程的概念和线程的切换.
     */
    private fun threadSwitch01() {

        /**
         * 针对JVM上的协程。
         * 什么是协程： 协程就是用线程来实现的并发管理库。
         * 本节内容：切线程 ：launch
         * 本节关键字：
         * 关键字：CoroutineScope  launch() ContinuationInterceptor  CoroutineDispatcher
         *
         */

        /**
         * java 中切线程：--为何要切:为了不卡主当前的线程,在别的线程中去并行执行某个任务.
         * 通常会把这种 并行线程叫做 子线程 或者 后台线程 都是一个意思.
         */
        thread {
            Log.i("TAG", "onCreate: ${Thread.currentThread().name}")
        }
        // 实际项目中一般会使用 线程池 来操作线程
        val executor =   Executors.newCachedThreadPool()
        /// 使用线程池中的线程去执行任务
        executor.execute {
            Log.i("TAG", "executor线程池: ${Thread.currentThread().name}")
        }

        /**
         * 对于带界面的程序 还有个特定的线程专门来更新界面 ,叫做 UI 线程. 如安卓中的UI 线程，,Swing 的事件分发线程.
         * 框架会提供相关的API 如 安卓是 Handler 或者 View.post{  }
         */
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            ///
            Log.i("TAG", "handler post: ${Thread.currentThread().name}")
        }
        /// 为了更新界面 把任务切到 UI 线程中.
        /**
         * 所以说切线程 就两种形式:
         * 1.切到 并行线程或 者后台线程 子线程(都是一个意思)
         * 2.切到 UI 线程
         */

    }
}