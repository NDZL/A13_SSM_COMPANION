package com.ndzl.sst_companionapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    TextView resultView;
    LocalContentObserver myContentObserver;
    LocalDataSetObserver myDataSetObserver;
    Uri cpUri;
    private final String AUTHORITY = "content://com.zebra.securestoragemanager.securecontentprovider/data";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultView = findViewById(R.id.result);
        resultView.setText("Scoped Storage Companion App\n"+getAndroidAPI()+"\n"+getTargetSDK()+"\nisExternalStorageManager:"+ Environment.isExternalStorageManager());

        //REGISTER FILE NOTIFICATION RECEIVER
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.zebra.configFile.action.notify");
        filter.addCategory("android.intent.category.DEFAULT");
        registerReceiver(new FileNotificationReceiver(), filter);

        //OBSERVER FOR DATA SHARING VIA SSM FROM DIFFERENT APP
        cpUri = Uri.parse(AUTHORITY);
        myContentObserver = new LocalContentObserver(null);
        myDataSetObserver = new LocalDataSetObserver();
    }


    @Override
    protected void onResume() {
        super.onResume();
        getContentResolver().registerContentObserver(cpUri, true, myContentObserver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(myContentObserver);
    }

    public void onClickManageExternalStorage(View view){
        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }


    public void onClickReadTest(View view){
        String personalLines = "";
        String sdcardLines = "";
        String downloadLines="";
        String enterpriseLines="";
        String androidDataAppLines="";
        String emulatedLines="";
        String docsLines="";
        BufferedReader br;


        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream("/sdcard/sdc.txt"),"utf-8"));
            sdcardLines = ""+br.readLine().length();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            sdcardLines = e.getMessage();
        }

        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream("/sdcard/personal/mars.txt"),"utf-8"));
            personalLines = ""+br.readLine().length();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            personalLines = e.getMessage();
        }

        try{
            br = new BufferedReader(new InputStreamReader(new FileInputStream("/sdcard/Download/moon.xml"),"utf-8"));
            downloadLines = ""+br.readLine().length();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            downloadLines= e.getMessage();
        }

        try{
            br = new BufferedReader(new InputStreamReader(new FileInputStream("/storage/emulated/0/Download/nesd.txt"),"utf-8"));
            emulatedLines = ""+br.readLine().length();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            emulatedLines= e.getMessage();
        }

        try{
            br = new BufferedReader(new InputStreamReader(new FileInputStream("/sdcard/Documents/doc.txt"),"utf-8"));
            docsLines = ""+br.readLine().length();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            docsLines= e.getMessage();
        }

        try{
            br = new BufferedReader(new InputStreamReader(new FileInputStream("/sdcard/Android/data/com.zebra.ssmdatapersist/app.xml"),"utf-8"));
            androidDataAppLines = ""+br.readLine().length();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            androidDataAppLines= e.getMessage();
        }

        try{
            br = new BufferedReader(new InputStreamReader(new FileInputStream("/enterprise/usr/persist/enterprise.txt"),"utf-8"));
            enterpriseLines = ""+br.readLine().length();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            enterpriseLines= e.getMessage();
        }

        ///////////////////////////////////////////////////////////

        StringBuilder sb = new StringBuilder();
        sb.append("1st line len in /sdcard/sdc.txt:"+sdcardLines);
        sb.append("\n1st line len in /sdcard/personal/mars.txt:"+personalLines);
        sb.append("\n1st line len in /sdcard/Download/moon.xml:"+downloadLines);
        sb.append("\n1st line len in /storage/emulated/0/Download/nesd.txt:"+emulatedLines);
        sb.append("\n1st line len in /sdcarmarsd/Documents/doc.txt:"+docsLines);
        sb.append("\n1st line len in /sdcard/Android/data/com.zebra.ssmdatapersist/app.xml:"+androidDataAppLines);
        sb.append("\n1st line len in /enterprise/usr/persist/enterprise.txt:"+enterpriseLines);


        resultView.setText(sb.toString());
    }

    public void onClickSSMDataQuery(View view){

        StringBuilder sb = new StringBuilder();
        sb.append( ssmQueryData() );

        resultView.setText(sb.toString());
    }

    String ssmQueryData(){
        //TO GET DATA SHARED BY OTHER APP
        return "hi!\nI can see "+ssm_notpersisted_countRecords()+" not-persisted recs in your SSM\nAnd " + ssm_ispersisted_countRecords()+" persisted recs";
    }

    int ssm_notpersisted_countRecords() {
        Uri cpUriQuery = Uri.parse(AUTHORITY + "/[" + getPackageName() + "]");
        String selection = COLUMN_TARGET_APP_PACKAGE + " = '" + getPackageName() + "'" + " AND " + COLUMN_DATA_PERSIST_REQUIRED + " = 'false'" + " AND " + COLUMN_DATA_TYPE + " = '" + "1" + "'";

        int _count=0;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(cpUriQuery, null, selection, null, null);
            _count = cursor.getCount();
        } catch (Exception e) {
            Log.d(TAG, "Error: " + e.getMessage());
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return _count;
    }

    int ssm_ispersisted_countRecords() {
        Uri cpUriQuery = Uri.parse(AUTHORITY + "/[" + getPackageName() + "]");
        String selection = COLUMN_TARGET_APP_PACKAGE + " = '" + getPackageName() + "'" + " AND " + COLUMN_DATA_PERSIST_REQUIRED + " = 'true'" + " AND " + COLUMN_DATA_TYPE + " = '" + "1" + "'";

        int _count=0;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(cpUriQuery, null, selection, null, null);
            cursor.registerDataSetObserver(myDataSetObserver);
            _count = cursor.getCount();
        } catch (Exception e) {
            Log.d(TAG, "Error: " + e.getMessage());
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        cursor.unregisterDataSetObserver(myDataSetObserver);
        return _count;
    }

    public void onClickSSMReadFile(View view){

        StringBuilder sb = new StringBuilder();
        sb.append(ssmQueryFile());
/*        try {
            sb.append( readFile(this, "content://com.zebra.securestoragemanager.SecureFileProvider/data/com.zebra.ssmfilepersist/0/A.txt") ) ;
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        resultView.setText(sb.toString());
    }

    private final String AUTHORITY_FILE = "content://com.zebra.securestoragemanager.securecontentprovider/files/";
    private final String RETRIEVE_AUTHORITY = "content://com.zebra.securestoragemanager.securecontentprovider/file/*";
    private final String COLUMN_DATA_NAME = "data_name";
    private final String COLUMN_DATA_VALUE = "data_value";
    private final String COLUMN_DATA_TYPE = "data_type";
    private final String COLUMN_DATA_PERSIST_REQUIRED = "data_persist_required";
    private final String COLUMN_TARGET_APP_PACKAGE = "target_app_package";
    private static final String TAG = "SSMCompanionApp : ";

    private final String signature = "";
    String ssmQueryFile() {
        Uri uriFile = Uri.parse(RETRIEVE_AUTHORITY);
        String selection = "target_app_package='com.ndzl.sst_companionapp'"; //GETS *ALL FILES* FOR THE PACKAGE NO PERSISTANCE FILTER
        //String selection = "target_app_package='com.ndzl.sst_companionapp'AND data_persist_required='false'"; //GETS *ALL FILES* FOR THE PACKAGE
        //-not-working-//String selection = "target_path='com.ndzl.sst_companionapp/A.txt'"; //POINTS TO A SPECIFIC FILE IN THE PACKAGE
        Log.i(TAG, "File selection " + selection);
        Log.i(TAG, "File cpUriQuery " + uriFile.toString());

        String res = "N/A";
        Cursor cursor = null;
        try {
            Log.i(TAG, "Before calling query API Time");
            cursor = getContentResolver().query(uriFile, null, selection, null, null);
            Log.i(TAG, "After query API called TIme");
        } catch (Exception e) {
            Log.d(TAG, "Error: " + e.getMessage());
        }
        try {
            if (cursor != null && cursor.moveToFirst()) {
                StringBuilder strBuild = new StringBuilder();
                String uriString;
                strBuild.append("FILES FOUND: "+cursor.getCount()+"\n");
                while (!cursor.isAfterLast()) {


                    /*
                    //for debug purpose: listing cursor's columns
                    for (int i = 0; i<cursor.getColumnCount(); i++) {
                        Log.d(TAG, "column " + i + "=" + cursor.getColumnName(i));
                    }
                    //RESULT: THE COLUMN NAMES USED BELOW
                    */


                    uriString = cursor.getString(cursor.getColumnIndex("secure_file_uri"));
                    String fileName = cursor.getString(cursor.getColumnIndex("secure_file_name"));
                    String isDir = cursor.getString(cursor.getColumnIndex("secure_is_dir"));
                    String crc = cursor.getString(cursor.getColumnIndex("secure_file_crc"));
                    strBuild.append("\n");
                    strBuild.append("URI - " + uriString).append("\n").append("FileName - " + fileName).append("\n").append("IS Directory - " + isDir)
                            .append("\n").append("CRC - " + crc).append("\n").append("FileContent - ").append(readFile(this, uriString));
                    Log.i(TAG, "File cursor " + strBuild);
                    strBuild.append("\n ----------------------").append("\n");

                    cursor.moveToNext();
                }
                Log.d(TAG, "Query File: " + strBuild);
                Log.d("Client - Query", "Set test to view =  " + System.currentTimeMillis());
                res =strBuild.toString();
            } else {
                res="No files to query for local package "+getPackageName();
            }
        } catch (Exception e) {
            Log.d(TAG, "Files query data error: " + e.getMessage());
            res="EXCP-"+e.getMessage();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return res;
    }

    void insertFile() {
        //String sourcePath = "/sdcard/Download/A.txt";  //GOOD FOR SD660 CHIPSET
        String sourcePath = "/sdcard/Android/data/A.txt";  //GOOD FOR 6490 CHIPSET TC58
        String targetPath = "com.ndzl.sst_companionapp/A.txt";
        Log.i(TAG, "targetPath " + targetPath);
        Log.i(TAG, "sourcePath " + sourcePath);
        Log.i(TAG, "*********************************");
        File file = new File(sourcePath);
        Log.i(TAG, "file path " + file.getPath() + " length: " + file.length());

        StringBuilder _sb = new StringBuilder();
        if (!file.exists()) {
            Toast.makeText(this, "File does not exists in the source", Toast.LENGTH_SHORT).show();
        } else {
            Uri cpUriQuery = Uri.parse(AUTHORITY_FILE + getPackageName());
            Log.i(TAG, "authority  " + cpUriQuery.toString());

            try {
                ContentValues values = new ContentValues();
                values.put(COLUMN_TARGET_APP_PACKAGE, String.format("{\"pkgs_sigs\": [{\"pkg\":\"%s\",\"sig\":\"%s\"}]}", getPackageName(), signature));
                values.put(COLUMN_DATA_NAME, sourcePath);
                values.put(COLUMN_DATA_TYPE, "3");
                values.put(COLUMN_DATA_VALUE, targetPath);
                values.put(COLUMN_DATA_PERSIST_REQUIRED, "false");

                Uri createdRow = getContentResolver().insert(cpUriQuery, values);
                Log.i(TAG, "SSM Insert File: " + createdRow.toString());
                //Toast.makeText(this, "File insert success", Toast.LENGTH_SHORT).show();
                _sb.append("Insert Result: "+createdRow+"\n" );
            } catch (Exception e) {
                Log.e(TAG, "SSM Insert File - error: " + e.getMessage() + "\n\n");
                _sb.append("SSM Insert File - error: " + e.getMessage() + "\n\n");
            }
            resultView.setText(_sb);
            Log.i(TAG, "*********************************");
        }
    }



    public void onClickDeleteFile(View view) {

        //THE FILE YOU WANT TO DELETE MUST BE OVERWRITTEN LOCALLY WITH THE CURRENT PACKAGENAME
        //BEFORE ACTUAL DELETION. IT COULD ALSO BE AN EMPTY FILE, WITH THE SAME FIELNAME.
        insertFile();

        //DELETE WORKS ONLY LOCALLY
        StringBuilder _sb = new StringBuilder();
        try {
            _sb.append("DELETING FOR TARGET PACKAGE com.ndzl.sst_companionapp\n");
            String whereClauseALL = "target_app_package='com.ndzl.sst_companionapp'";
            String whereClauseFALSE = "target_app_package='com.ndzl.sst_companionapp' AND data_persist_required='false'";
            String whereClauseTRUE = "target_app_package='com.ndzl.sst_companionapp' AND data_persist_required='true'";

            Uri cpUriQuery = Uri.parse(AUTHORITY_FILE +getPackageName());
            int deleteStatusALL = getContentResolver().delete(cpUriQuery, whereClauseALL, null);// 0 means success
            //int deleteStatusTRUE = getContentResolver().delete(cpUriQuery, whereClauseTRUE, null);// 0 means success

            Log.d(TAG, "File deleted, status = " + deleteStatusALL);
            _sb.append("Target File delete result="+deleteStatusALL/*+"+"+deleteStatusTRUE*/);

        } catch (Exception e) {
            Log.d(TAG, "Delete file - error: " + e.getMessage());
            _sb.append("EXCEPTION in delete result="+e.getMessage());
        }
        resultView.setText( _sb.toString());
    }

    String getTargetSDK(){
        int version = 0;
        String app_username="";
        PackageManager pm = getPackageManager();
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = pm.getApplicationInfo(getPackageName() , 0);
        } catch (PackageManager.NameNotFoundException e) {}
        if (applicationInfo != null) {
            version = applicationInfo.targetSdkVersion;
            app_username = AndroidFileSysInfo.getNameForId( applicationInfo.uid );
        }
        return  "APP_TARGET_API:"+version+" APP_USER:"+app_username;
    }


    private String readFile(Context context, String uriString) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(Uri.parse(uriString));
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
        Log.d(TAG, "full content = " + sb);
        return sb.toString();
    }

    String getAndroidAPI(){
        String _sb_who =  Build.MANUFACTURER+","+ Build.MODEL+"\n"+ Build.DISPLAY+", API:"+ android.os.Build.VERSION.SDK_INT;
        return  _sb_who;
    }

}
class LocalContentObserver extends ContentObserver {
    public LocalContentObserver(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange) {
        this.onChange(selfChange, null);
        Log.d("LocalContentObserver", "### received self change notification from uri: ");
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        Log.d("LocalContentObserver", "### received notification from uri: " + uri.toString());
    }
}

class LocalDataSetObserver extends DataSetObserver {
    public LocalDataSetObserver() {

    }

    @Override
    public void onInvalidated() {
        super.onInvalidated();
        Log.d("LocalDataSetObserver", "onInvalidate");
    }

    @Override
    public void onChanged() {
        super.onChanged();
        Log.d("LocalDataSetObserver", "onChanged");
    }
}