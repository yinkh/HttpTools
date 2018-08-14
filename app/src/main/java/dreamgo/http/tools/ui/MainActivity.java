package dreamgo.http.tools.ui;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.folderselector.FileChooserDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

import dreamgo.http.tools.R;
import dreamgo.http.tools.http.Constants;
import dreamgo.http.tools.http.HttpClient;
import dreamgo.http.tools.utils.ClearEditText;
import dreamgo.http.tools.utils.DES;
import dreamgo.http.tools.utils.ItemManager;
import dreamgo.http.tools.utils.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class MainActivity extends BaseActivity implements View.OnClickListener, FileChooserDialog.FileCallback {
    private Context context;
    private CoordinatorLayout container;
    private FloatingActionButton refresh;
    private String current_method;
    private ClearEditText url, paramsName, paramsValue;
    private ScrollView scroll_view;
    private TextView show;
    private Button var, clear, type, add, POST, GET, DELETE, PUT, PATCH;
    private OkHttpClient client;
    private MultipartBody.Builder builder;
    private File file;
    private Boolean hasPart;

    private ArrayList<String> urls = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> values = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        checkPermission();
        readData("urls", urls);
        readData("names", names);
        readData("values", values);

        initView();
        setListener();
        initHttp();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /**
         *   检查是否需要清理缓存
         */
        if (Utils.hasValue(context, Constants.removeCache)) {
            if (Utils.getBooleanValue(context, Constants.removeCache)) {
                urls.clear();
                names.clear();
                values.clear();

                setAutoCompleteSource(urls, url);
                setAutoCompleteSource(names, paramsName);
                setAutoCompleteSource(values, paramsValue);
                Utils.putBooleanValue(context, Constants.removeCache, false);
            }
        }
    }


    /**
     * 检测是否有存储权限
     */
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    20);
        }
    }

    /**
     * 由文件读取缓存
     */
    private void readData(String name, ArrayList<String> dataList) {
        ItemManager.getManager().readItems();
        String data = ItemManager.getManager().getItem(name).replace("[", "").replace("]", "").replace(" ", "");

        StringTokenizer tokenizer = new StringTokenizer(data, ",");
        while (tokenizer.hasMoreTokens()) {
            dataList.add(tokenizer.nextToken());
        }
    }

    /**
     * 设置数据源
     *
     * @param dataList
     * @param clearEditText
     */
    private void setAutoCompleteSource(ArrayList<String> dataList, ClearEditText clearEditText) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, dataList.toArray(new String[dataList.size()]));
        clearEditText.setAdapter(adapter);
    }

    /**
     * 添加记录
     *
     * @param dataList
     * @param clearEditText
     */
    private void addSearchInput(ArrayList<String> dataList, ClearEditText clearEditText) {
        String input = clearEditText.getText().toString();
        if (!dataList.contains(input)) {
            dataList.add(input);
            setAutoCompleteSource(dataList, clearEditText);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        /**
         * 检测是否获取存储权限成功
         */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            initView();
        }
    }

    private void initView() {
        container = (CoordinatorLayout) findViewById(R.id.main_container);
        refresh = (FloatingActionButton) findViewById(R.id.refresh);

        url = (ClearEditText) findViewById(R.id.url);
        url.setText(Utils.getValue(context, "url"));
        //光标移至url结尾处
        Selection.setSelection(url.getText(), url.length());

        paramsName = (ClearEditText) findViewById(R.id.params_name);
        paramsValue = (ClearEditText) findViewById(R.id.params_value);
        scroll_view = (ScrollView) findViewById(R.id.scroll_view);
        show = (TextView) findViewById(R.id.show);

        var = (Button) findViewById(R.id.var);
        clear = (Button) findViewById(R.id.clear);
        type = (Button) findViewById(R.id.params_type);
        add = (Button) findViewById(R.id.add);
        POST = (Button) findViewById(R.id.POST);
        GET = (Button) findViewById(R.id.GET);
        DELETE = (Button) findViewById(R.id.DELETE);
        PUT = (Button) findViewById(R.id.PUT);
        PATCH = (Button) findViewById(R.id.PATCH);

        setAutoCompleteSource(urls, url);
        setAutoCompleteSource(names, paramsName);
        setAutoCompleteSource(values, paramsValue);
    }

    private void setListener() {
        var.setOnClickListener(this);
        clear.setOnClickListener(this);
        type.setOnClickListener(this);
        add.setOnClickListener(this);
        POST.setOnClickListener(this);
        GET.setOnClickListener(this);
        DELETE.setOnClickListener(this);
        PUT.setOnClickListener(this);
        PATCH.setOnClickListener(this);
        refresh.setOnClickListener(this);
    }

    private void initHttp() {
        show.setText("");
        builder = new MultipartBody.Builder();
        client = new OkHttpClient();
        file = null;
        hasPart = false;
    }


    /**
     * 智能处理未添加 http:// 或 https:// 的情况
     *
     * @return
     */
    private String getUrl() {
        String URL = url.getText().toString();
        if (URL.contains("http://") || URL.contains("https://"))
            return URL;
        else
            return "http://" + URL;
    }

    /**
     * 将返回结果打印至界面
     *
     * @param response
     */
    private void processResponse(final Response response) {
        final int old_height = show.getHeight();
        try {
            final String str_response = response.body().string();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    show.append("code:" + response.code() + "\n");
                    //先按JSONObject打印 无法打印按JSONArray打印 依旧无法打印则打印string
                    try {
                        JSONObject jsonObject = new JSONObject(str_response);
                        show.append("body:" + jsonObject.toString(4) + "\n");
                    } catch (JSONException e) {
                        try {
                            JSONArray jsonArray = new JSONArray(str_response);
                            show.append("body:" + jsonArray.toString(4) + "\n");
                        } catch (JSONException e1) {
                            show.append("body:" + str_response + "\n");
                            e1.printStackTrace();
                        }
                        e.printStackTrace();
                    }
                    show.append("message:" + response.message().toString() + "\n");

                    if (Utils.hasValue(context, Constants.showAll)) {
                        if (Utils.getBooleanValue(context, Constants.showAll)) {
                            //显示完整结果
                            show.append("all:" + response.toString() + "\n");
                        }
                    }
                    show.append("-----------------------------------------" +
                            "---------------------------------------------");

                    if (Utils.hasValue(context, Constants.highLight)) {
                        if (Utils.getBooleanValue(context, Constants.highLight)) {
                            //高亮状态码
                            int startIndex = 0;
                            String text = show.getText().toString();
                            SpannableStringBuilder stringBuilder = new SpannableStringBuilder(text);
                            while (text.substring(startIndex).contains("code:")) {
                                startIndex = text.indexOf("code:", startIndex) + 5;
                                int end = startIndex + String.valueOf(response.code()).length();

                                //以2开头的状态码设置为绿色 其他为红色
                                stringBuilder.setSpan(new ForegroundColorSpan(text.substring(startIndex).startsWith("2") ? Color.GREEN : Color.RED),
                                        startIndex, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                            }
                            show.setText(stringBuilder);

                        }
                    }

                    //自动滚动到底部
                    scroll_view.post(new Runnable() {
                        @Override
                        public void run() {
                            scroll_view.smoothScrollBy(0, show.getHeight() - old_height + 200);
                        }
                    });

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * 根据是否添加头部更换HttpClient
     */
    private void changeClientByHeader() {
        if (Utils.getBooleanValue(context, Constants.addHeader)) {
            HttpClient.setHeaderName(Utils.getValue(context, Constants.headerName));
            HttpClient.setHeaderValue(Utils.getValue(context, Constants.headerValue));
            client = HttpClient.getClient();
            show.append(Utils.getValue(context, Constants.headerName) + ":"
                    + Utils.getValue(context, Constants.headerValue) + "\n");
        } else {
            client = new OkHttpClient();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear:
                initHttp();
                collapseSoftInput();
                showSnackBar(container, "已重置");
                break;
            case R.id.params_type:
                new FileChooserDialog.Builder(this)
                        .initialPath(Environment.getExternalStorageDirectory().getPath())  // changes initial path, defaults to external storage directory
                        .mimeType("*/*") // Optional MIME type filter
                        .show(this); // an AppCompatActivity which implements FileCallback
                break;
            case R.id.add:
                collapseSoftInput();

                String name = paramsName.getText().toString();
                String value = paramsValue.getText().toString();
                if (file == null) {
                    // 检测是否需要MD5
                    if (Utils.hasValue(context, Constants.md5Field)) {
                        if (name.equals(Utils.getValue(context, Constants.md5Field))
                                && Utils.getBooleanValue(context, Constants.md5)) {
                            value = DES.md5(value);
                        }
                    }

                    builder.addFormDataPart(name, value);
                } else {
                    value = value + "(文件)";
                    if (file.exists() && file.length() > 0) {
                        builder.addFormDataPart(name, file.getName(),
                                RequestBody.create(MediaType.parse("application/octet-stream"), file));
                    }
                    file = null;
                }

                hasPart = true;
                // 缓存参数键值对
                addSearchInput(names, paramsName);
                addSearchInput(values, paramsValue);

                final int old_height = show.getHeight();

                show.append(name + ":" + value + "\n");

                paramsName.setText("");
                paramsValue.setText("");

                //自动滚动到底部
                scroll_view.post(new Runnable() {
                    @Override
                    public void run() {
                        scroll_view.smoothScrollBy(0, show.getHeight() - old_height + 200);
                    }
                });
                break;
            case R.id.POST:
                current_method = "POST";
                collapseSoftInput();

                if (Utils.hasValue(context, Constants.sendLoop)) {
                    if (Utils.getBooleanValue(context, Constants.sendLoop)) {
                        POST();

                        Handler handler1 = new Handler();
                        for (int i = 1; i < Utils.getIntValue(context, Constants.loopTimes); i++) {
                            handler1.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    POST();
                                }
                            }, Utils.getIntValue(context, Constants.loopInterval) * i);
                        }
                    } else {
                        POST();
                    }
                } else {
                    POST();
                }
                break;
            case R.id.GET:
                current_method = "GET";
                collapseSoftInput();

                if (Utils.hasValue(context, Constants.sendLoop)) {
                    if (Utils.getBooleanValue(context, Constants.sendLoop)) {
                        GET();

                        Handler handler1 = new Handler();
                        for (int i = 1; i < Utils.getIntValue(context, Constants.loopTimes); i++) {
                            handler1.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    GET();
                                }
                            }, Utils.getIntValue(context, Constants.loopInterval) * i);
                        }
                    } else {
                        GET();
                    }
                } else {
                    GET();
                }
                break;
            case R.id.DELETE:
                current_method = "DELETE";
                collapseSoftInput();

                if (Utils.hasValue(context, Constants.sendLoop)) {
                    if (Utils.getBooleanValue(context, Constants.sendLoop)) {
                        DELETE();

                        Handler handler1 = new Handler();
                        for (int i = 1; i < Utils.getIntValue(context, Constants.loopTimes); i++) {
                            handler1.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    DELETE();
                                }
                            }, Utils.getIntValue(context, Constants.loopInterval) * i);
                        }
                    } else {
                        DELETE();
                    }
                } else {
                    DELETE();
                }
                break;
            case R.id.PUT:
                current_method = "PUT";
                collapseSoftInput();

                if (Utils.hasValue(context, Constants.sendLoop)) {
                    if (Utils.getBooleanValue(context, Constants.sendLoop)) {
                        PUT();

                        Handler handler1 = new Handler();
                        for (int i = 1; i < Utils.getIntValue(context, Constants.loopTimes); i++) {
                            handler1.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    PUT();
                                }
                            }, Utils.getIntValue(context, Constants.loopInterval) * i);
                        }
                    } else {
                        PUT();
                    }
                } else {
                    PUT();
                }
                break;
            case R.id.PATCH:
                current_method = "PATCH";
                collapseSoftInput();

                if (Utils.hasValue(context, Constants.sendLoop)) {
                    if (Utils.getBooleanValue(context, Constants.sendLoop)) {
                        PATCH();

                        Handler handler1 = new Handler();
                        for (int i = 1; i < Utils.getIntValue(context, Constants.loopTimes); i++) {
                            handler1.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    PATCH();
                                }
                            }, Utils.getIntValue(context, Constants.loopInterval) * i);
                        }
                    } else {
                        PATCH();
                    }
                } else {
                    PATCH();
                }
                break;
            case R.id.refresh:
                Animation animation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setDuration(1000);
                refresh.startAnimation(animation);

                collapseSoftInput();

                refreshRequest();
                break;
            default:
                break;
        }
    }

    /**
     * 获取RequestBody MultipartBody要求必填参数
     *
     * @return
     */
    private RequestBody getRequestBody() {
        RequestBody requestBody;
        if (hasPart) {
            requestBody = builder.setType(MultipartBody.FORM).build();
        } else {
            FormBody.Builder builder = new FormBody.Builder();
            requestBody = builder.build();
        }
        return requestBody;
    }

    /**
     * POST请求
     */
    private void POST() {
        if (url.getText().toString().equals("") || !URLUtil.isValidUrl(getUrl())) {
            url.setError("非法URL");
            return;
        }

        Utils.putValue(context, "url", url.getText().toString());
        addSearchInput(urls, url);

        //填充参数
        Request requestPost = new Request.Builder()
                .post(getRequestBody())
                .url(getUrl())
                .build();
        //判断是否使用Header
        changeClientByHeader();

        //POST请求
        client.newCall(requestPost).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        show.append("failure:" + e.toString() + "\n");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                processResponse(response);
            }
        });
    }

    /**
     * GET请求
     */
    private void GET() {
        if (url.getText().toString().equals("") || !URLUtil.isValidUrl(getUrl())) {
            url.setError("非法URL");
            return;
        }

        Utils.putValue(context, "url", url.getText().toString());
        addSearchInput(urls, url);

        //填充参数
        Request requestGet = new Request.Builder()
                .get()
                .url(getUrl())
                .build();
        //判断是否使用Header
        changeClientByHeader();

        //GET请求
        client.newCall(requestGet).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        show.append("failure:" + e.toString() + "\n");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                processResponse(response);
            }
        });
    }

    /**
     * DELETE请求
     */
    private void DELETE() {
        if (url.getText().toString().equals("") || !URLUtil.isValidUrl(getUrl())) {
            url.setError("非法URL");
            return;
        }

        Utils.putValue(context, "url", url.getText().toString());
        addSearchInput(urls, url);

        //填充参数
        Request requestDel = new Request.Builder()
                .delete(getRequestBody())
                .url(getUrl())
                .build();
        //判断是否使用Header
        changeClientByHeader();

        //GET请求
        client.newCall(requestDel).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        show.append("failure:" + e.toString() + "\n");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                processResponse(response);
            }
        });
    }

    /**
     * PUT请求
     */
    private void PUT() {
        if (url.getText().toString().equals("") || !URLUtil.isValidUrl(getUrl())) {
            url.setError("非法URL");
            return;
        }

        Utils.putValue(context, "url", url.getText().toString());
        addSearchInput(urls, url);

        //填充参数
        Request requestPut = new Request.Builder()
                .put(getRequestBody())
                .url(getUrl())
                .build();
        //判断是否使用Header
        changeClientByHeader();

        //POST请求
        client.newCall(requestPut).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        show.append("failure:" + e.toString() + "\n");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                processResponse(response);
            }
        });
    }

    /**
     * PATCH请求
     */
    private void PATCH() {
        if (url.getText().toString().equals("") || !URLUtil.isValidUrl(getUrl())) {
            url.setError("非法URL");
            return;
        }

        Utils.putValue(context, "url", url.getText().toString());
        addSearchInput(urls, url);

        //填充参数
        Request requestPatch = new Request.Builder()
                .patch(getRequestBody())
                .url(getUrl())
                .build();
        //判断是否使用Header
        changeClientByHeader();

        //POST请求
        client.newCall(requestPatch).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        show.append("failure:" + e.toString() + "\n");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                processResponse(response);
            }
        });
    }

    /**
     * 重复上次请求
     */
    private void refreshRequest() {
        if (current_method != null) {
            switch (current_method) {
                case "POST":
                    POST();
                    break;
                case "GET":
                    GET();
                    break;
                case "DELETE":
                    DELETE();
                    break;
                case "PUT":
                    PUT();
                    break;
                case "PATCH":
                    PATCH();
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_clear:
                show.setText("");
                showSnackBar(container, "已清屏");
                break;
            case R.id.menu_export:
                String filename = Utils.getLogFilename() + ".txt";
                final File log = saveLog(filename, show.getText().toString());
                Snackbar.make(container, "文件已保存至 HttpTools/log/" + Utils.getLogFilename(), Snackbar.LENGTH_LONG)
                        .setAction("打开", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //打开对应的txt文件
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.addCategory("android.intent.category.DEFAULT");
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Uri uri = FileProvider.getUriForFile(context, Constants.FileProviderName, log);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                intent.putExtra(Intent.EXTRA_STREAM, uri);
                                intent.setDataAndType(uri, "application/*");
                                if (intent.resolveActivity(getPackageManager()) != null)
                                    startActivity(Intent.createChooser(intent, log.getName()));
                                else
                                    showToast("无打开文件的相应应用");
                            }
                        })
                        .show();
                break;
            case R.id.menu_setting:
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 保存当前界面数据至txt
     *
     * @param filename
     * @param data
     * @return
     */
    public File saveLog(String filename, String data) {
        try {
            File path = Environment.getExternalStorageDirectory();
            File dir = new File(path, Constants.LogRoot);
            if (!dir.exists())
                dir.mkdirs();

            File plan = new File(dir.getAbsolutePath(), filename);
            FileOutputStream fileOutputStream = new FileOutputStream(plan);
            byte[] bytes = data.getBytes();
            fileOutputStream.write(bytes);
            fileOutputStream.close();
            return plan;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 添加历史记录
        ItemManager.getManager().addItem("urls", urls.toString());
        ItemManager.getManager().addItem("names", names.toString());
        ItemManager.getManager().addItem("values", values.toString());
        // 保存历史记录
        ItemManager.getManager().saveItems();
    }

    long oldTime;

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - oldTime < 3 * 1000) {
            super.onBackPressed();
        } else {
            showSnackBar(container, "再按一次即可退出");
            oldTime = currentTime;
        }
    }

    /**
     * 显示SnackBar
     *
     * @param view
     * @param msg
     */
    public void showSnackBar(View view, String msg) {
        Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * 收起软键盘
     */
    public void collapseSoftInput() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onFileSelection(@NonNull FileChooserDialog dialog, @NonNull File file) {
        paramsValue.setText(file.getName());
        this.file = file;
    }

    @Override
    public void onFileChooserDismissed(@NonNull FileChooserDialog dialog) {

    }
}
