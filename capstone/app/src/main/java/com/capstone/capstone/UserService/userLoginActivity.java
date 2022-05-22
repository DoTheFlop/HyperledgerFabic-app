package com.capstone.capstone.UserService;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.capstone.capstone.API.UserApi;
import com.capstone.capstone.DTO.JwtToken;
import com.capstone.capstone.DTO.UserLoginDTO;
import com.capstone.capstone.DTO.UserLoginRequestDTO;
import com.capstone.capstone.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class userLoginActivity extends AppCompatActivity {

    EditText loginId, loginPassword;
    TextView resultText;
    Button confirm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login");

        loginId = (EditText) findViewById(R.id.userLoginId);
        loginPassword = (EditText) findViewById(R.id.userLoginPassword);
        resultText = (TextView) findViewById(R.id.userLoginResult);
        confirm = (Button) findViewById(R.id.userLoginConfirm);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserLoginRequestDTO userLoginRequestDTO = new UserLoginRequestDTO(
                        Long.parseLong(loginId.getText().toString()),
                        loginPassword.getText().toString()
                );
                userLoginService(userLoginRequestDTO);
            }
        });

    }

    public void userLoginService(UserLoginRequestDTO userLoginRequestDTO){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.localhost))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        UserApi service = retrofit.create(UserApi.class);

        Call<UserLoginDTO> call = service.login(userLoginRequestDTO);

        call.enqueue(new Callback<UserLoginDTO>() {
            @Override
            public void onResponse(Call<UserLoginDTO> call, Response<UserLoginDTO> response) {
                if(response.isSuccessful()){
                    UserLoginDTO result = response.body();
                    resultText.setText(result.toString());
                    JwtToken.setToken(result.getAccessToken());
                    Log.d(TAG, "onResponse: 성공, 결과 \n" + result.toString());
                }else{
                    Log.d(TAG, "onResponse: 실패");
                }
            }

            @Override
            public void onFailure(Call<UserLoginDTO> call, Throwable t) {
                Log.d(TAG,"onFailure" + t.getMessage());
            }
        });
    }
}