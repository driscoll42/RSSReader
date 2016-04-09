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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
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
    private EditText addedCategory;
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
        addedCategory = (EditText) findViewById(R.id.addedCategory);


        Button btnAddLink = (Button) findViewById(R.id.btnAddLink);
        btnAddLink.setOnClickListener(this);

        Button btnPS4 = (Button) findViewById(R.id.btnDispCat);
        btnPS4.setOnClickListener(this);

        Button btnXBone = (Button) findViewById(R.id.btnSaved);
        btnXBone.setOnClickListener(this);


    }

    @Override
    public void onResume() {
        //TODO: Figure out why it doesn't immediately display the data
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

            //TODO: Turn populating the spinners into a class to be called, at this point I'm repeating code four times
            Spinner feedSpinner = (Spinner) findViewById(R.id.spnFeeds);

            ArrayList<String> feedList = new ArrayList<String>();
            RealmResults<FeedData> linkList = realm.distinct(FeedData.class, "feedLink");

            for (FeedData data : linkList) {
                FeedData feedItems = realm.where(FeedData.class).equalTo(FeedData.FEEDLINK, data.getFeedLink()).findFirst();
                if (data.getFeedLink() != null) {
                    String linkString = feedItems.getFeedLink();
                    feedList.add(linkString);
                }
            }


            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, feedList);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            feedSpinner.setAdapter(spinnerArrayAdapter);

            Spinner spinner = (Spinner) findViewById(R.id.spnCategory);

            ArrayList<String> catSpinList = new ArrayList<String>();
            RealmResults<FeedData> catListResults = realm.distinct(FeedData.class, "category");

            for (FeedData catData : catListResults) {
                FeedData feedItems = realm.where(FeedData.class).equalTo(FeedData.CATEGORY, catData.getCategory()).findFirst();
                if (catData.getCategory() != null) {
                    String catString = feedItems.getCategory();
                    catSpinList.add(catString);
                }
            }


            ArrayAdapter<String> spinnerCatArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, catSpinList);
            spinnerCatArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerCatArrayAdapter);

        }

        listApps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                FeedData app = (FeedData) parent.getItemAtPosition(position);
                realm.beginTransaction();
                FeedData toSave = realm.where(FeedData.class).equalTo("title", app.getTitle()).findFirst();
                toSave.setSaved(true);
                realm.commitTransaction();

            }
        });

    }


    public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        Realm realm = Realm.getInstance(realmConfig);

        switch (v.getId()) {
            //TODO: Fix the odd timing issue requiring two presses to get the data
            //"http://feeds.ign.com/IGNXboxOneAll?format=xml"
            //http://feeds.ign.com/IGNPS4All?format=xml
            //http://feeds.ign.com/ign/wii-u-all?format=xml
            case R.id.btnDispCat:
                Spinner catSpin=(Spinner) findViewById(R.id.spnCategory);
                String catText = catSpin.getSelectedItem().toString();
                Log.d("testsaved", "Button Pressed");

                ArrayList<FeedData> catList = new ArrayList<FeedData>();
                RealmResults<FeedData> catData = realm.where(FeedData.class).equalTo(FeedData.CATEGORY, catText).findAll();

                for (FeedData data : catData) {
                    if (data.getTitle() != null) {
                        FeedData temp = new FeedData(data.getTitle(),
                                data.getLink(),
                                data.getDescription(),
                                data.getPubDate(),
                                data.getFeedLink(),
                                data.getCategory(),
                                data.isSaved());
                        catList.add(temp);
                    }
                }

                ArrayAdapter<FeedData> arrayAdapterCategory = new ArrayAdapter<FeedData>(
                        MainActivity.this, R.layout.list_item, catList);
                listApps.setAdapter(arrayAdapterCategory);
                break;

            case R.id.btnDispData:
                Log.d("testsaved", "Button Pressed");

                Spinner linkSpin =(Spinner) findViewById(R.id.spnFeeds);
                String linkText = linkSpin.getSelectedItem().toString();

                ArrayList<FeedData> dataList = new ArrayList<FeedData>();
                RealmResults<FeedData> linkData = realm.distinct(FeedData.class, "title");

                for (FeedData data : linkData) {
                    if (data.getTitle() != null) {
                    FeedData feedItems = realm.where(FeedData.class).
                            equalTo(FeedData.TITLE, data.getTitle()).
                            equalTo(FeedData.FEEDLINK, linkText).
                            findFirst();
                        FeedData temp = new FeedData(feedItems.getTitle(),
                                feedItems.getLink(),
                                feedItems.getDescription(),
                                feedItems.getPubDate(),
                                feedItems.getFeedLink(),
                                feedItems.getCategory(),
                                feedItems.isSaved());
                        dataList.add(temp);
                    }
                }

                ArrayAdapter<FeedData> arrayAdapterLink = new ArrayAdapter<FeedData>(
                        MainActivity.this, R.layout.list_item, dataList);
                listApps.setAdapter(arrayAdapterLink);
                break;


            case R.id.btnSaved:

                ArrayList<FeedData> savedList = new ArrayList<FeedData>();
                RealmResults<FeedData> savedData = realm.where(FeedData.class).equalTo(FeedData.SAVED, true).findAll();

                for (FeedData data : savedData) {
                    if (data.getTitle() != null) {
                        Log.d("testsaved", data.getTitle());
                        FeedData temp = new FeedData(data.getTitle(),
                                data.getLink(),
                                data.getDescription(),
                                data.getPubDate(),
                                data.getFeedLink(),
                                data.getCategory(),
                                data.isSaved());
                        savedList.add(temp);
                    }
                }

                ArrayAdapter<FeedData> arrayAdapterSaved = new ArrayAdapter<FeedData>(
                        MainActivity.this, R.layout.list_item, savedList);
                listApps.setAdapter(arrayAdapterSaved);
                break;

            case R.id.btnAddLink:
                //TODO: Check that I'm not duplicating data, because I totally am
                //Consoles
                //"http://feeds.ign.com/IGNXboxOneAll?format=xml
                //http://feeds.ign.com/IGNPS4All?format=xml
                //http://feeds.ign.com/ign/wii-u-all?format=xml
                //Handhelds
                //http://feeds.ign.com/ign/ds-all?format=xml
                //http://feeds.ign.com/ign/ps-vita-all?format=xml

                String newLink = addedLink.getText().toString();
                String assignCat = addedCategory.getText().toString();

                new DownloadData().execute(addedLink.getText().toString());
                if (mFileContents == null) {
                    Toast.makeText(getApplicationContext(), "Invalid URL", Toast.LENGTH_SHORT).show();
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    ArrayList<FeedData> addedList = new ArrayList<FeedData>();
                    ParseFeedData parseAddedLink = new ParseFeedData(mFileContents, newLink, assignCat);
                    parseAddedLink.process();

                    RealmResults<FeedData> addedData = realm.distinct(FeedData.class, "title");

                    for (FeedData data : addedData) {
                        FeedData feedItems = realm.where(FeedData.class).equalTo(FeedData.TITLE, data.getTitle()).findFirst();
                        if (data.getTitle() != null) {
                            FeedData temp = new FeedData(feedItems.getTitle(),
                                    feedItems.getLink(),
                                    feedItems.getDescription(),
                                    feedItems.getPubDate(),
                                    feedItems.getFeedLink(),
                                    feedItems.getCategory(),
                                    feedItems.isSaved());
                            addedList.add(temp);
                            Log.d("AddedData", temp.toString());
                        }
                    }


                    ArrayAdapter<FeedData> arrayAdapterAddLink = new ArrayAdapter<FeedData>(
                            MainActivity.this, R.layout.list_item, addedList);
                    listApps.setAdapter(arrayAdapterAddLink);
                }

                Spinner feedSpinner = (Spinner) findViewById(R.id.spnFeeds);

                ArrayList<String> feedList = new ArrayList<String>();
                RealmResults<FeedData> linkList = realm.distinct(FeedData.class, "feedLink");

                for (FeedData data : linkList) {
                    FeedData feedItems = realm.where(FeedData.class).equalTo(FeedData.FEEDLINK, data.getFeedLink()).findFirst();
                    if (data.getFeedLink() != null) {
                        String linkString = feedItems.getFeedLink();
                        feedList.add(linkString);
                    }
                }


                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, feedList);
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                feedSpinner.setAdapter(spinnerArrayAdapter);

                Spinner spinner = (Spinner) findViewById(R.id.spnCategory);

                ArrayList<String> catSpinList = new ArrayList<String>();
                RealmResults<FeedData> catListResults = realm.distinct(FeedData.class, "category");

                for (FeedData catSpnData : catListResults) {
                    FeedData feedItems = realm.where(FeedData.class).equalTo(FeedData.CATEGORY, catSpnData.getCategory()).findFirst();
                    if (catSpnData.getCategory() != null) {
                        String catString = feedItems.getCategory();
                        catSpinList.add(catString);
                    }
                }


                ArrayAdapter<String> spinnerCatArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, catSpinList);
                spinnerCatArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(spinnerCatArrayAdapter);
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
            // Log.d("DownloadData", "The response code was " + response);
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



