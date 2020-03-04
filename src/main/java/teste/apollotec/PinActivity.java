package teste.apollotec;

import androidx.appcompat.app.AppCompatActivity;

import android.content.*;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class PinActivity extends AppCompatActivity {
    String url;
    int counterSQLEnteries = 0;
    Button loadbutton;
    EditText EditTextPin, EditTextEmail;
    ImageView ImageViewLogo;
    String Pin;
    String Email;
    String ip = "192.168.1.133";

    String logostr;
    String primarycolorstr;
    String secundarycolorstr;
    String fontstr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        downloadCustomLogin("http://" + ip + "/aponta/GetWebAppLogin.php?email=ruben");
        updateCustomLogin();
        setContentView(R.layout.activity_pin);
        loadbutton = findViewById(R.id.loadbutton);
        EditTextPin = findViewById(R.id.editTextPin);
        EditTextEmail = findViewById(R.id.editTextEmail);
        ImageViewLogo = findViewById(R.id.imageViewLogo);

        EditTextEmail.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        loadbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("test");
                Pin = EditTextPin.getText().toString();
                Email = EditTextEmail.getText().toString();
                String urluser = "http://" + ip + "/aponta/GetWebApp.php?email="+Email+"&pin="+Pin;
                downloadUrl(urluser);

            }
        });

        CheckPresentUrl();
        if (counterSQLEnteries > 0){

            AbrirAgenda(url);
        }
    }
    void AbrirAgenda(String url){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("LinkAgenda", url);

        startActivity(intent);
        finish();
    }
    private void downloadUrl(final String urlWebService) {

        class DownloadUrl extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (!s.contains("erro")){
                    AddSQLite(Email,Pin,s);
                    AbrirAgenda(s);
                }

            }

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(urlWebService);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }
            }
        }
        DownloadUrl getJSON = new DownloadUrl();
        getJSON.execute();
    }


    private void downloadCustomLogin(final String urlWebService) {

        class DownloadCustomLogin extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                try {
                    JSONObject jObject = new JSONObject(s);
                    logostr = jObject.getString("logo");
                    primarycolorstr = jObject.getString("primarycolor");
                    secundarycolorstr = jObject.getString("secundarycolor");
                    fontstr = jObject.getString("font");
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(urlWebService);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }
            }
        }
        DownloadCustomLogin getJSON = new DownloadCustomLogin();
        getJSON.execute();
    }
    // SQL LITE
    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "loadagenda";
        public static final String COLUMN_NAME_1 = "email";
        public static final String COLUMN_NAME_2 = "pin";
        public static final String COLUMN_NAME_3 = "url";
    }
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedEntry.COLUMN_NAME_1 + " TEXT," +
                    FeedEntry.COLUMN_NAME_2 + " TEXT," +
                    FeedEntry.COLUMN_NAME_3 + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;


    public class FeedReaderDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "FeedReader.db";

        public FeedReaderDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    void CheckPresentUrl(){
        FeedReaderDbHelper dbHelper = new FeedReaderDbHelper(this);
        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor c = db.rawQuery("SELECT url FROM loadagenda ", null);

        if (c.moveToFirst()){
            do {
                // Passing values
                 url = c.getString(0);
                // Do something Here with values
                counterSQLEnteries++;
            } while(c.moveToNext());
        }
        c.close();
        db.close();

    }
    void AddSQLite(String email, String pin,String url){
        FeedReaderDbHelper dbHelper = new FeedReaderDbHelper(this);
        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_1,email);
        values.put(FeedEntry.COLUMN_NAME_2,pin);
        values.put(FeedEntry.COLUMN_NAME_3,url);


        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(FeedEntry.TABLE_NAME, null, values);
        db.close();
    }
    void updateCustomLogin()
    {
        //ImageViewLogo.setImageURI(Uri.parse());
        String test = "android.resource://"+R.class.getPackage().getName()+"/drawable/"+logostr;
        Toast.makeText(this,test, Toast.LENGTH_LONG).show();

    }


}
