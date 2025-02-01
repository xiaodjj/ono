package moe.ono.util.api.ark;

public interface ArkRequestCallback {
    void onSuccess(String result);
    void onFailure(Exception e);
}