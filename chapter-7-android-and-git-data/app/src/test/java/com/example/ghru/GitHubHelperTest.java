package com.example.ghru;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertTrue;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class GitHubHelperTest {
    @Test
    public void testClient() throws Exception {

        String login = System.getenv("GITHUB_HELPER_USERNAME");
        String password = System.getenv("GITHUB_HELPER_PASSWORD");
        String repoName = login + ".github.io";

        int randomNumber = (int)(Math.random() * 10000000);
        String randomString = String.valueOf( randomNumber );
        String randomAndDate = randomString + " " + (new Date()).toString() ;

        GitHubHelper ghh = new GitHubHelper( login, password );
        ghh.SaveFile(repoName, 
		     "Some random title", 
		     "Some random body text", 
		     randomAndDate );

        Thread.sleep(3000);

        String url = "https://api.github.com/repos/" + 
	    login + "/" + repoName + "/events";
        OkHttpClient ok = new OkHttpClient();
        Request request = new Request.Builder()
                .url( url )
                .build();
        Response response = ok.newCall( request ).execute();
        String body = response.body().string();

        assertTrue( "Body does not have: " + randomAndDate,  
		    body.contains( randomAndDate ) );
    }

}

