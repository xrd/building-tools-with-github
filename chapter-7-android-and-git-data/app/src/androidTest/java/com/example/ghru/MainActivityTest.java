package com.example.ghru;

import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.*;

public class MainActivityTest 
    extends ActivityInstrumentationTestCase2<MainActivity> {

    public MainActivityTest() {
        super( MainActivity.class );
    }

    public void testLogin() {
        injectInstrumentation( InstrumentationRegistry.getInstrumentation() );
        MainActivity mainActivity = getActivity();
        String username = mainActivity
                .getString( R.string.github_helper_username );
        onView( withId( R.id.username ) )
	        .perform( typeText( username ) );
        String password = mainActivity
                .getString( R.string.github_helper_password );
        onView( withId( R.id.password ) )
	        .perform( typeText( password ) );
        onView( withId( R.id.login ) )
	        .perform( click() );
        onView( withId( R.id.status ) )
	        .check( matches( withText( "Logged into GitHub" ) ) );


    }
}
