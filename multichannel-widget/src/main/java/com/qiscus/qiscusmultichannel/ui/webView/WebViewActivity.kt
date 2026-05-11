package com.qiscus.qiscusmultichannel.ui.webView

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings.LOAD_DEFAULT
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.qiscus.qiscusmultichannel.R
import com.qiscus.qiscusmultichannel.databinding.ActivityWebViewMcBinding
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created on : 14/02/20
 * Author     : arioki
 * Name       : Yoga Setiawan
 * GitHub     : https://github.com/arioki
 */

class WebViewActivity : AppCompatActivity() {

    private lateinit var webViewClient: WebViewClient
    private lateinit var binding: ActivityWebViewMcBinding

    private var mWebViewClient: WebViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            binding.tvTitle.text = view.title
            binding.tvUrl.text = view.url
            //view.loadUrl("javascript:window.android.onUrlChange(window.location.href);")
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            binding.tvTitle.text = view.title
            binding.tvUrl.text = view.url
            if (url.startsWith("intent://") && url.contains("scheme=http")) {
                val bkpUrl: String?
                val regexBkp: Pattern = Pattern.compile("intent://(.*?)#")
                val regexMatcherBkp: Matcher = regexBkp.matcher(url)
                return if (regexMatcherBkp.find()) {
                    bkpUrl = regexMatcherBkp.group(1)
                    val myIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://$bkpUrl"))
                    startActivity(myIntent)
                    finish()
                    true
                } else {
                    false
                }
            }
            return false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (android.os.Build.VERSION.SDK_INT >= 35) {
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        }
        binding = ActivityWebViewMcBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        onWindow()

        webViewClient = WebViewClient()
        val url = intent.getStringExtra("url")
        url?.let { binding.webview.loadUrl(it) }

        with(binding.webview.settings) {
            cacheMode = LOAD_DEFAULT
            javaScriptEnabled = true
            domStorageEnabled = true
            allowContentAccess = true
            allowFileAccess = true
        }

        binding.webview.webViewClient = mWebViewClient
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun onWindow() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}
