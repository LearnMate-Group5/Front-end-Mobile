package com.example.LearnMate.network.dto;

import com.google.gson.annotations.SerializedName;

public class ApiResult<T> {
    @SerializedName("value") public T value;
    @SerializedName("isSuccess") public boolean isSuccess;
    @SerializedName("isFailure") public boolean isFailure;
    @SerializedName("error") public ApiError error;
}
