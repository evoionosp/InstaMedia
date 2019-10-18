package com.developer.devshubhpatel.instamedia.utils;



import com.developer.devshubhpatel.instamedia.models.IMedia;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface RetrofitInterface {

    @GET
    @Streaming
    Call<ResponseBody> downloadFile(@Url String url);

    @GET("{short_code}/?__a=1")
    Call<IMedia> JsonResponseURL(@Path("short_code") String short_code);
}
