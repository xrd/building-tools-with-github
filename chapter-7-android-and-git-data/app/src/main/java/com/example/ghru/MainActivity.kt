package com.example.ghru

import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.view.View

import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.service.UserService

import java.io.IOException

class MainActivity : Activity() {

    internal var username: String = ""
    internal var password: String = ""

    /** Called when the activity is first created.  */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        val login = findViewById(R.id.login) as Button
        login.setOnClickListener {
            val utv = findViewById(R.id.username) as EditText
            val ptv = findViewById(R.id.password) as EditText
            username = utv.text.toString()
            password = ptv.text.toString()

            LoginTask().execute(username, password)
        }
    }

    private fun login() {

        setContentView(R.layout.logged_in)

        val submit = findViewById(R.id.submit) as Button
        submit.setOnClickListener {
            val post = findViewById(R.id.post) as EditText
            val postContents = post.text.toString()

            val repo = findViewById(R.id.repository) as EditText
            val repoName = repo.text.toString()

            val title = findViewById(R.id.title) as EditText
            val titleText = title.text.toString()

            doPost(repoName, titleText, postContents)
        }
    }

    private fun loggedIn() {

        setContentView(R.layout.logged_in)

        val submit = findViewById(R.id.submit) as Button
        submit.setOnClickListener {
            val post = findViewById(R.id.post) as EditText
            val postContents = post.text.toString()

            val repo = findViewById(R.id.repository) as EditText
            val repoName = repo.text.toString()

            val title = findViewById(R.id.title) as EditText
            val titleText = title.text.toString()

            doPost(repoName, titleText, postContents)
        }
    }

    internal inner class LoginTask : AsyncTask<String, Void, Boolean>() {
        override fun doInBackground(vararg credentials: String): Boolean? {
            var rv = false
            val us = UserService()
            us.client.setCredentials(credentials[0], credentials[1])
            try {
                val user = us.getUser(credentials[0])
                rv = null != user
            } catch (ioe: IOException) {
            }

            return rv
        }

        override fun onPostExecute(result: Boolean?) {
            if (result!!) {
                loggedIn()
            } else {
                val status = findViewById(R.id.login_status) as TextView
                status.text = "Invalid login, please check credentials"
            }
        }
    }

    private fun doPost(repoName: String, title: String, post: String) {
        PostTask().execute(username, password, repoName, title, post)
    }

    internal inner class PostTask : AsyncTask<String, Void, Boolean>() {

        override fun doInBackground(vararg information: String): Boolean? {
            val login = information[0]
            val password = information[1]
            val repoName = information[2]
            val titleText = information[3]
            val postContents = information[4]

            var rv: Boolean? = false
            val ghh = GitHubHelper(login, password)
            try {
                rv = ghh.SaveFile(repoName, titleText, postContents, "GhRu Update")
            } catch (ioe: IOException) {
                Log.d(ioe.stackTrace.toString(), "GhRu")
            }

            return rv
        }

        override fun onPostExecute(result: Boolean?) {
            val status = findViewById(R.id.status) as TextView
            if (result!!) {
                status.text = "Successful jekyll post"

                val post = findViewById(R.id.post) as EditText
                post.setText("")

                val repo = findViewById(R.id.repository) as EditText
                repo.setText("")

                val title = findViewById(R.id.title) as EditText
                title.setText("")
            } else {
                status.text = "Post failed."
            }
        }
    }

}
