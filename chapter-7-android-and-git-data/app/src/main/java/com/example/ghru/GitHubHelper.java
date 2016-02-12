package com.example.ghru;

import android.util.Log;

import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.apache.commons.codec.binary.Base64;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;
import java.util.*;

class GitHubHelper {

    String login;
    String password;

    GitHubHelper( String _login, String _password ) {
        login = _login;
        password = _password;
    }

    public boolean SaveFile( String _repoName,
                             String _title,
                             String _post,
                             String _commitMessage ) throws IOException {
        post = _post;
        repoName = _repoName;
        title = _title;
        commitMessage = _commitMessage;

        boolean rv = false;

        generateContent();
        createServices();
        retrieveBaseSha();

        if( null != baseCommitSha && "" != baseCommitSha ) {
            createBlob();
            generateTree();
            createCommitUser();
            createCommit();
            createResource();
            updateMasterResource();
            rv = true;
        }

        return rv;
    }

    Tree newTree;
    String commitMessage;
    String postContentsWithYfm;
    String contentsBase64;
    String filename;
    String post;
    String title;
    String repoName;

    private void generateContent() {
        postContentsWithYfm =
                "---\n" +
                        "layout: post\n" +
                        "published: true\n" +
                        "title: '" + title + "'\n---\n\n" +
                        post;
        contentsBase64 =
                new String( Base64.encodeBase64( postContentsWithYfm.getBytes() ) );
        filename = getFilename();
    }

    private String getFilename() {
        String titleSub = title.substring( 0,
                post.length() > 30 ?
                        30 :
                        title.length() );
        String jekyllfied = titleSub.toLowerCase()
                .replaceAll( "\\\\W+", "-")
                .replaceAll( "\\\\W+$", "" );
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd-" );
        String prefix = sdf.format( new Date() );
        return "_posts/" + prefix + jekyllfied + ".md";
    }

    String blobSha;
    Blob blob;
    private void createBlob() throws IOException {
        blob = new Blob();
        blob.setContent(contentsBase64);
        blob.setEncoding(Blob.ENCODING_BASE64);
        blobSha = dataService.createBlob(repository, blob);
    }

    Tree baseTree;
    private void generateTree() throws IOException {
        baseTree = dataService.getTree(repository, baseCommitSha);
        TreeEntry treeEntry = new TreeEntry();
        treeEntry.setPath( filename );
        treeEntry.setMode( TreeEntry.MODE_BLOB );
        treeEntry.setType( TreeEntry.TYPE_BLOB );
        treeEntry.setSha(blobSha);
        treeEntry.setSize(blob.getContent().length());
        Collection<TreeEntry> entries = new ArrayList<TreeEntry>();
        entries.add(treeEntry);
        newTree = dataService.createTree( repository, entries, baseTree.getSha() );
    }

    RepositoryService repositoryService;
    CommitService commitService;
    DataService dataService;

    private void createServices() throws IOException {
        GitHubClient ghc = new GitHubClient();
        ghc.setCredentials( login, password );
        repositoryService = new RepositoryService( ghc );
        commitService = new CommitService( ghc );
        dataService = new DataService( ghc );
    }

    Repository repository;
    RepositoryBranch theBranch;
    String baseCommitSha;
    private void retrieveBaseSha() throws IOException {
        // get some sha's from current state in git
        repository =  repositoryService.getRepository(login, repoName);
        theBranch = getBranch();
        baseCommitSha =  theBranch.getCommit().getSha();
    }

    public RepositoryBranch getBranch() throws IOException {
        List<RepositoryBranch> branches = repositoryService.getBranches(repository);
        RepositoryBranch master = null;
        // Iterate over the branches and find gh-pages or master
        for( RepositoryBranch i : branches ) {
            String theName = i.getName().toString();
            if( theName.equalsIgnoreCase("gh-pages") ) {
                theBranch = i;
            }
            else if( theName.equalsIgnoreCase("master") ) {
                master = i;
            }
        }
        if( null == theBranch ) {
            theBranch = master;
        }
        return theBranch;
    }

    CommitUser commitUser;
    private void createCommitUser() throws IOException {
        UserService us = new UserService();
        us.getClient().setCredentials( login, password );
        commitUser = new CommitUser();
        User user = us.getUser();
        commitUser.setDate(new Date());
        String name = user.getName();
        if( null == name || name.isEmpty() ) {
            name = "Unknown";
        }

        commitUser.setName( name );
        String email = user.getEmail();
        if( null == email || email.isEmpty() ) {
            email = "unknown@example.com";
        }
        commitUser.setEmail( email );
    }

    Commit newCommit;
    private void createCommit() throws IOException {
        Commit commit = new Commit();
        commit.setMessage(commitMessage);
        commit.setAuthor(commitUser);
        commit.setCommitter(commitUser);
        commit.setTree(newTree);
        List<Commit> listOfCommits = new ArrayList<Commit>();
        Commit parentCommit = new Commit();
        parentCommit.setSha(baseCommitSha);
        listOfCommits.add(parentCommit);
        commit.setParents(listOfCommits);
        newCommit = dataService.createCommit(repository, commit);
    }

    TypedResource commitResource;
    private void createResource() {
        commitResource = new TypedResource();
        commitResource.setSha(newCommit.getSha());
        commitResource.setType(TypedResource.TYPE_COMMIT);
        commitResource.setUrl(newCommit.getUrl());
    }

    private void updateMasterResource() throws IOException {
        Reference reference =
                dataService.getReference(repository,
                        "heads/" + theBranch.getName() );
        reference.setObject(commitResource);
        dataService.editReference(repository, reference, true) ;
    }
}
