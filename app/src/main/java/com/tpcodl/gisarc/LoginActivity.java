package com.tpcodl.gisarc;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.tpcodl.gisarc.utils.UtilsClass;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class LoginActivity extends AppCompatActivity {
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 101;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;
    private EditText edit_mobile,edit_password;
    private Button button_validate;
    private String username="";
    private String password="";
    private  ImageView view_password;
    private String dBDATE="";
    private String synckFlag="";
    private String currentDATE="";

    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private String result="";
    private String version="";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userverification);
        edit_mobile=findViewById(R.id.edit_mobile);
        edit_password=findViewById(R.id.edit_password);
        button_validate=findViewById(R.id.button_validate);


        SharedPreferences prefs = getSharedPreferences("GIS", MODE_PRIVATE);
        username = prefs.getString("username", "");//"No name defined" is the default value.
        password = prefs.getString("password", ""); //0 is the default value.

       // scheduleBackgrounService();




        if (username!=null && (!username.equalsIgnoreCase(""))){
            edit_mobile.setText(username);
        }

        if (password!=null && (!password.equalsIgnoreCase(""))){
            edit_password.setText(password);
        }


        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);

        checkPermissions();

        button_validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                if (UtilsClass.isConnected(LoginActivity.this)){
                    if (edit_mobile.getText().toString().trim().length()==0){
                        Toast.makeText(LoginActivity.this,"Please enter user ID",Toast.LENGTH_SHORT).show();
                    }
                    else if (edit_password.getText().toString().trim().length()==0){
                        Toast.makeText(LoginActivity.this,"Please enter password",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String url = "https://portal.tpcentralodisha.com:5059/sathi/SAYAKAPPSERVICE.asmx/TPCODL_AD_Authentication?strAccountId="+edit_mobile.getText().toString().trim().replace(" ","")+"&strPassword="+edit_password.getText().toString().trim()+"&Application_Name=GISAPP";
                        new callLoginApi().execute(url);
                    }
                }
                else {

                    Toast.makeText(LoginActivity.this,"No internet connection,please connect to internet and try again",Toast.LENGTH_SHORT).show();

                    //  Toast.makeText(LoginActivity.this,"No internet,please connect to intenet.",Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void callLoginApi() {
        Intent intent=new Intent(LoginActivity.this,HomeActivity.class);
        startActivity(intent);
        finish();
    }

    public void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        int REQUEST_CODE_FILE_UPLOAD = 5902;
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted

                        // finish();
                        if (permissionStatus.getBoolean(REQUIRED_SDK_PERMISSIONS[0], true)) {
                            //Previously Permission Request was cancelled with 'Dont Ask Again',
                            // Redirect to Settings after showing Information about why you need the permission
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            builder.setTitle("Need Storage Permission");
                            builder.setMessage("This app needs storage permission.");
                            builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    sentToSettings = true;
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                                    Toast.makeText(getBaseContext(), "Go to Permissions to Grant Storage", Toast.LENGTH_LONG).show();
                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            builder.show();
                        }
                        return;
                    }
                }
                SharedPreferences.Editor editor = permissionStatus.edit();
                editor.putBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, true);
                editor.commit();

                // pickPicture(REQUEST_CODE_FILE_UPLOAD);
                break;
        }
    }

    private class callLoginApi extends AsyncTask<String, Integer, String> {


        private ProgressDialog progressDialog;


        @Override
        protected String doInBackground(String... params) {
            //activity = (MainActivity)params[0];

            String strURL = params[0];
            URLConnection conn = null;
            InputStream inputStreamer = null;
            String bodycontent = null;
            Log.d("DemoApp", " strURL   " + strURL);

            try {


                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();

                URL url = null;
                url = new URL(strURL);
                URLConnection con = url.openConnection();
                con.setConnectTimeout(20000);//The timeout in mills
                con.setReadTimeout(20000);
                Document doc = db.parse(con.getInputStream());
                Element element = doc.getDocumentElement();
                element.normalize();

                result = doc.getFirstChild().getTextContent();

                System.out.println("sdfgt==" + result);



            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Toast.makeText(LoginActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();

                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }

            return bodycontent;
        }

        @Override

        protected void onPreExecute() {
            try {
                ConnectivityManager cm = (ConnectivityManager) LoginActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                      progressDialog = ProgressDialog.show(LoginActivity.this, "Authenticating Data", "Please Wait:: connecting to server");

                } else {
                    android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(LoginActivity.this);
                    alertDialogBuilder.setTitle("Enable Data");
                    alertDialogBuilder.setMessage("Enable Data & Retry")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    // create alert dialog
                    android.app.AlertDialog alertDialog = alertDialogBuilder.create();
                    // show it
                    alertDialog.show();
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }


        }

        @Override
        protected void onPostExecute(String str) {

            try {

                ConnectivityManager cm = (ConnectivityManager) LoginActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

                if (result.equalsIgnoreCase("1")){

                    SharedPreferences.Editor editor = getSharedPreferences("GIS", MODE_PRIVATE).edit();
                    editor.putString("username", edit_mobile.getText().toString().trim().toLowerCase().replace(" ",""));
                    editor.putString("password",edit_password.getText().toString().trim());
                    editor.apply();




                    Intent intent=new Intent(LoginActivity.this,HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                 Toast.makeText(LoginActivity.this,"Invalid credentials.",Toast.LENGTH_SHORT).show();
                }





            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Log.d("DemoApp", " str   " + str);
               progressDialog.dismiss();


            //fetChImage(empId);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        String url = "https://portal.tpcentralodisha.com:5059/sathi/SAYAKAPPSERVICE.asmx/MMG_APP_Version";

       //new  checkSyncData().execute(url);
    }

    private class checkSyncData extends AsyncTask<String, Integer, String> {


        @Override
        protected String doInBackground(String... params) {
            //activity = (MainActivity)params[0];

            String strURL = params[0];
            URLConnection conn = null;
            InputStream inputStreamer = null;
            String bodycontent = null;
            Log.d("DemoApp", " strURL   " + strURL);

            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();

                URL url = null;
                url = new URL(strURL);
                URLConnection con = url.openConnection();
                con.setConnectTimeout(20000);//The timeout in mills
                con.setReadTimeout(20000);
                Document doc = db.parse(con.getInputStream());
                Element element = doc.getDocumentElement();
                element.normalize();

                version = doc.getFirstChild().getTextContent();

                System.out.println("sdfgt==" + version);

                //  isSync = Boolean.parseBoolean(doc.getFirstChild().getTextContent());

                //  String[] res = doc.getFirstChild().getTextContent().split("[#]", 0);


            } catch (Exception e) {
                e.printStackTrace();
            }

            return bodycontent;
        }

        @Override

        protected void onPreExecute() {
            try {
                ConnectivityManager cm = (ConnectivityManager) LoginActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                    //  progressDialog = ProgressDialog.show(MainActivity.this, "Fetching Data", "Please Wait:: connecting to server");

                } else {
                    android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(LoginActivity.this);
                    alertDialogBuilder.setTitle("No internet!");
                    alertDialogBuilder.setMessage("Please connect to intenet.")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    //finish();
                                }
                            });
                           /* .setNegativeButton("Exit App", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });*/
                    // create alert dialog
                    android.app.AlertDialog alertDialog = alertDialogBuilder.create();
                    // show it
                    alertDialog.show();
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }


        }

        @Override
        protected void onPostExecute(String str) {

            try {

                ConnectivityManager cm = (ConnectivityManager) LoginActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();


                if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                    if ((version != null)&&(!version.equalsIgnoreCase(""))) {

                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            Log.d("DemoApp", " str   " + str);
            //   progressDialog.dismiss();


            //fetChImage(empId);
        }

    }





}
