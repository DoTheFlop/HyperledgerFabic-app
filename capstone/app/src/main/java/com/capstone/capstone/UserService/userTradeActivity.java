package com.capstone.capstone.UserService;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.capstone.capstone.API.UserApi;
import com.capstone.capstone.API.UserTradeApi;
import com.capstone.capstone.DTO.JwtToken;
import com.capstone.capstone.DTO.UserGetAssetDTO;
import com.capstone.capstone.DTO.UserTradeResponseDTO;
import com.capstone.capstone.R;
import com.capstone.capstone.TradeRecycler.Adapter;
import com.capstone.capstone.TradeRecycler.PaintTitle;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class userTradeActivity extends AppCompatActivity {

    private static final String TAG = "userTradeActivity";
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;

    private int page = 1;
    boolean isLoading = false;

    ArrayList<PaintTitle> myDataset = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usertradehistory);

        mRecyclerView = (RecyclerView) findViewById(R.id.TradeRecycler);
        populateData();
        initAdapter();
        initScrollListener();
    }

    private void populateData() {
        getUserTradeHistory(page);
    }

    private void initAdapter() {
        mAdapter = new Adapter(myDataset);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(layoutManager);
    }

    private void initScrollListener(){
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if(!isLoading) {
                    if(layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == myDataset.size() - 1){
                        loadMore();
                        isLoading = true;
                    }

                }
            }
        });
    }

    private void loadMore(){
        myDataset.add(null);
        mAdapter.notifyItemInserted(myDataset.size() - 1);

        System.out.println("myDataset.size()1 = " + (myDataset.size() - 1));
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                System.out.println("myDataset.size()2 = " + (myDataset.size() - 1));

                getUserTradeHistory(page);
                isLoading = false;
            }
        }, 2000);
    }

    public void getUserTradeHistory(int pageInit){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.localhost))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        UserTradeApi service = retrofit.create(UserTradeApi.class);

        System.out.println("jwtToken = " + JwtToken.getJwt());
        Call<List<UserTradeResponseDTO>> call = service.trade(JwtToken.getJwt(), pageInit);
        Toast loadingToast = Toast.makeText(getApplicationContext(), "기록을 불러오는 중...", Toast.LENGTH_SHORT);
        loadingToast.show();

        call.enqueue(new Callback<List<UserTradeResponseDTO>>() {
            @Override
            public void onResponse(Call<List<UserTradeResponseDTO>> call, Response<List<UserTradeResponseDTO>> response) {
                if(response.isSuccessful()){
                    System.out.println(page + "------");
                    if(page > 1){
                        myDataset.remove(myDataset.size() - 1);
                        mAdapter.notifyItemRemoved(myDataset.size());
                    }
                    List<UserTradeResponseDTO> result = response.body();
                    for (UserTradeResponseDTO userTradeResponseDTO : result) {

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                        LocalDateTime dateTime = LocalDateTime.parse(userTradeResponseDTO.getDateCreated(), formatter);
                        String yyMd = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                        System.out.println("JwtToken.getId() = " + JwtToken.getId());
                        System.out.println("userTradeResponseDTO = " + userTradeResponseDTO.getSenderStudentId().toString());
                        if(JwtToken.getId().equals(userTradeResponseDTO.getSenderStudentId().toString())){
                            System.out.println("dateTime = " + dateTime);
                            myDataset.add(new PaintTitle
                                    (
                                            userTradeResponseDTO.getReceiverStudentIdOrPhoneNumber().toString(), userTradeResponseDTO.getReceiverName(),
                                            userTradeResponseDTO.getCoinName(), "-" + userTradeResponseDTO.getAmount().toString(),
                                            yyMd, dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                                    )
                            );
                            System.out.println(dateTime);
                        }else{
                            myDataset.add(new PaintTitle
                                    (
                                            userTradeResponseDTO.getSenderStudentId().toString(), userTradeResponseDTO.getSenderName(),
                                            userTradeResponseDTO.getCoinName(), userTradeResponseDTO.getAmount().toString(),
                                            yyMd, dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                                    )
                            );

                        }

                    }
                    mAdapter.notifyDataSetChanged();
                    page++;
                    System.out.println("page: " + page);
                    loadingToast.cancel();
                    if(result.toString() == "[]"){
                        Toast.makeText(getApplicationContext(), "더 이상 기록이 없습니다.", Toast.LENGTH_SHORT).show();
                    } else{
                        Toast.makeText(getApplicationContext(), "완료", Toast.LENGTH_SHORT).show();
                    }
                    Log.d(TAG, "onResponse: 성공, 결과 \n" + result.toString());
                }else{
                    Log.d(TAG, "onResponse: 실패");
                }
            }

            @Override
            public void onFailure(Call<List<UserTradeResponseDTO>> call, Throwable t) {
                Log.d(TAG,"onFailure" + t.getMessage());
            }
        });
    }
}
