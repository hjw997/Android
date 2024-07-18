package com.example.a01_coroutine_launch

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.concurrent.Executors
import kotlin.concurrent.thread

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