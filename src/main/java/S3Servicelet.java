import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

//
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;


//



@WebServlet("/s3")
public class S3Servicelet extends HttpServlet {
	private String  bucketName    = "bucketconversorsamba";
	private String sourceKeyName  = "videosamba.dv";
	private String destKeyName    = "videosamba.mp4";	
	private String uploadFileName = "C:\\Users\\lsi\\Downloads\\testvideo.m4v";
	
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
//		System.out.println(java.util.Arrays.asList(request.getParameterNames()));
//		Enumeration<String> params = request.getParameterNames();
//		while(params.hasMoreElements())
//			System.out.println(params.nextElement());
//		String uploadFile = request.getParameter("file");
//		System.out.println(request.getParameter("nome"));
//		System.out.println("Resultado Arquivo:");
//		System.out.println(uploadFile);

		URL signed = null;
	
		PrintWriter out = response.getWriter();
		AmazonS3 s3 = new AmazonS3Client(new ProfileCredentialsProvider());
		
        try {
            System.out.println("Uploading a new object to S3 from a file\n");
            File file = new File(uploadFileName);
//            System.out.println("teste");
            
//          out.println("<html>");
//    		out.println("<body>");
//    		out.println("Request realizado com sucesso");
//    		out.println("Loading...");
//    		out.println("</body>");
//    		out.println("</html>");
    		
    		out.flush();
            
            s3.putObject(new PutObjectRequest(bucketName, sourceKeyName, file));
            
//            java.util.Date expiration = new java.util.Date();
//            long msec = expiration.getTime();
//            msec += 1000 * 60 * 60; // 1 hour.
//            expiration.setTime(msec);
//         
//            GeneratePresignedUrlRequest generatePresignedUrlRequest = 
//                    new GeneratePresignedUrlRequest(bucketName, destKeyName);
//            generatePresignedUrlRequest.setMethod(HttpMethod.GET); // Default.
//            generatePresignedUrlRequest.setExpiration(expiration);
                   
            
            
            String get = "s3://" + bucketName + "/" + sourceKeyName,
            	   put = "s3://" + bucketName + "/" + destKeyName;
            
            System.out.println("S3 DONE");
            
            zencoderPost(get, put);

         } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which " +
            		"means your request made it " +
                    "to Amazon S3, but was rejected with an error response" +
                    " for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which " +
            		"means the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
        
        java.util.Date expiration = new java.util.Date();
        long msec = expiration.getTime();
        msec += 1000 * 60 * 60; // 1 hour.
        expiration.setTime(msec);
     
        GeneratePresignedUrlRequest generatePresignedUrlRequest = 
                new GeneratePresignedUrlRequest(bucketName, destKeyName);
        generatePresignedUrlRequest.setMethod(HttpMethod.GET); // Default.
        generatePresignedUrlRequest.setExpiration(expiration);
        signed = s3.generatePresignedUrl(generatePresignedUrlRequest);
        
		out.println("<html>");
		out.println("<body>");
        out.println("<video width=\"320\" height=\"240\" controls>");
        out.println("<source src=\""+ signed + "\" type=\"video/mp4\">") ;
        out.println("</video>"); 
		out.println("</body>");
		out.println("</html>");
		
		out.close();
		
	}
	
	public void checkProgress(String id) throws ClientProtocolException, IOException {
		HttpClient httpclient = HttpClients.createDefault();
		HttpGet httpput = new HttpGet("https://app.zencoder.com/api/v2/jobs/" + id +  "/progress");
		//HttpGet httpput = new HttpGet("https://app.zencoder.com/api/v2/jobs/" + "466012898" +  "/progress");
		httpput.setHeader("Zencoder-Api-Key", "8c7050a93023870d3a3419056c6e00bc");
	    httpput.setHeader("Accept", "application/json");
	    httpput.setHeader("Content-type", "application/json");
	    
	    String state = "";
//	    GET /api/v2/jobs/465971066/progress HTTP/1.1
//	    Accept: application/json
//	    Content-Type: application/json
//	    Zencoder-Api-Key: 8c7050a93023870d3a3419056c6e00bc
	    while (!state.equals("finished")) {
			HttpResponse response = httpclient.execute(httpput);
			HttpEntity aux = response.getEntity();
		    
			String responseString = EntityUtils.toString(aux, "UTF-8");
			System.out.println(responseString);
			
			JSONObject jsonObj = new JSONObject(responseString);
			state = jsonObj.get("state").toString();
			System.out.println(state);
	    	try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    }
	    System.out.println("JOBS DONE");
	    try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    System.out.println("Final countdown");
	}
	
	
	public void zencoderPost(String getURL, String putURL) throws ClientProtocolException, IOException {
		HttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost("https://app.zencoder.com/api/v2/jobs");
		
		String json = "{ "
				+ "\"input\": \""+ getURL +"\","
				+ "\"outputs\": [ { "
					+ "\"label\": \"mp4 high\","
					+ "\"url\": \""+ putURL +"\","
					+ "\"credentials\": \"s3\","
					+ "\"h264_profile\": \"high\" }"
				+ " ] } ";
		
	    StringEntity entity = new StringEntity(json);
	    httppost.setEntity(entity);
	    httppost.setHeader("Accept", "application/json");
	    httppost.setHeader("Content-type", "application/json");
	    httppost.setHeader("Zencoder-Api-Key", "8c7050a93023870d3a3419056c6e00bc");
		
	    //CloseableHttpResponse response = client.execute(httpPost);
	    //assertThat(response.getStatusLine().getStatusCode(), equalTo(200));
	    //client.close();	    

		//Execute and get the response.
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity aux = response.getEntity();
		String responseString = EntityUtils.toString(aux, "UTF-8");
		
		JSONObject jsonObj = new JSONObject(responseString);
		String id = jsonObj.get("id").toString();
		System.out.println(id);
		/*HttpEntity entity = response.getEntity();*/
		checkProgress(id);		
		

	}
	
	

}
