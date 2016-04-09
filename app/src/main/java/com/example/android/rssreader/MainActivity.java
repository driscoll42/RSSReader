package com.example.android.rssreader;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String mFileContents;
    private Button btnPS4;
    private Button btnXBone;
    private Button btnAddLink;
    private EditText addedLink;
    private ListView listApps;
    public static final String MyPREFERENCES = "MyPrefs";
    SharedPreferences sharedpreferences;
    public static final String URLKey = "urlKey";
    public static String restoredText = "";
    private Realm realm;
    private RealmConfiguration realmConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // These operations are small enough that
        // we can generally safely run them on the UI thread.

        // Create the Realm configuration
        realmConfig = new RealmConfiguration.Builder(this).build();
        //Realm.deleteRealm(realmConfig);
        Realm.setDefaultConfiguration(realmConfig);
        // Open the Realm for the UI thread.
        realm = Realm.getInstance(realmConfig);


        addedLink = (EditText) findViewById(R.id.addedLink);


        Button btnAddLink = (Button) findViewById(R.id.btnAddLink);
        btnAddLink.setOnClickListener(this);

        Button btnPS4 = (Button) findViewById(R.id.btnPS4);
        btnPS4.setOnClickListener(this);

        Button btnXBone = (Button) findViewById(R.id.btnXBone);
        btnXBone.setOnClickListener(this);

    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        listApps = (ListView) findViewById(R.id.xmlListView);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, 0);
        restoredText = sharedpreferences.getString(URLKey, null);

        addedLink.setText(restoredText);
        RealmResults<FeedData> realmData = realm.where(FeedData.class).findAll();
        ArrayList<FeedData> myList = new ArrayList<FeedData>();

        // Load from file "cities.json" first time
        if (realmData != null) {

            for (FeedData test : realmData) {
                if (test.getTitle() != null) {
                    FeedData temp = new FeedData(test.getTitle(),
                            test.getLink(),
                            test.getDescription(),
                            test.getPubDate(),
                            test.getFeedLink(),
                            test.getCategory(),
                            test.isSaved());
                    myList.add(temp);
                }
            }

            ArrayAdapter<FeedData> arrayAdapterLoadedData = new ArrayAdapter<FeedData>(
                    MainActivity.this, R.layout.list_item, myList);
            listApps.setAdapter(arrayAdapterLoadedData);

        }
    }


    public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        switch (v.getId()) {
            //TODO: Fix the odd timing issue requiring two presses to get the data
            case R.id.btnPS4:
                new DownloadData().execute("http://feeds.ign.com/IGNPS4All?format=xml");
                ArrayList<FeedData> myList = new ArrayList<FeedData>();
                ParseFeedData parsePS4 = new ParseFeedData(mFileContents, "http://feeds.ign.com/IGNPS4All?format=xml", "PS4");
                parsePS4.process();

                Realm realm = Realm.getInstance(realmConfig);
                RealmResults<FeedData> PS4Data = realm.distinct(FeedData.class, "title");

                for (FeedData data : PS4Data) {
                    FeedData feedItems = realm.where(FeedData.class).equalTo(FeedData.TITLE, data.getTitle()).findFirst();
                    if (data.getTitle() != null) {
                        FeedData temp = new FeedData(feedItems.getTitle(),
                                feedItems.getLink(),
                                feedItems.getDescription(),
                                feedItems.getPubDate(),
                                feedItems.getFeedLink(),
                                feedItems.getCategory(),
                                feedItems.isSaved());
                        myList.add(temp);
                    }
                }

                ArrayAdapter<FeedData> arrayAdapter = new ArrayAdapter<FeedData>(
                        MainActivity.this, R.layout.list_item, myList);
                listApps.setAdapter(arrayAdapter);
                break;

            case R.id.btnXBone:

                new DownloadData().execute("http://feeds.ign.com/IGNXboxOneAll?format=xml");
                ParseFeedData parseXBone = new ParseFeedData(mFileContents);
                parseXBone.process();
                ArrayAdapter<FeedData> arrayAdapterXbone = new ArrayAdapter<FeedData>(
                        MainActivity.this, R.layout.list_item, parseXBone.getFeedDatas());
                listApps.setAdapter(arrayAdapterXbone);
                realm = Realm.getInstance(realmConfig);
                realm.beginTransaction();
                realm.clear(FeedData.class);
                realm.commitTransaction();
                break;

            case R.id.btnAddLink:
                //http://feeds.ign.com/ign/wii-u-all?format=xml
                new DownloadData().execute(addedLink.getText().toString());
                if (mFileContents == null) {
                    Toast.makeText(getApplicationContext(), "Invalid URL", Toast.LENGTH_SHORT).show();
                    AlertDialog dialog = builder.create();
                    // Display the alert dialog on interface
                    dialog.show();
                }

                ParseFeedData parseAddedLink = new ParseFeedData(mFileContents);
                parseAddedLink.process();
                ArrayAdapter<FeedData> arrayAdapterAddLink = new ArrayAdapter<FeedData>(
                        MainActivity.this, R.layout.list_item, parseAddedLink.getFeedDatas());
                listApps.setAdapter(arrayAdapterAddLink);
                break;

            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        sharedpreferences = getSharedPreferences(MyPREFERENCES, 0);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(URLKey, addedLink.getText().toString());
        editor.commit();
        realm.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close(); // Remember to close Realm when done.\
      //  Realm.deleteRealm(realmConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class DownloadData extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {
            mFileContents = downloadXMLFile(params[0]);
            if (mFileContents == null) {
                Log.d("DownloadData", "Error downloading");
            }
            return mFileContents;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("DownloadData", "Result was: " + result);
        }
    }


    private String downloadXMLFile(String urlPath) {
        StringBuilder tempBuffer = new StringBuilder();
        try {
            URL url = new URL(urlPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int response = connection.getResponseCode();
            Log.d("DownloadData", "The response code was " + response);
            InputStream is = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);

            int charRead;
            char[] inputBuffer = new char[500];
            while (true) {
                charRead = isr.read(inputBuffer);
                if (charRead <= 0) {
                    break;
                }
                tempBuffer.append(String.copyValueOf(inputBuffer, 0, charRead));
            }
            return tempBuffer.toString();

        } catch (IOException e) {
            Log.d("DownloadData", "IOException reading data:" + e.getMessage());
            e.printStackTrace();
        } catch (SecurityException e) {
            Log.d("DownloadData", "Security exception. Needs permissions? " + e.getMessage());
        }

        return null;
    }
}



