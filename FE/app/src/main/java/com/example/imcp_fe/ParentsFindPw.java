package com.example.imcp_fe;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.imcp_fe.Network.AppHelper;

import java.util.HashMap;
import java.util.Map;

public class ParentsFindPw extends AppCompatActivity {

    private Button btn_parents_findpw;
    private EditText et_parents_findpw_name, et_parents_findpw_id, et_parents_findpw_email;
    private  String url = "http://tomcat.comstering.synology.me/IMCP_Server/parentFindPW.jsp";
    private String name;
    private String id;
    private String email;
    private Intent intent;



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_password);

        btn_parents_findpw = (Button)findViewById(R.id.btn_parents_findpw);
        et_parents_findpw_name = findViewById(R.id.et_parents_findpw_name);
        et_parents_findpw_id = findViewById(R.id.et_parents_findpw_id);
        et_parents_findpw_email = findViewById(R.id.et_parents_findpw_email);

        btn_parents_findpw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                name= et_parents_findpw_name.getText().toString();
                id = et_parents_findpw_id.getText().toString();
                email  = et_parents_findpw_email.getText().toString();

                if(TextUtils.isEmpty(String.valueOf(name))){
                    Toast.makeText(getApplicationContext(),"이름을 입력해주세요.",Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(String.valueOf(id))){
                    Toast.makeText(getApplicationContext(),"아이디을 입력해주세요.",Toast.LENGTH_SHORT).show();
                }else if(TextUtils.isEmpty(String.valueOf(email))){
                    Toast.makeText(getApplicationContext(),"이메일을 입력해주세요.",Toast.LENGTH_SHORT).show();
                }else {

                    findpwRequest(url);

                }

            }
        });

        // 이름, 아이디, 이메일 서버에 저장된거랑 비교
        //et_parents_findpw_id

        // 재발급 신청 버튼 클릭 시
        // 이메일로 임시비밀번호 발급


    }

    public void findpwRequest(String url) {
        Log.e("volley", "1");
        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        switch (response){
                            case "FindSucess":
                                Toast.makeText(getApplicationContext(), "Sucess",Toast.LENGTH_SHORT).show();
                                changePW();
                                break;
                            case "FindPWFail":
                                Toast.makeText(getApplicationContext(), "Fail",Toast.LENGTH_SHORT).show();
                                break;
                            case "DBError":
                                Toast.makeText(getApplicationContext(), "Error",Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                Log.e("volley", response);
                        }
                    }
                },
                new Response.ErrorListener() { //에러발생시 호출될 리스너 객체
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("error", error.toString());
                        error.printStackTrace();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("ID", id);
                params.put("Name",name);
                params.put("Email",email);
                return params;
            }
        };


        request.setShouldCache(false);
        AppHelper.requestQueue = Volley.newRequestQueue(this);
        AppHelper.requestQueue.add(request);
    }
    public void changePW(){
        intent = new Intent(getApplicationContext(), Change_password.class);
        startActivity(intent);
    }
}

