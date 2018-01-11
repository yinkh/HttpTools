package dreamgo.http.tools.http;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class HttpClient {
	private static String headerName;
	private static String headerValue;

	private static OkHttpClient client = new OkHttpClient.Builder()
			.connectTimeout(10000, TimeUnit.MILLISECONDS)//设置10s钟超时
			.addInterceptor(new Interceptor() {
				//拦截器 用于为每个request添加"Authorization", "Token "+token 信息用于服务器区分合法用户
				@Override
				public Response intercept(Chain chain) throws IOException {
					Request request = chain.request();
					if(headerName != null && headerValue != null){
						request = request.newBuilder().addHeader(headerName, headerValue).build();//验证信息
					}
					return chain.proceed(request);
				}
			}).build();

	public static String getHeaderName() {
		return headerName;
	}

	public static void setHeaderName(String headerName) {
		HttpClient.headerName = headerName;
	}

	public  static String  getHeaderValue() {
		return headerValue;
	}

	public  static void  setHeaderValue(String headerValue) {
		HttpClient.headerValue = headerValue;
	}


	public static OkHttpClient  getClient() {
		return client;
	}
}
