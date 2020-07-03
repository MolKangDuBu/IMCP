package com.example.imcp_fe;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.imcp_fe.Network.AppHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ParentsFindId extends AppCompatActivity {

    private EditText et_findid_name;
    private  EditText et_findid_email;
    private Button btn_findid_ok;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_id);

        et_findid_name = findViewById(R.id.et_parents_findid_name);
        et_findid_email = findViewById(R.id.et_parents_findid_email);
        btn_findid_ok = findViewById(R.id.btn_parents_findid_ok);

        btn_findid_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkRequest(et_findid_name.getText().toString(), et_findid_email.getText().toString());
            }
        });
        // 찾기 버튼을 누르면
        // 이름과 이메일이 존재하는지 판단 후
        // 이메일로 아이디를 전송한다.

    }

    public void checkRequest(String name, String email) {
        String url = "https://www.google.co.kr";

        StringRequest request = new StringRequest(
                Request.Method.GET,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONArray jarray = new JSONArray(response);
                            int size = jarray.length();
                            for (int i = 0; i < size; i++) {
                                JSONObject row = jarray.getJSONObject(i);
                             /*   x= row.getDouble("x"); // x, y 좌표를 받아옴.
                                y = row.getDouble("y");
                           */ }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() { //에러발생시 호출될 리스너 객체
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
              //  params.put("",sendlocation);
                return params;
            }
        };

        request.setShouldCache(false);
        AppHelper.requestQueue.add(request);
    }
}
