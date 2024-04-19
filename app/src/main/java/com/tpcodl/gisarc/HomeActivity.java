package com.tpcodl.gisarc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.tpcodl.gisarc.utils.AppUtils;
import com.tpcodl.gisarc.utils.UtilsClass;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    private ConstraintLayout btn_scan;
    private ConstraintLayout cl_capture;
    private Button btn_submit;
    public static int SCAN_ACTIVITY=101;
    String requiredValue="";
    private TextView scan_title,scan_data;
    private Uri picUri;
    private String filePath="";
    ImageView iv_upload,iv_image_cap;
    boolean doubleBackToExitPressedOnce = false;

    public final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";
    public final String METHOD_NAME = "GIS_Images";
    public final String SOAP_ACTION = "http://tempuri.org/GIS_Images";
    public final String SOAP_ADDRESS = "https://portal.tpcentralodisha.com:5059/sathi/SAYAKAPPSERVICE.asmx?WSDL";
    private String base64Image="";
    private String responseSubmit="";
    private ProgressDialog progressDialog;
    private String username="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        initView();

    }

    private void initView() {
        btn_scan=findViewById(R.id.btn_scan);
        cl_capture=findViewById(R.id.cl_capture);
        btn_submit=findViewById(R.id.btn_submit);
        scan_data=findViewById(R.id.scan_data);
        scan_title=findViewById(R.id.scan_title);
        iv_image_cap=findViewById(R.id.iv_image_cap);
        iv_upload=findViewById(R.id.iv_upload);
        scan_title.setVisibility(View.GONE);
        scan_data.setVisibility(View.GONE);

        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(HomeActivity.this,ScanActivity.class);
                startActivityForResult(intent,SCAN_ACTIVITY);

            }
        });
        cl_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();

            }
        });
        SharedPreferences prefs = getSharedPreferences("GIS", MODE_PRIVATE);
        username = prefs.getString("username", "");//"No name defined" is the default value.








        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (UtilsClass.isConnected(HomeActivity.this)){
                      if (requiredValue.toString().trim().length()==0){
                        Toast.makeText(HomeActivity.this,"Please scan data",Toast.LENGTH_SHORT).show();
                    }
                    else if (filePath.toString().trim().length()==0){
                        Toast.makeText(HomeActivity.this,"Please capture image.",Toast.LENGTH_SHORT).show();

                    }
                    else {
                        submitData();
                      }
                }

                else {
                     Toast.makeText(HomeActivity.this,"No intenet,please connect to internet.",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void submitData() {
          new TestAsynk().execute();
    }

    private void captureImage() {
        ImagePicker.Companion.with(this)
                // .crop()
              //  .cameraOnly()//Crop image(Optional), Check Customization for more option
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == SCAN_ACTIVITY  && resultCode  == RESULT_OK) {
                requiredValue = data.getStringExtra("returnResult");
                scan_title.setVisibility(View.VISIBLE);
                scan_data.setVisibility(View.VISIBLE);
                scan_data.setText(requiredValue);
                Toast.makeText(HomeActivity.this, requiredValue.toString(),
                        Toast.LENGTH_SHORT).show();
            }

           else if (resultCode == Activity.RESULT_OK) {
                picUri = data != null ? data.getData() : null;

                try {
                    iv_image_cap.setImageURI(picUri);
                    iv_upload.setVisibility(View.GONE);

                }catch (Exception ex){
                    ex.printStackTrace();
                }

                filePath = picUri.getPath();
                base64Image= AppUtils.readFileAsBase64String(filePath);

                System.out.println("Asdf=="+base64Image);

                try {
                    iv_image_cap.setImageURI(picUri);
                    iv_upload.setVisibility(View.GONE);

                }catch (Exception ex){
                    ex.printStackTrace();
                }



            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                //Toast.makeText(this, ImagePicker.RESULT_ERROR, Toast.LENGTH_SHORT).show();
            } else {
                //   Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            Toast.makeText(HomeActivity.this, ex.toString(),
                    Toast.LENGTH_SHORT).show();
        }

    }




    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }


    private class TestAsynk extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... params) {

            SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE,
                    METHOD_NAME);
            request.addProperty("p_checksum", "01091981".toString().trim());

            if (!(username!=null && (!username.equalsIgnoreCase("")))){
                username="";
            }

            SimpleDateFormat sdf = new SimpleDateFormat("HHmmss", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());

            request.addProperty("p_ImageName", username+"_"+requiredValue+"_"+currentDateandTime+".jpg");
            request.addProperty("p_image", base64Image);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;

            envelope.setOutputSoapObject(request);


            System.out.println("Sdfgtyhu=="+username+"_"+requiredValue+"_"+currentDateandTime+".jpg");

            HttpTransportSE androidHttpTransport = new HttpTransportSE(
                    SOAP_ADDRESS);
           /* try {
                androidHttpTransport.call(SOAP_ACTION,envelope);
            }
            catch (IOException ex){
                ex.printStackTrace();
            }
            catch (XmlPullParserException ex){
                ex.printStackTrace();
            }*/

            SoapObject response = null;
            try {

                androidHttpTransport.call(SOAP_ACTION, envelope);

                if (envelope.bodyIn instanceof SoapFault)
                {
                    final SoapFault sf = (SoapFault) envelope.bodyIn;
                    responseSubmit = sf.toString();

                }
                else {
                    response = (SoapObject) envelope.bodyIn;
                    responseSubmit = response.getProperty("GIS_ImagesResult").toString();

                }



                //Log.e("Object response", response.toString());

                System.out.println("sdfgh=="+responseSubmit);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return responseSubmit;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ConnectivityManager cm = (ConnectivityManager)HomeActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if(activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                progressDialog = ProgressDialog.show(HomeActivity.this, "Submitting Data", "Please Wait:: connecting to server");

            }else{
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(HomeActivity.this);
                alertDialogBuilder.setTitle("Enable Data");
                alertDialogBuilder.setMessage("Enable Data & Retry")
                        .setCancelable(false)
                        .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
            }
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                progressDialog.dismiss();


                if (result.equalsIgnoreCase("1")){

                    iv_image_cap.setImageBitmap(null);
                    //  iv_image_cap.setImageResource(android.R.color.transparent);
                    iv_image_cap.setBackgroundResource(R.drawable.dotted_rectangle);
                    iv_upload.setVisibility(View.VISIBLE);

                    scan_title.setVisibility(View.GONE);
                    scan_data.setVisibility(View.GONE);
                    filePath="";
                    requiredValue="";

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(HomeActivity.this);
                    alertDialogBuilder.setTitle("Successful!");
                    alertDialogBuilder.setMessage("Your document is uploaded successfully.")

                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    dialog.cancel();
                                }
                            });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    // show it
                    alertDialog.show();
                }
                else{
                    Toast.makeText(HomeActivity.this,"Something went wrong.",Toast.LENGTH_SHORT).show();
                }

            }catch (Exception ex){
                ex.printStackTrace();
            }



        }

    }

}

