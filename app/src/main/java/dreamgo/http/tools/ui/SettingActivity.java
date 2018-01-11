package dreamgo.http.tools.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.text.Selection;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;

import dreamgo.http.tools.R;
import dreamgo.http.tools.alipay.AlipayZeroSdk;
import dreamgo.http.tools.http.Constants;
import dreamgo.http.tools.utils.ClearEditText;
import dreamgo.http.tools.utils.Utils;

/**
 * Created by ykh on 2016/11/22.
 */

public class SettingActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private LinearLayout ll_header, ll_md5, ll_highlight, ll_showAll, ll_loop, ll_cache, ll_qq, ll_mail, ll_donate;
    private TextView tv_header, tv_md5;
    private Switch switch_header, switch_md5, switch_highlight, switch_showAll, switch_loop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        initView();
    }

    private void initView() {
        ll_header = (LinearLayout) findViewById(R.id.setting_ll_header);
        ll_md5 = (LinearLayout) findViewById(R.id.setting_ll_md5);
        ll_highlight = (LinearLayout) findViewById(R.id.setting_ll_highlight);
        ll_loop = (LinearLayout) findViewById(R.id.setting_ll_loop);
        ll_showAll = (LinearLayout) findViewById(R.id.setting_ll_showAll);
        ll_cache = (LinearLayout) findViewById(R.id.setting_ll_cache);
        ll_qq = (LinearLayout) findViewById(R.id.setting_ll_qq);
        ll_mail = (LinearLayout) findViewById(R.id.setting_ll_mail);
        ll_donate = (LinearLayout) findViewById(R.id.setting_ll_donate);
        tv_header = (TextView) findViewById(R.id.setting_tv_header);
        tv_md5 = (TextView) findViewById(R.id.setting_tv_md5);
        switch_header = (Switch) findViewById(R.id.setting_switch_header);
        switch_md5 = (Switch) findViewById(R.id.setting_switch_md5);
        switch_highlight = (Switch) findViewById(R.id.setting_switch_highlight);
        switch_showAll = (Switch) findViewById(R.id.setting_switch_showAll);
        switch_loop = (Switch) findViewById(R.id.setting_switch_loop);

        ll_header.setOnClickListener(this);
        ll_md5.setOnClickListener(this);
        ll_highlight.setOnClickListener(this);
        ll_showAll.setOnClickListener(this);
        ll_loop.setOnClickListener(this);
        ll_cache.setOnClickListener(this);
        ll_qq.setOnClickListener(this);
        ll_mail.setOnClickListener(this);
        ll_donate.setOnClickListener(this);

        initText();
        initSwitch();
    }

    private void initText() {
        if (Utils.hasValue(context, Constants.headerValue)) {
            tv_header.setText(Utils.getValue(context, Constants.headerName) + ":"
                    + Utils.getValue(context, Constants.headerValue));
        }
        if (Utils.hasValue(context, Constants.md5Field)) {
            tv_md5.setText("为字段" + Utils.getValue(context, Constants.md5Field) + "进行MD5加密");
        }
    }

    private void initSwitch() {
        // 添加Header功能是否打开
        if (Utils.hasValue(context, Constants.addHeader)) {
            if (Utils.getBooleanValue(context, Constants.addHeader)) {
                switch_header.setChecked(true);
            } else {
                switch_header.setChecked(false);
            }
        }
        switch_header.setOnCheckedChangeListener(this);

        // 加密密码功能是否打开
        if (Utils.hasValue(context, Constants.md5)) {
            if (Utils.getBooleanValue(context, Constants.md5)) {
                switch_md5.setChecked(true);
            } else {
                switch_md5.setChecked(false);
            }
        }
        switch_md5.setOnCheckedChangeListener(this);

        // 高亮状态码功能是否打开
        if (Utils.hasValue(context, Constants.highLight)) {
            if (Utils.getBooleanValue(context, Constants.highLight)) {
                switch_highlight.setChecked(true);
            } else {
                switch_highlight.setChecked(false);
            }
        }
        switch_highlight.setOnCheckedChangeListener(this);

        // 显示完整结果功能是否打开
        if (Utils.hasValue(context, Constants.showAll)) {
            if (Utils.getBooleanValue(context, Constants.showAll)) {
                switch_showAll.setChecked(true);
            } else {
                switch_showAll.setChecked(false);
            }
        }
        switch_showAll.setOnCheckedChangeListener(this);

        // 显示循环发送功能是否打开
        if (Utils.hasValue(context, Constants.sendLoop)) {
            if (Utils.getBooleanValue(context, Constants.sendLoop)) {
                switch_loop.setChecked(true);
            } else {
                switch_loop.setChecked(false);
            }
        }
        switch_loop.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setting_ll_header:
                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.dialog_two, null);
                final ClearEditText headerName = (ClearEditText) view.findViewById(R.id.dialog_two_one);
                final ClearEditText headerValue = (ClearEditText) view.findViewById(R.id.dialog_two_two);
                headerName.setHint("headerName");
                headerValue.setHint("headerValue");
                headerName.setText(Utils.getValue(context, Constants.headerName));
                headerValue.setText(Utils.getValue(context, Constants.headerValue));
//                addHeader.setText("378da32d78c444ce4da2f90ed529b436b8a28429");
                builder.setTitle("添加Header:");
                builder.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String name = headerName.getText().toString().trim();
                                String value = headerValue.getText().toString().trim();
                                if (!name.equals(""))
                                    Utils.putValue(context, Constants.headerName, name);
                                else
                                    Utils.RemoveValue(context, Constants.headerName);

                                if (!value.equals(""))
                                    Utils.putValue(context, Constants.headerValue, value);
                                else
                                    Utils.RemoveValue(context, Constants.headerValue);
                                // 刷新显示
                                initText();
                            }
                        });
                builder.setNegativeButton("取消", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.setView(view);
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        //调出键盘
                        InputMethodManager manager = ((InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE));
                        manager.showSoftInput(headerName, InputMethodManager.SHOW_IMPLICIT);
                        //光标移至末尾处
                        Selection.setSelection(headerName.getText(), headerName.length());
                    }
                });
                alertDialog.show();
                break;
            case R.id.setting_ll_md5:
                AlertDialog.Builder builder_md5;
                builder_md5 = new AlertDialog.Builder(this);
                LayoutInflater inflater_md5 = getLayoutInflater();
                View view_md5 = inflater_md5.inflate(R.layout.dialog_one, null);
                final ClearEditText md5Field = (ClearEditText) view_md5.findViewById(R.id.dialog_one_one);
                md5Field.setHint("待MD5的字段");
                md5Field.setText(Utils.getValue(getApplicationContext(), Constants.md5Field));
                builder_md5.setTitle("MD5:");
                builder_md5.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String md5Name = md5Field.getText().toString().trim();
                                if (!md5Name.equals(""))
                                    Utils.putValue(context, Constants.md5Field, md5Name);
                                else
                                    Utils.RemoveValue(context, Constants.md5Field);
                                // 刷新显示
                                initText();
                            }
                        });
                builder_md5.setNegativeButton("取消", null);
                AlertDialog alertDialog_md5 = builder_md5.create();
                alertDialog_md5.setView(view_md5);
                alertDialog_md5.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        //调出键盘
                        InputMethodManager manager = ((InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE));
                        manager.showSoftInput(md5Field, InputMethodManager.SHOW_IMPLICIT);
                        //光标移至末尾处
                        Selection.setSelection(md5Field.getText(), md5Field.length());
                    }
                });
                alertDialog_md5.show();
                break;
            case R.id.setting_ll_loop:
                AlertDialog.Builder builder_loop;
                builder_loop = new AlertDialog.Builder(this);
                LayoutInflater inflater_loop = getLayoutInflater();
                View view_loop = inflater_loop.inflate(R.layout.dialog_send_loop, null);
                final ClearEditText loopTimes = (ClearEditText) view_loop.findViewById(R.id.dialog_send_loop_times);
                final ClearEditText loopInterval = (ClearEditText) view_loop.findViewById(R.id.dialog_send_loop_interval);
                loopTimes.setHint("循环次数");
                loopInterval.setHint("间隔时间(毫秒)");
                loopTimes.setText(String.valueOf(Utils.getIntValue(context, Constants.loopTimes)));
                loopInterval.setText(String.valueOf(Utils.getIntValue(context, Constants.loopInterval)));
                builder_loop.setTitle("循环设置:");
                builder_loop.setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int times = Integer.parseInt(loopTimes.getText().toString());
                                int interval = Integer.parseInt(loopInterval.getText().toString());
                                if (times >= 0)
                                    Utils.putIntValue(context, Constants.loopTimes, times);
                                else
                                    showToast("请输入大于0的值");

                                if (interval >= 0)
                                    Utils.putIntValue(context, Constants.loopInterval, interval);
                                else
                                    showToast("请输入大于0的值");
                                // 刷新显示
                                initText();
                            }
                        });
                builder_loop.setNegativeButton("取消", null);
                AlertDialog alertDialogLoop = builder_loop.create();
                alertDialogLoop.setView(view_loop);
                alertDialogLoop.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        //调出键盘
                        InputMethodManager manager = ((InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE));
                        manager.showSoftInput(loopTimes, InputMethodManager.SHOW_IMPLICIT);
                        //光标移至末尾处
                        Selection.setSelection(loopTimes.getText(), loopTimes.length());
                    }
                });
                alertDialogLoop.show();
                break;
            case R.id.setting_ll_cache:
                File path = Environment.getExternalStorageDirectory();
                File dir = new File(path, "HttpTools/");
                if (!dir.exists())
                    dir.mkdirs();
                File items = new File(dir.getAbsolutePath(), "items.txt");
                if (items.exists()) {
                    boolean flag = items.delete();
                    // MainActivity OnResume时检测此标志位 为true则进行缓存清理
                    Utils.putBooleanValue(context, Constants.removeCache, true);
                    if (flag)
                        showToast("清除成功");
                }
                break;
            case R.id.setting_ll_qq:
                String url = "mqqwpa://im/chat?chat_type=wpa&uin=614457662";
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                break;
            case R.id.setting_ll_mail:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri
                        .parse("mailto:ykh@dreamgo.tech"));
                startActivity(Intent.createChooser(emailIntent, "请选择邮件类应用"));
                break;
            case R.id.setting_ll_donate:
                if (AlipayZeroSdk.hasInstalledAlipayClient(context)) {
                    AlipayZeroSdk.startAlipayClient(context, "aex06301yzbcysc0jrgbtf9");
                } else {
                    showToast("大爷您未安装支付宝客户端哦 :(");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.setting_switch_header:
                if (isChecked)
                    Utils.putBooleanValue(context, Constants.addHeader, true);
                else
                    Utils.putBooleanValue(context, Constants.addHeader, false);
                break;
            case R.id.setting_switch_md5:
                if (isChecked)
                    Utils.putBooleanValue(context, Constants.md5, true);
                else
                    Utils.putBooleanValue(context, Constants.md5, false);
                break;
            case R.id.setting_switch_highlight:
                if (isChecked)
                    Utils.putBooleanValue(context, Constants.highLight, true);
                else
                    Utils.putBooleanValue(context, Constants.highLight, false);
                break;
            case R.id.setting_switch_showAll:
                if (isChecked)
                    Utils.putBooleanValue(context, Constants.showAll, true);
                else
                    Utils.putBooleanValue(context, Constants.showAll, false);
                break;
            case R.id.setting_switch_loop:
                if (isChecked)
                    Utils.putBooleanValue(context, Constants.sendLoop, true);
                else
                    Utils.putBooleanValue(context, Constants.sendLoop, false);
                break;
            default:
                break;
        }
    }
}
