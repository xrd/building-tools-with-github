package com.example.ghru

import android.util.Log

import org.eclipse.egit.github.core.*
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.service.CommitService
import org.eclipse.egit.github.core.service.DataService
import org.eclipse.egit.github.core.service.RepositoryService
import org.eclipse.egit.github.core.service.UserService
import org.apache.commons.codec.binary.Base64

import java.text.SimpleDateFormat
import java.util.Date
import java.io.IOException
import java.util.*

internal class GitHubHelper(var login: String, var password: String) {

//    @Throws(IOException::class)
    fun SaveFile(_repoName: String,
                 _title: String,
                 _post: String,
                 _commitMessage: String): Boolean {
        post = _post
        repoName = _repoName
        title = _title
        commitMessage = _commitMessage

        var rv = false

        generateContent()
        createServices()
        retrieveBaseSha()

        if (null != baseCommitSha && "" !== baseCommitSha) {
            createBlob()
            generateTree()
            createCommitUser()
            createCommit()
            createResource()
            updateMasterResource()
            rv = true
        }

        return rv
    }

    var newTree: Tree = null!!
    var commitMessage: String = null!!
    var postContentsWithYfm: String = ""
    var contentsBase64: String = ""
    var filename: String = ""
    var post: String = ""
    var title: String = ""
    var repoName: String = ""

    private fun generateContent() {
        postContentsWithYfm = "---\n" +
                "layout: post\n" +
                "published: true\n" +
                "title: '" + title + "'\n---\n\n" +
                post
        contentsBase64 = String(Base64.encodeBase64(postContentsWithYfm.toByteArray()))
        filename = buildFilename()
    }

    private fun buildFilename(): String {
        val titleSub = title.substring(0,
                if (post.length > 30)
                    30
                else
                    title.length)
        val jekyllfied = titleSub.toLowerCase()
                .replace("\\\\W+".toRegex(), "-")
                .replace("\\\\W+$".toRegex(), "")
        val sdf = SimpleDateFormat("yyyy-MM-dd-")
        val prefix = sdf.format(Date())
        return "_posts/$prefix$jekyllfied.md"
    }

    var blobSha: String = ""
    var blob: Blob = null
    @Throws(IOException::class)
    private fun createBlob() {
        blob = Blob()
        blob.content = contentsBase64
        blob.encoding = Blob.ENCODING_BASE64
        blobSha = dataService.createBlob(repository, blob)
    }

    var baseTree: Tree = null
    @Throws(IOException::class)
    private fun generateTree() {
        baseTree = dataService.getTree(repository, baseCommitSha)
        val treeEntry = TreeEntry()
        treeEntry.path = filename
        treeEntry.mode = TreeEntry.MODE_BLOB
        treeEntry.type = TreeEntry.TYPE_BLOB
        treeEntry.sha = blobSha
        treeEntry.size = blob.content.length.toLong()
        val entries = ArrayList<TreeEntry>()
        entries.add(treeEntry)
        newTree = dataService.createTree(repository, entries, baseTree.sha)
    }

    var repositoryService: RepositoryService = null!!
    var commitService: CommitService = null!!
    var dataService: DataService = null!!

    @Throws(IOException::class)
    private fun createServices() {
        val ghc = GitHubClient()
        ghc.setCredentials(login, password)
        repositoryService = RepositoryService(ghc)
        commitService = CommitService(ghc)
        dataService = DataService(ghc)
    }

    var repository: Repository = null!!
    var theBranch: RepositoryBranch? = null
    var baseCommitSha: String? = null
    @Throws(IOException::class)
    private fun retrieveBaseSha() {
        // get some sha's from current state in git
        repository = repositoryService.getRepository(login, repoName)
        theBranch = branch
        baseCommitSha = theBranch!!.commit.sha
    }

    // Iterate over the branches and find gh-pages or master
    val branch: RepositoryBranch
        @Throws(IOException::class)
        get() {
            val branches = repositoryService.getBranches(repository)
            var master: RepositoryBranch? = null
            for (i in branches) {
                val theName = i.name.toString()
                if (theName.equals("gh-pages", ignoreCase = true)) {
                    theBranch = i
                } else if (theName.equals("master", ignoreCase = true)) {
                    master = i
                }
            }
            if (null == theBranch) {
                theBranch = master
            }
            return theBranch
        }

    var commitUser: CommitUser
    @Throws(IOException::class)
    private fun createCommitUser() {
        val us = UserService()
        us.client.setCredentials(login, password)
        commitUser = CommitUser()
        val user = us.user
        commitUser.date = Date()
        var name: String? = user.name
        if (null == name || name.isEmpty()) {
            name = "Unknown"
        }

        commitUser.name = name
        var email: String? = user.email
        if (null == email || email.isEmpty()) {
            email = "unknown@example.com"
        }
        commitUser.email = email
    }

    var newCommit: Commit
    @Throws(IOException::class)
    private fun createCommit() {
        val commit = Commit()
        commit.message = commitMessage
        commit.author = commitUser
        commit.committer = commitUser
        commit.tree = newTree
        val listOfCommits = ArrayList<Commit>()
        val parentCommit = Commit()
        parentCommit.sha = baseCommitSha
        listOfCommits.add(parentCommit)
        commit.parents = listOfCommits
        newCommit = dataService.createCommit(repository, commit)
    }

    var commitResource: TypedResource
    private fun createResource() {
        commitResource = TypedResource()
        commitResource.sha = newCommit.sha
        commitResource.type = TypedResource.TYPE_COMMIT
        commitResource.url = newCommit.url
    }

    @Throws(IOException::class)
    private fun updateMasterResource() {
        val reference = dataService.getReference(repository,
                "heads/" + theBranch!!.name)
        reference.`object` = commitResource
        dataService.editReference(repository, reference, true)
    }
}
