package com.noyss.blockcall;

import static android.telecom.TelecomManager.ACTION_CHANGE_DEFAULT_DIALER;
import static android.telecom.TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.BlockedNumberContract;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telecom.TelecomManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.noyss.blockcall.util.StringUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText mEditTextNumber = null;
    private ListView historyCallList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        historyCallList = (ListView) findViewById(R.id.his_call_list);
        mEditTextNumber = (EditText) findViewById(R.id.editTextNumber);
        checkPermission();
        chnagedialer();
        getCallDetails();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Toast.makeText(this, "onStart", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Toast.makeText(this, "onStart", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        Toast.makeText(this, "onRestart", Toast.LENGTH_SHORT).show();
        getCallDetails();

    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
//    }

    private void chnagedialer() {
        TelecomManager systemService = this.getSystemService(TelecomManager.class);
        if (systemService != null && !systemService.getDefaultDialerPackage().equals("com.android.contacts")) {
            startActivity((new Intent(ACTION_CHANGE_DEFAULT_DIALER)).putExtra(EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, "com.android.contacts"));
        }
    }

    public void onClickBlock(View view) {
        String number = mEditTextNumber.getText().toString();
        if (number != null && number.length() > 0) {
            blockNumber(number);
        }
    }

    public void onClickUnBlock(View view) {
        String number = mEditTextNumber.getText().toString();
        if (number != null && number.length() > 0) {
            unBlockNumber(number);
        }
    }

    public void onClickIsBlocked(View view) {
        String number = mEditTextNumber.getText().toString();
        if (number != null && number.length() > 0) {
            isBlocked(number);
        }
    }

    private void blockNumber(String number) {
        try {
            if (BlockedNumberContract.canCurrentUserBlockNumbers(this)) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number);
                Uri uri = getContentResolver().insert(BlockedNumberContract.BlockedNumbers.CONTENT_URI, contentValues);
                Toast.makeText(MainActivity.this, number + " : Blocked", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            System.out.println(e);
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void unBlockNumber(String number) {
        try {
            if (BlockedNumberContract.canCurrentUserBlockNumbers(this)) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number);
                Uri uri = getContentResolver().insert(BlockedNumberContract.BlockedNumbers.CONTENT_URI, contentValues);
                getContentResolver().delete(uri, null, null);
                Toast.makeText(MainActivity.this, number + " : UnBlocked", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            System.out.println(e);
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void isBlocked(String number) {
        try {
            if (BlockedNumberContract.canCurrentUserBlockNumbers(this)) {
                boolean blocked = BlockedNumberContract.isBlocked(this, number);
                Toast.makeText(MainActivity.this, number + " Blocked:" + blocked, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "User cannot perform  this operation", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            System.out.println(e);
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();

        }
    }

    private void getBlockedList() {
        try {
            Cursor cursor = getContentResolver().query(BlockedNumberContract.BlockedNumbers.CONTENT_URI,
                    new String[]{BlockedNumberContract.BlockedNumbers.COLUMN_ID,
                            BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER,
                            BlockedNumberContract.BlockedNumbers.COLUMN_E164_NUMBER},
                    null, null, null);
        } catch (Exception e) {
            System.out.println(e);
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void getCallDetails() {

        ArrayAdapter<String> arrayAdapter = null;
        List<String> hisList = new ArrayList<>();
        String sb = "";
        Calendar c = Calendar.getInstance(new Locale("TH"));
        Cursor managedCursor = managedQuery(CallLog.Calls.CONTENT_URI, null,
                null, null, null);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        int cachedName = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
        int countryISO = managedCursor.getColumnIndex(CallLog.Calls.COUNTRY_ISO);

        while (managedCursor.moveToNext()) {
            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String callDate = managedCursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            String callDuration = managedCursor.getString(duration);
            String callCachedName = managedCursor.getString(cachedName);
            String callCountryISO = managedCursor.getString(countryISO);
            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
//                    dir = "OUTGOING";
                    dir = "โทรออก";
                    break;

                case CallLog.Calls.INCOMING_TYPE:
//                    dir = "INCOMING";
                    dir = "โทรเข้า";
                    break;

                case CallLog.Calls.MISSED_TYPE:
//                    dir = "MISSED";
                    dir = "ไม่ได้รับ";
                case CallLog.Calls.REJECTED_TYPE:
//                    dir = "REJECTED";
                    dir = "ไม่รับ";
                    break;
                case CallLog.Calls.VOICEMAIL_TYPE:
                    dir = "VOICEMAIL";
                    break;
                case CallLog.Calls.BLOCKED_TYPE:
                    dir = "BLOCKED";
                    break;
                default:
                    dir = dircode+"";
            }
            if (callCachedName == null || "".equals(callCachedName)) {
//                callCachedName = "Unknown";
                callCachedName = "ไม่รู้จัก";
            }
            if (phNumber.length() == 10) {
                phNumber = phNumber.substring(0, 3) + "XXXXX" + phNumber.substring(8, 10);
            }

            c.setTime(callDayTime);
            String dateTime = StringUtil.digitTwo(c.get(Calendar.DATE)) + "/" + StringUtil.digitTwo(c.get(Calendar.MONTH)) + "/" + c.get(Calendar.YEAR) + " " + StringUtil.digitTwo(c.get(Calendar.HOUR)) + ":" + StringUtil.digitTwo(c.get(Calendar.MINUTE));
            sb = callCachedName + ": " + phNumber + " (" + dir + ") \nเวลา: " + dateTime + " \nเวลาสนทนา: " + callDuration + " วินาที";
            hisList.add(sb);
        }

        managedCursor.close();
        String[] hisArr = hisList.toArray(new String[0]);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, hisArr);
        historyCallList.setAdapter(arrayAdapter);
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED
            ) {

                System.out.println("===================================> 00");
            } else {
                System.out.println("===================================> 01");
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.WRITE_CALL_LOG,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS,
                }, 1);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED
                && grantResults[3] == PackageManager.PERMISSION_GRANTED
                && grantResults[4] == PackageManager.PERMISSION_GRANTED
                && grantResults[5] == PackageManager.PERMISSION_GRANTED
                && grantResults[6] == PackageManager.PERMISSION_GRANTED
        ) {
            System.out.println("===================================> onRequestPermissionsResult: OK");
        } else {
            System.out.println("===================================> 22");
            checkPermission();
        }
    }
}