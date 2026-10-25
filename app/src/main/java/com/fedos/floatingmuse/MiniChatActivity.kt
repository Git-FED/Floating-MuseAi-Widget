package com.fedos.floatingmuse

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MiniChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mini_chat)
        findViewById<View>(R.id.close_button).setOnClickListener { finish() }
        findViewById<ChatWebView>(R.id.chat_web_view).loadUrl(getString(R.string.muse_chat_url))
    }

    override fun onDestroy() {
        findViewById<ChatWebView?>(R.id.chat_web_view)?.destroy()
        super.onDestroy()
    }
}
