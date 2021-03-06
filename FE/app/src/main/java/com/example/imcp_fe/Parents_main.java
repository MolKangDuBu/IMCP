package com.example.imcp_fe;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.example.imcp_fe.Child;
import com.example.imcp_fe.GPS.GPStracker;
import com.example.imcp_fe.GPS.Restartservice;
import com.example.imcp_fe.Network.AppHelper;
import com.example.imcp_fe.Adapter.rv_mychildren_adapter;
import com.example.imcp_fe.Data.rv_mychildren_data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.graphics.Bitmap;
import android.widget.Toast;

/*
 * 부모 메인 화면
 * */
public class Parents_main extends AppCompatActivity {

    private Intent foregroundIntent;

    //리사이클러뷰
    private RecyclerView rv_mychildren;
    private LinearLayoutManager layoutManager = null;
    private rv_mychildren_adapter rvMychildrenAdapter = null;
    private ArrayList<rv_mychildren_data> arrayList = new ArrayList<rv_mychildren_data>();
    private rv_mychildren_data rvMychildrenData;

    //버튼
    private Button btn_main_addchild;
    private Button btn_main_missingclist;
    private ImageButton iv_mypage;

    private Intent intent;

    public Bitmap test = null;

    private SharedPreferences login_preference;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;

    //퍼미션
    private String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION};
    //서버 url
    private String url = "http://tomcat.comstering.synology.me/IMCP_Server/getChildList.jsp";

    private long backKeyPressedTime = 0;
    private Toast toast;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //인스턴스 저장
        btn_main_missingclist = findViewById(R.id.btn_main_missinglist);
        btn_main_addchild = findViewById(R.id.btn_main_addchild);
        iv_mypage = findViewById(R.id.iv_mypage);
        rv_mychildren = findViewById(R.id.rv_mychildren);

        test = BitmapFactory.decodeResource(getResources(), R.drawable.children);
        login_preference = getSharedPreferences("Login", MODE_PRIVATE);

        //아이 추가 엑티비티로 전환
        btn_main_addchild.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(view.getContext(), Add_child_check.class);
                startActivity(intent);
            }
        });
        // 마이페이지 엑티비티로 전환
        iv_mypage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(view.getContext(), Parent_info.class);
                startActivity(intent);
            }
        });
        //실종 아이 엑티비티로 전환
        btn_main_missingclist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(view.getContext(), Missing_children.class);
                startActivity(intent);
            }
        });

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            checkRunTimePermission();
        }


        //서비스 상태 확인
        if (GPStracker.serviceIntent == null) {
            foregroundIntent = new Intent(this, GPStracker.class);
            startService(foregroundIntent);
            Toast.makeText(getApplicationContext(), "Start service", Toast.LENGTH_SHORT).show();
        } else {
            foregroundIntent = GPStracker.serviceIntent;
            Toast.makeText(getApplicationContext(), "already", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onStart() {
        super.onStart();

        childlistRequest(url);


    }


    // 뒤로가기 버튼 2회 입력 시 종료
    @Override
    public void onBackPressed() {

        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(getApplicationContext(), "뒤로 버튼을 한번 더 누르면 종료합니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            moveTaskToBack(true);
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
            toast.cancel();

        }
    }


    @Override
    protected void onDestroy() {//서비스 종료
        super.onDestroy();
        if (null != foregroundIntent) {
            stopService(foregroundIntent);
            foregroundIntent = null;
        }
    }


    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {

                //위치 값을 가져올 수 있음
                ;
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[2])) {

                    Toast.makeText(Parents_main.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();


                } else {

                    Toast.makeText(Parents_main.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();

                }
            }

        }
    }

    void checkRunTimePermission() {


        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(Parents_main.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(Parents_main.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        int hasBackGROUNDLocationPermission = ContextCompat.checkSelfPermission(Parents_main.this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasBackGROUNDLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음


        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(Parents_main.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(Parents_main.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(Parents_main.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(Parents_main.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(Parents_main.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        childlistRequest(url);

    }


    public void childlistRequest(String url) {

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            if (!response.equals(null)) {
                                Log.e("fire", "2");

                                layoutManager = new LinearLayoutManager(getApplicationContext());
                                rv_mychildren.setHasFixedSize(true);//일정한 크기의 아이템뷰를 만들어줌
                                rv_mychildren.setLayoutManager(layoutManager);//LinearLayout으로 리사이클러뷰 모양을 만듬.

                                JSONArray jarray = new JSONArray(response);
                                int size = jarray.length();
                                for (int i = 0; i < size; i++) {
                                    JSONObject row = jarray.getJSONObject(i);
                                    rvMychildrenData = new rv_mychildren_data();
                                    rvMychildrenData.setRv_mychild_image(row.getString("image"));
                                    rvMychildrenData.setRv_mychild_name(row.getString("name"));
                                    rvMychildrenData.setkey(row.getString("key"));
                                    rvMychildrenData.setbirth(row.getString("birth"));

                                    arrayList.add(rvMychildrenData);
                                }
                                rvMychildrenAdapter = new rv_mychildren_adapter(Parents_main.this, arrayList);
                                rv_mychildren.setAdapter(rvMychildrenAdapter);//리사이클러뷰에 어댑터 연결

                               //받은 키값이 있다면 체크
                                Intent intent = getIntent();
                                if (intent.getStringExtra("key") != null) {
                                    String key = intent.getStringExtra("key");
                                    for (int i = 0; i < arrayList.size(); i++) {//아이 엑티비티로 값 전달
                                        if (arrayList.get(i).getRv_mychild_Key().equals(key)) {//받은키와 같은 아이템을 선택해 아이 엑티비티로 전환
                                            intent = new Intent(getApplicationContext(), Child.class);
                                            intent.putExtra("key", arrayList.get(i).getRv_mychild_Key());
                                            intent.putExtra("image", arrayList.get(i).getRv_mychild_image());
                                            intent.putExtra("name", arrayList.get(i).getRv_mychild_name());
                                            intent.putExtra("birth", arrayList.get(i).getRv_mychild_Birth());
                                            startActivity(intent);
                                        }
                                    }
                                }

                            } else if (response.equals(null)) {
                                Toast.makeText(getApplicationContext(), "null..", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() { //에러발생시 호출될 리스너 객체
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("volley", "main error : " + error);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("ID", login_preference.getString("id", ""));
                return params;
            }
        };

        request.setShouldCache(false);
        AppHelper.requestQueue = Volley.newRequestQueue(this);
        AppHelper.requestQueue.add(request);

    }


}
