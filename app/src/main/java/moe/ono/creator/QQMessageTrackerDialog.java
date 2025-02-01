package moe.ono.creator;

import static moe.ono.creator.ElementSender.send_ark_msg;
import static moe.ono.util.Session.getContact;
import static moe.ono.util.Session.getCurrentChatType;
import static moe.ono.util.Session.getCurrentPeerID;
import static moe.ono.util.SyncUtils.runOnUiThread;
import static moe.ono.util.analytics.ActionReporter.reportVisitor;
import static moe.ono.util.api.ArkEnv.GET_CARD_DATA_ENDPOINT;
import static moe.ono.util.api.ArkEnv.getAuthAPI;
import static rikka.core.util.ContextUtils.requireActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.core.BottomPopupView;
import com.lxj.xpopup.util.XPopupUtils;

import org.json.JSONException;

import java.util.Objects;
import java.util.UUID;

import moe.ono.R;
import moe.ono.bridge.kernelcompat.ContactCompat;
import moe.ono.common.CheckUtils;
import moe.ono.config.ONOConf;
import moe.ono.hooks.base.util.Toasts;
import moe.ono.hooks.protocol.QFakeFileHelperKt;
import moe.ono.hooks.protocol.QPacketHelperKt;
import moe.ono.ui.CommonContextWrapper;
import moe.ono.ui.view.LoadingButton;
import moe.ono.util.AppRuntimeHelper;
import moe.ono.util.Logger;
import moe.ono.util.api.ark.ArkRequest;
import moe.ono.util.api.ark.ArkRequestCallback;

@SuppressLint("ResourceType")
public class QQMessageTrackerDialog extends BottomPopupView {
    private static BasePopupView popupView;

    public QQMessageTrackerDialog(@NonNull Context context) {
        super(context);
    }

    public static void createView(Context context) {
        Context fixContext = CommonContextWrapper.createAppCompatContext(context);
        XPopup.Builder NewPop = new XPopup.Builder(fixContext).moveUpToKeyboard(true).isDestroyOnDismiss(true);
        NewPop.maxHeight((int) (XPopupUtils.getScreenHeight(context) * .75f));
        NewPop.popupHeight((int) (XPopupUtils.getScreenHeight(context) * .63f));


        reportVisitor(AppRuntimeHelper.getAccount(), "CreateView-QQMessageTrackerDialog");

        popupView = NewPop.asCustom(new QQMessageTrackerDialog(fixContext));
        popupView.show();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate() {
        super.onCreate();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            TextView tvTarget = findViewById(R.id.tv_target);

            EditText title = findViewById(R.id.title);
            EditText desc = findViewById(R.id.desc);
            EditText icon = findViewById(R.id.icon);
            EditText preview = findViewById(R.id.preview);
            EditText prompt = findViewById(R.id.prompt);
            EditText url = findViewById(R.id.url);
            LoadingButton loadingButton = findViewById(R.id.btn_send);


            int chat_type = getCurrentChatType();
            if (chat_type == 1) {
                tvTarget.setText("当前会话: " + getCurrentPeerID() + " | " + "好友");
            } else if (chat_type == 2) {
                tvTarget.setText("当前会话: " + getCurrentPeerID() + " | " + "群聊");
            } else {
                tvTarget.setText("当前会话: " + getCurrentPeerID() + " | " + "未知");
            }

            title.setVisibility(VISIBLE);
            desc.setVisibility(VISIBLE);
            icon.setVisibility(VISIBLE);
            preview.setVisibility(VISIBLE);
            prompt.setVisibility(VISIBLE);
            url.setVisibility(VISIBLE);



            title.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    ONOConf.setString("input", "title", String.valueOf(s));
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
            title.setText(ONOConf.getString("input", "title", ""));

            desc.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    ONOConf.setString("input", "desc", String.valueOf(s));
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
            desc.setText(ONOConf.getString("input", "desc", ""));

            icon.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    ONOConf.setString("input", "icon", String.valueOf(s));
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
            icon.setText(ONOConf.getString("input", "icon", ""));

            preview.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    ONOConf.setString("input", "preview", String.valueOf(s));
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
            preview.setText(ONOConf.getString("input", "preview", ""));

            prompt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    ONOConf.setString("input", "prompt", String.valueOf(s));
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
            prompt.setText(ONOConf.getString("input", "prompt", ""));

            url.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    ONOConf.setString("input", "url", String.valueOf(s));
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
            url.setText(ONOConf.getString("input", "url", ""));

            loadingButton.setOnClickListener(v -> {
                loadingButton.setEnabled(false);
                loadingButton.setLoading(true);

                String id = getRandomUuid();
                String API_endpoint = "/get_card?title=%s&desc=%s&icon=%s&prompt=%s&url=%s&preview=%s";
                String endpoint = String.format(API_endpoint, title.getText(), desc.getText(), icon.getText(), prompt.getText(), url.getText(), getAuthAPI() + "s/" + id + ".png");
                String img = Objects.requireNonNull(preview.getText()).toString();
                if (!img.isEmpty()){
                    endpoint = endpoint.substring(0, endpoint.length() - 4);
                    endpoint += "/" + Base64.encodeToString(img.getBytes(), Base64.NO_WRAP);
                }

                Logger.d("endpoint", endpoint);

                ArkRequest.sendSignatureRequest(endpoint, null, getContext(), new ArkRequestCallback() {
                    @Override
                    public void onSuccess(String result) {
                        runOnUiThread(() -> {
                            loadingButton.setLoading(false);
                            loadingButton.setEnabled(true);
                            if (CheckUtils.isJSON(result)) {
                                new MaterialAlertDialogBuilder(getContext())
                                        .setTitle("服务器返回有效签名数据")
                                        .setMessage("签名成功，是否发送追踪卡片？")
                                        .setPositiveButton("发送", (dialog, which) -> {
                                            ContactCompat contactCompat = getContact();
                                            try {
                                                send_ark_msg(result, contactCompat);
                                            } catch (JSONException e) {
                                                Logger.e(e);
                                            }
                                        })
                                        .setNegativeButton("关闭", null)
                                        .show();
                            } else {
                                new MaterialAlertDialogBuilder(getContext())
                                        .setTitle("服务器响应了你的请求，但貌似出了点问题")
                                        .setMessage(result)
                                        .setPositiveButton("好", null)
                                        .show();
                            }

                            Logger.d(result);
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        runOnUiThread(() -> new MaterialAlertDialogBuilder(getContext())
                                .setTitle("出了点问题！")
                                .setMessage("请求失败: " + e.getMessage())
                                .setPositiveButton("好", null)
                                .show());
                        Logger.e(e);
                        runOnUiThread(() -> {
                            loadingButton.setLoading(false);
                            loadingButton.setEnabled(true);
                        });
                    }
                });
            });

        }, 100);


    }


    private String getRandomUuid(){
        return UUID.randomUUID().toString().trim().replaceAll("-", "");
    }



    @Override
    protected void beforeDismiss() {
        super.beforeDismiss();
    }

    @Override
    protected void onDismiss() {
        super.onDismiss();
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.layout_qq_message_tracker;
    }
}


