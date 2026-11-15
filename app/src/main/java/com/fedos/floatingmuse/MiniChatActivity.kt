package com.fedos.floatingmuse

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import java.lang.ref.WeakReference

class MiniChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openInstance = WeakReference(this)
        setContentView(R.layout.mini_chat)

        window.setGravity(Gravity.CENTER)
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.attributes = window.attributes.apply { dimAmount = 0.55f }
        val metrics = resources.displayMetrics
        window.setLayout((metrics.widthPixels * 0.92f).toInt(), (metrics.heightPixels * 0.85f).toInt())

        findViewById<View>(R.id.close_button).setOnClickListener { finish() }
        findViewById<ChatWebView>(R.id.chat_web_view).loadUrl(getString(R.string.muse_chat_url))
    }

    override fun onDestroy() {
        findViewById<ChatWebView?>(R.id.chat_web_view)?.destroy()
        if (openInstance?.get() === this) openInstance = null
        super.onDestroy()
    }

    companion object {
        private var openInstance: WeakReference<MiniChatActivity>? = null

        fun closeOpenInstance(): Boolean {
            val activity = openInstance?.get()
            return if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
                activity.finish()
                true
            } else {
                openInstance = null
                false
            }
        }
    }
}
