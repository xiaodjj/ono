package moe.ono.hooks.base;

import static moe.ono.constants.Constants.MethodCacheKey_InputRoot;
import static moe.ono.util.SyncUtils.runOnUiThread;
import static moe.ono.util.Utils.findMethodByName;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import moe.ono.config.ConfigManager;
import moe.ono.creator.ElementSender;
import moe.ono.hooks._base.BaseFunctionHookItem;
import moe.ono.hooks._base.BaseHookItem;
import moe.ono.hooks._core.annotation.HookItem;
import moe.ono.hooks.unlimit.MaskLengthFilter;
import moe.ono.startup.HookBase;
import moe.ono.util.Logger;

@SuppressLint("DiscouragedApi")
@HookItem(path = "获取InputRoot", description = "")
public class HookInputRoot extends BaseFunctionHookItem {
    public void hookInputRoot(ClassLoader classLoader) {

        String cachedMethodSignature = ConfigManager.getDefaultConfig().getString(MethodCacheKey_InputRoot, null);
        Method targetMethod = null;

        if (cachedMethodSignature != null) {
            try {
                String[] parts = cachedMethodSignature.split("#");
                String className = parts[0];
                String methodName = parts[1];
                Class<?> clazz = classLoader.loadClass(className);
                targetMethod = findMethodByName(clazz, methodName);
            } catch (Exception e) {
                Logger.e("Error loading method from cache: " + e);
            }
        }



        try {
            XposedBridge.hookMethod(targetMethod, new XC_MethodHook(40) {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);

                    Button sendBtn = null;
                    EditText editText = null;
                    ViewGroup inputRoot = null;
                    Field[] fs = param.thisObject.getClass().getDeclaredFields();
                    for (Field f : fs) {
                        Class<?> type = f.getType();
                        if (type.equals(Button.class)) {
                            f.setAccessible(true);
                            sendBtn = (Button) f.get(param.thisObject);
                            Logger.d(String.valueOf(f.get(param.thisObject)));
                        } else if (type.equals(EditText.class)) {
                            f.setAccessible(true);
                            editText = (EditText) f.get(param.thisObject);
                        } else if (type.equals(ViewGroup.class)) {
                            f.setAccessible(true);
                            inputRoot = (ViewGroup) f.get(param.thisObject);
                            assert inputRoot != null;
                        }
                    }


                    if (sendBtn != null && editText != null && inputRoot != null){
                        if (!new MaskLengthFilter().isEnabled()){
                            return;
                        }
                        EditText finalEditText = editText;
                        sendBtn.setOnLongClickListener(v -> {
                            runOnUiThread(() -> ElementSender.createView(null, v.getContext(), finalEditText.getText().toString()));

                            return true;
                        });
                        EditText finalEditText1 = editText;
                        editText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                finalEditText1.setFilters( new InputFilter[]{ new InputFilter.LengthFilter(Integer.MAX_VALUE)});
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {

                            }
                        });

                    }
                }
            });
        } catch (Exception e) {
            Logger.e("err:"+e);
        }
    }

    @Override
    public void load(@NonNull ClassLoader classLoader) throws Throwable {
        hookInputRoot(classLoader);
    }
}