package pt.apollotec.widoes;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import pt.apollotec.R;


public class PinActivity extends AppCompatActivity {
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedEntry.COLUMN_NAME_1 + " TEXT," +
                    FeedEntry.COLUMN_NAME_2 + " TEXT," +
                    FeedEntry.COLUMN_NAME_3 + " TEXT)";
    private static final String SQL_DELETE_ENTRIES =
            String.format("DROP TABLE IF EXISTS %s", FeedEntry.TABLE_NAME);
    String url;
    int counterSQLEnteries = 0;
    Button loadbutton;
    EditText EditTextPin, EditTextEmail;
    ImageView ImageViewLogo;
    String Pin;
    String Email;
    String ip = "78.46.117.137";

    public static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                StringBuilder h = new StringBuilder(Integer.toHexString(0xFF & aMessageDigest));
                while (h.length() < 2)
                    h.insert(0, "0");
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_pin);
        loadbutton = findViewById(R.id.loadbutton);
        EditTextPin = findViewById(R.id.editTextPin);
        EditTextEmail = findViewById(R.id.editTextLogin);
        ImageViewLogo = findViewById(R.id.imageViewLogo);

        EditTextEmail.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        loadbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("test");
                Pin = EditTextPin.getText().toString();
                Pin = md5(Pin);
                Email = EditTextEmail.getText().toString();
                String urluser = "http://" + ip + "/widoes/GetWebApp.php?email=" + Email + "&pin=" + Pin;
                downloadUrl(urluser);

            }
        });

        CheckPresentUrl();
        if (counterSQLEnteries > 0) {

            AbrirAgenda(url);
        }
    }

    void AbrirAgenda(String url) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("LinkAgenda", url);

        startActivity(intent);
        finish();
    }

    private void downloadUrl(final String urlWebService) {

        @SuppressLint("StaticFieldLeak")
        class DownloadUrl extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (!s.contains("erro")) {
                    AddSQLite(Email, Pin, s);
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
                        sb.append(json).append("\n");
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

    void CheckPresentUrl() {
        FeedReaderDbHelper dbHelper = new FeedReaderDbHelper(this);
        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor c = db.rawQuery("SELECT url FROM loadagenda ", null);

        if (c.moveToFirst()) {
            do {
                // Passing values
                url = c.getString(0);
                // Do something Here with values
                counterSQLEnteries++;
            } while (c.moveToNext());
        }
        c.close();
        db.close();

    }

    void AddSQLite(String email, String pin, String url) {
        FeedReaderDbHelper dbHelper = new FeedReaderDbHelper(this);
        // Gets the data repository in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_1, email);
        values.put(FeedEntry.COLUMN_NAME_2, pin);
        values.put(FeedEntry.COLUMN_NAME_3, url);


        // Insert the new row, returning the primary key value of the new row
        db.insert(FeedEntry.TABLE_NAME, null, values);
        db.close();
    }

    // SQL LITE
    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        static final String TABLE_NAME = "loadagenda";
        static final String COLUMN_NAME_1 = "email";
        static final String COLUMN_NAME_2 = "pin";
        static final String COLUMN_NAME_3 = "url";
    }

    public static class FeedReaderDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        static final int DATABASE_VERSION = 1;
        static final String DATABASE_NAME = "Widoes.db";

        FeedReaderDbHelper(Context context) {
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


}
