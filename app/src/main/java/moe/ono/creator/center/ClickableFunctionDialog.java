package moe.ono.creator.center;

import static moe.ono.constants.Constants.PrekCfgXXX;
import static moe.ono.constants.Constants.PrekClickableXXX;
import static moe.ono.constants.Constants.PrekClickableXXX;
import static moe.ono.hooks._core.factory.HookItemFactory.getItem;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import moe.ono.config.ConfigManager;
import moe.ono.hooks._base.BaseClickableFunctionHookItem;
import moe.ono.hooks.item.chat.StickerPanelEntry;
import moe.ono.hooks.item.sigma.SurnamePredictor;
import moe.ono.util.Logger;

public class ClickableFunctionDialog {
    public static void showCFGDialogSurnamePredictor(BaseClickableFunctionHookItem item, Context context){
        if (context == null) return;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("猜姓氏");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        final MaterialCheckBox checkBox = new MaterialCheckBox(context);
        checkBox.setText("启用");

        final TextView textView = new TextView(context);
        final EditText input = new EditText(context);
        input.setHint("300");
        input.setText(String.valueOf(ConfigManager.dGetInt(PrekCfgXXX + item.getPath(), 300)));
        textView.setText("操作间隔（毫秒）");
        layout.addView(checkBox);
        layout.addView(textView);
        layout.addView(input);

        builder.setView(layout);

        final TextView warningText = new TextView(context);
        layout.addView(warningText);

        builder.setNegativeButton("关闭", (dialog, which) -> dialog.cancel());

        checkBox.setChecked(item.isEnabled());

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ConfigManager.dPutBoolean(PrekClickableXXX + getItem(SurnamePredictor.class).getPath(), isChecked);
            item.setEnabled(isChecked);
            if (isChecked) {
                item.startLoad();
            }
        });

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    ConfigManager.dPutInt(PrekCfgXXX + getItem(SurnamePredictor.class).getPath(), Integer.parseInt(s.toString()));
                    warningText.setText("");
                } catch (NumberFormatException e) {
                    warningText.setText("输入错误");
                }

            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        builder.show();
    }

    public static void showCFGDialogStickerPanelEntry(BaseClickableFunctionHookItem item, Context context){
        if (context == null) return;
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("表情面板");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        final MaterialCheckBox checkBox = new MaterialCheckBox(context);
        checkBox.setText("启用");

        final MaterialCheckBox checkBox2 = new MaterialCheckBox(context);
        checkBox2.setText("在底部新增一个按钮用于打开面板 (没写)");

        checkBox.setChecked(ConfigManager.dGetBoolean(PrekClickableXXX + getItem(StickerPanelEntry.class).getPath()));
        checkBox2.setChecked(ConfigManager.dGetBoolean(PrekCfgXXX + "createStickerPanelIcon"));
        final TextView textView = new TextView(context);
        textView.setText("更多设置");
        layout.addView(checkBox);
        layout.addView(textView);
        layout.addView(checkBox2);

        builder.setView(layout);

        final TextView warningText = new TextView(context);
        layout.addView(warningText);

        builder.setNegativeButton("关闭", (dialog, which) -> dialog.cancel());

        checkBox.setChecked(item.isEnabled());

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ConfigManager.dPutBoolean(PrekClickableXXX + item.getPath(), isChecked);
            item.setEnabled(isChecked);
            if (isChecked) {
                item.startLoad();
            }
        });

        checkBox2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ConfigManager.dPutBoolean(PrekCfgXXX + "createStickerPanelIcon", isChecked);
            if (isChecked) {
                item.startLoad();
            }
        });



        builder.show();
    }
}
