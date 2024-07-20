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
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.newSingleThreadContext
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.coroutines.ContinuationInterceptor
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
        //threadSwitch01()
        //coroutineSwitch()
        //threadPoolConfig()




    }

    /**
     * 3.线程池的配置 ContinuationInterceptor
     */
    private fun threadPoolConfig() {
        val threadPool = Executors.newCachedThreadPool()
        threadPool.execute {
            Log.i("TAG", "threadPoolConfig: ${Thread.currentThread().name}")
        }

        //协程默认配置--这种最简单的配置来启动协程,那么协程就会用默认的线程池中的某个线程来执行任务.
        val coroutineScope = CoroutineScope(EmptyCoroutineContext)
        coroutineScope.launch {
            Log.i("TAG", "coroutineScope: ${Thread.currentThread().name}")
        }

        /**
         * 协程中这里 "管理任务执行的线程"的工具 叫 ContinuationInterceptor
         * 执意 继续 拦截器. 也就是 拦截一下,做点别的工作, 在继续执行. 实际上就是切个线程在往下执行.
         * 协程官方的 实现就是切了线程的.
         * 协程给我们提供了四个 ContinuationInterceptor 在哪里呢?
         *
         * public actual object Dispatchers {
         *     @JvmStatic
         *     public actual val Default: CoroutineDispatcher = DefaultScheduler
         *
         *     @JvmStatic
         *     public actual val Main: MainCoroutineDispatcher get() = MainDispatcherLoader.dispatcher
         *
         *     @JvmStatic
         *     public actual val Unconfined: CoroutineDispatcher = kotlinx.coroutines.Unconfined
         *
         *     @JvmStatic
         *     public val IO: CoroutineDispatcher = DefaultIoScheduler
         *
         *     ...
         *     }
         *   Default  Main    Unconfined  IO 这4个.
         *
         *   为何叫 Dispatchers 不叫 ContinuationInterceptor ,因为 并不是直接实现 ContinuationInterceptor
         *   而是实现了ContinuationInterceptor唯一的子类. CoroutineDispatcher--- 协程 调度器 ,调度什么呢?调度任务,也就是切线程.
         *   虽然 ContinuationInterceptor 名字比较抽象,但是 CoroutineDispatcher 名字就比较好理解了.
         *   它和线程API里的 Executor 的定位属于一类,都是管理线程的.用Dispatchers不仅可以切换到后台线程也可以切换到 UI 线程和主线程.
         *   而Executor 只负责后台线程.
         *   这就是 为何叫 Dispatchers,因为内部装了几个 CoroutineDispatcher 的对象
         *   它俩的更深层次在后续章节.
         *   17分
         */

        /**
         * 如果CoroutineScope 没有指定 CoroutineContext 那么默认使用的是 Dispatchers.Default
         * Dispatchers.Default 它提供了一个全局的线程池来管理任务.
         * 用它来启动协程会在它提供的线程池里去运行.
         * 跟他类似的 还有一个 Dispatchers.IO,也是提供一个后台线程池.
         *
         * Dispatchers.Default 和 Dispatchers.IO 区别:
         * Dispatchers.Default : 处理计算密集型的,计算密集型指的是需要大量的CPU资源来做运算,
         * 比如滤镜算法图片处理-CPU计算充满整个处理流程, 此调度程序经过了专门优化，适合在主线程之外执行占用大量 CPU 资源的工作。用例示例包括对列表排序和解析 JSON。
         * Dispatchers.Default 的线程数是个CPU核心数是相等的,有几个CPU核就有几个线程. 因为线程太多反而会降低效率.
         * 比如如果是8个核心的CPU,如果开了8个以上的线程,线程执行是需要切换的,切换线程是比较耗时的.
         *
         *
         * Dispatchers.IO : 是处理IO密集型的. IO 指的是内存以外世界交互的输入输出 如网络的读写,磁盘数据的读写等,
         * 虽然耗时但是CPU资源不会被占用太多.CPU 运行速度是很快的.大部分时间都在等待.
         * 特点:工作期间CPU 是闲置的.
         * 内存和磁盘读写 两者相比,内存读写是非常快的,CUP 读取 IO 时候大部分时间CPU是闲置的,读写这些是磁盘去读写的,包括网络数据是网卡来读写的.
         * - 此调度程序经过了专门优化，适合在主线程之外执行磁盘或网络 I/O。示例包括使用Room组件、从文件中读取数据或向文件中写入数据，以及运行任何网络操作。
         *  Dispatchers.IO 线程池有64个线程.超过 64核那么就多少CPU 就有多少个线程. 因为IO 读写,CPU其实是闲置的.
         *
         *  这俩本质区别就是内部线程池的线程数量不同. 知道了以上这些以后就 不在迷惑 到底用哪个 Default 还是 IO
         *  和磁盘交互,文件读写 网络访问 用IO
         *  图片处理等,媒体编解码,代码字符串拼接等等 在内存中处理 用Default.
         *
         *  协程提供的后台线程池的 ContinuationInterceptor 就这两个.
         */
        // 什么不设置 下面这个也是 Default的.
         CoroutineScope(EmptyCoroutineContext).launch {

         }
        //怎么用? 直接传入 CoroutineContext 对象. Dispatchers.IO 这样启动的协程就是 Dispatchers.IO控制
        CoroutineScope(Dispatchers.IO).launch {

        }
        /// 这样启动的协程就是由 Dispatchers.Default 来控制的.什么不设置 EmptyCoroutineContext 也是 Default的.
        CoroutineScope(Dispatchers.Default).launch {

        }

        /// coroutineScope1全局的 EmptyCoroutineContext 来控制.--可复用
        val coroutineScope1 = CoroutineScope(EmptyCoroutineContext)
        /// 也可以在 launch 的时候设置 --这次的在 launch 设置的线程池中.
        coroutineScope1.launch(Dispatchers.Default){

        }

        coroutineScope1.launch {
            //使用的默认 设置 CoroutineScope(EmptyCoroutineContext)
        }

        /**
         * Dispatchers.Main - 使用此调度程序可在 Android 主线程上运行协程。此调度程序只能用于与界面交互和执行快速工作。
         *  安卓的 UI 线程.如果是服务器程序这个就会报错了.
         */
        /// 在主线程 -
        CoroutineScope(Dispatchers.Main).launch {

        }

        /// 如果要自己创建线程池 如下: 协程也提供了.报黄 有精细注解,意思是容易出错.--因为不使用时候要关闭.
        val newPool = newFixedThreadPoolContext(4, "MyFixedThreadPool")
        newSingleThreadContext("singleThread") //单线程.  所以不用线程数
        ///....使用中..
        CoroutineScope(newPool).launch {
            //...... 在自己创建的线程池中做事,
        }
        newPool.close() //用完要关闭(要在合适的位置)
        // 为何那俩 Default 和 IO  不用关闭 因为 是全局的
        /// 但是自定义的这种线程池基本用不上.
        // 37分:https://www.bilibili.com/cheese/play/ep778877?csource=private_space_class_null&spm_id_from=333.999.0.0&pagetype=payback
        ///还有一个 Unconfined  不进行限制 ,用它启动的协程,直接就开始执行里面的代码了--不切线程
        /// Unconfined 不会把 挂起函数自动切回来. 而其他的会把线程切回来.
        /// 实际开发中不会用到.

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