package com.tpcodl.gisarc.database;
///QA CODE

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;


import com.tpcodl.gisarc.utils.DialogUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static String DB_NAME = "SMRD.db";
    private static String DB_PATH = "";
    private static String DB_PATH1 = "";
    public static String strInputFilePath = Environment.getExternalStorageDirectory() + "/cesuapp/input/";
    private static final int DB_VERSION = 9;
    private static SQLiteDatabase db1;
    private SQLiteDatabase mDataBase;
    private final Context mContext;
    //  private static Context mContext1 = null;
    private DialogUtils dUtils;
    int Headercount = 0;
    int ChildCount = 0;
    int HeaderChkCnt = 0;
    int ChildChkCnt = 0;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        if (android.os.Build.VERSION.SDK_INT >= 17) {
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        } else {
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        }
        this.mContext = context;
    }


    public void createDataBase() throws IOException {
        //If the database does not exist, copy it from the assets.
        boolean mDataBaseExist = checkDataBase();
        if (!mDataBaseExist) {
            this.getReadableDatabase();
            this.close();
            try {
                //Copy the database from assests
                copyDataBase();
                //Log.e(TAG, "createDatabase database created");
            } catch (IOException mIOException) {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    public void open() {
        this.mDataBase = this.getWritableDatabase();
    }

    /**
     * Close the database connection.
     */
    public boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        //Log.e("dbFile", dbFile + "   " + dbFile.exists());
        return dbFile.exists();
    }

    //Copy the database from assets
    private void copyDataBase() throws IOException {
        InputStream mInput = mContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream mOutput = new FileOutputStream(outFileName);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0) {
            mOutput.write(mBuffer, 0, mLength);
        }
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }

    public void updateMasterUser(String syncDate, String userId) {
        String printerName = "";
        SQLiteDatabase db = this.getReadableDatabase();
        String strSelectSQL_01 = "UPDATE Mst_User SET SyncDate='" + syncDate + "'  WHERE UserId='" + userId + "'  ";
        db.execSQL(strSelectSQL_01);
        db.close();
        Log.d("DemoApp", "strSelectSQL_01" + strSelectSQL_01);
   /*     SQLiteDatabase db = this.getReadableDatabase();
      //  db.execSQL(strSelectSQL_01);
        ContentValues cv = new ContentValues();
        cv.put("SyncDate",syncDate); //These Fields should be your String values of actual column names

        db.update("Mst_User", cv, "UserId = ?", new String[]{userId});
        db.close();*/

    }

    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }
    public void deleteDataHeader(String installationNo) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + "TBL_SPOTBILL_HEADER_DETAILS" + " WHERE " + "INSTALLATION" + "='" + installationNo + "'");
        db.close();
    }
    public void insertIntoTEMPTARIFF(String strSQL) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(strSQL);
    }

    public void updateFileDesc(String strSQL) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(strSQL);
    }

    public void deleteMSTUSER() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM Mst_User");
        db.close();
    }

    public void deleteDataChild(String installationNo) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + "TBL_SPOTBILL_CHILD_DETAILS" + " WHERE " + "INSTALLATION" + "='" + installationNo + "'");
        db.close();
    }


    public void deleteDataHeaderInstallationWise(String USER_TYPE) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM TBL_SPOTBILL_HEADER_DETAILS where USER_TYPE='X'");
        db.close();
    }


    public void truncateTariffHeader() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM TBL_SPOTBILL_TARIFF_HEADER");
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            try {
                String query = "ALTER TABLE TBL_SPOTBILL_CHILD_DETAILS  ADD COLUMN READ_FLAG";
                db.execSQL(query);
            } catch (Exception e) {

            }

        }

    }
}





