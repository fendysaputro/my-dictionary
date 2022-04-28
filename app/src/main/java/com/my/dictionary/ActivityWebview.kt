package com.my.dictionary

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import com.my.dictionary.data.GlobalVariable
import com.my.dictionary.data.Tools
import com.my.dictionary.theme.ActionBarColoring

class ActivityWebview : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var actionBar: ActionBar
    var globalVariable: GlobalVariable? = null
    private var parentView: View? = null
    var webView: WebView? = null
    var progressDialog: ProgressDialog? = null
    private var webId: Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        parentView = findViewById(android.R.id.content)
        globalVariable = application as GlobalVariable

        getIntentData()

        setupToolbar()
        setupWebview()
    }

    private fun getIntentData() {
        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            webId = bundle.getInt("dataInt", 0)
        }
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        actionBar = supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
        ActionBarColoring(this).getColor(actionBar)
        // for system bar in lollipop
        // for system bar in lollipop
        Tools.systemBarLolipop(this)
    }

    private fun setupWebview() {
        val urlWeb = "file:///android_asset/sample.html"
        val urlWebTwo = "file:///android_asset/sampletwo.html"

        webView = findViewById(R.id.wv_url)
        webView?.requestFocus()

        progressDialog = ProgressDialog(this)
        progressDialog?.setMessage("Loading")
        progressDialog?.setCancelable(false)
        progressDialog?.show()

        if (webId == 1) {
            val client: WebViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    view.loadUrl(url)
                    return false
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    progressDialog?.dismiss()
                }
            }

            val settings: WebSettings? = webView?.settings
            settings?.javaScriptEnabled = true
            webView?.settings?.domStorageEnabled = true
            webView?.settings?.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            webView?.webViewClient = client
            webView?.loadUrl(urlWeb)
        } else {
            val client: WebViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    view.loadUrl(url)
                    return false
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    progressDialog?.dismiss()
                }
            }

            val settings: WebSettings? = webView?.settings
            settings?.javaScriptEnabled = true
            webView?.settings?.domStorageEnabled = true
            webView?.settings?.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            webView?.webViewClient = client
            webView?.loadUrl(urlWebTwo)
        }


    }

    override fun onBackPressed() {
        val intent = Intent(applicationContext, ActivityMain::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        finish()
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val intent = Intent(applicationContext, ActivityMain::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

}