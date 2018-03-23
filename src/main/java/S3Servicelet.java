import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

//
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;


//



@WebServlet("/s3")
@MultipartConfig
public class S3Servicelet extends HttpServlet {
	private String  bucketName    = "sambaconversor";
	
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
	    Part filePart = request.getPart("file");						
	    String fileName = getSubmittedFileName(filePart);
	    InputStream fileContent = filePart.getInputStream();	//absorve o foumulario html
		//System.out.println(fileName);
		ObjectMetadata metadata = new ObjectMetadata();			
		metadata.setContentType(filePart.getContentType());
		metadata.setContentLength(filePart.getSize());			
		
		PrintWriter out = response.getWriter();
		
		//AmazonS3 s3 = new AmazonS3Client(new ProfileCredentialsProvider());
		
		
		AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .withRegion(Regions.SA_EAST_1)
                .build();

																	//inicializa o client s3
																	//ponto de melhoria: poderiamos ter usado as Profile e Enviromental credentias
																	//para cada execução específica (local e cloud)
		
        try {
            System.out.println("Uploading a new object to S3 from a file\n"); 


    		s3.putObject(new PutObjectRequest(bucketName, fileName, fileContent,metadata));

            
            String get = "s3://" + bucketName + "/" + fileName,
            	   put = "s3://" + bucketName + "/" + fileName + ".mp4"; 		//monta as urls para requisição do zencoder
            																	//Melhoria: Tratar a st
            //System.out.println("S3 DONE");
            
            zencoderPost(get, put, out);

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
        
        URL signed = null;
        
        GeneratePresignedUrlRequest generatePresignedUrlRequest = 						//gera url pre-assinada para acesso direto ao arquivo no s3
                new GeneratePresignedUrlRequest(bucketName, fileName + ".mp4");
        generatePresignedUrlRequest.setMethod(HttpMethod.GET); // Default.
        generatePresignedUrlRequest.setExpiration(expiration);
        signed = s3.generatePresignedUrl(generatePresignedUrlRequest);
        
        printLoadingPage(out, signed.toString());										//Monta a página de resposta
        																				//Ponto de melhoria: Montar atraves de redirecionamento.
        																				//*Pendente estudo
		
		out.close();
		
	}
	
	public void checkProgress(String id, PrintWriter out ) throws ClientProtocolException, IOException {
		HttpClient httpclient = HttpClients.createDefault();
		HttpGet httpput = new HttpGet("https://app.zencoder.com/api/v2/jobs/" + id +  "/progress");
		httpput.setHeader("Zencoder-Api-Key", "8c7050a93023870d3a3419056c6e00bc");
	    httpput.setHeader("Accept", "application/json");
	    httpput.setHeader("Content-type", "application/json");
	    
	    String state = "";
	    while (!state.equals("finished")) {											//loop que verifica o progresso utilizando API do zencoder
	    																			//ponto de melhoria: utlizar os parametros de notificação para
			HttpResponse response = httpclient.execute(httpput);					//receber a finalização do job
			HttpEntity aux = response.getEntity();
		    
			String responseString = EntityUtils.toString(aux, "UTF-8");
			System.out.println("Resonse String" + responseString);
			
			JSONObject jsonObj = new JSONObject(responseString);
			state = jsonObj.get("state").toString();
			//System.out.println(state);
	    	try {
				Thread.sleep(2000);
				out.flush();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    }
	    try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	public void zencoderPost(String getURL, String putURL, PrintWriter out) throws ClientProtocolException, IOException {
		HttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost("https://app.zencoder.com/api/v2/jobs");
		
		String json = "{ "											//monta o json para envio de requisicao
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
		    

		//Execute and get the response.
		HttpResponse response = httpclient.execute(httppost);		//recebe e trata a resposta do zencoder para recebermos o job id
		HttpEntity aux = response.getEntity();					
		String responseString = EntityUtils.toString(aux, "UTF-8");
		
		JSONObject jsonObj = new JSONObject(responseString);
		String id = jsonObj.get("id").toString();
		System.out.println("Job id:" + id);

		checkProgress(id, out);								//metodo para aguardar o término da conversão
		
	}
	
	
	private static String getSubmittedFileName(Part part) {
	    for (String cd : part.getHeader("content-disposition").split(";")) {
	        if (cd.trim().startsWith("filename")) {
	            String fileName = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
	            return fileName.substring(fileName.lastIndexOf('/') + 1).substring(fileName.lastIndexOf('\\') + 1); // MSIE fix.
	        }
	    }
	    return null;
	}
	
	
	private void printLoadingPage(PrintWriter out, String signed) {
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("<head>");
		out.println("  <meta charset=\"utf-8\">");
		out.println("  <link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css\" integrity=\"sha384-Gn5384xqQ1aoWXA+058RXPxPg6fy4IWvTNh0E263XmFcJlSAwiGgFAW/dAiS6JXm\" crossorigin=\"anonymous\">");
		out.println("  <style>");
		out.println("    .centeredColumn {");
		out.println("    display: flex;");
		out.println("    flex-direction: column;");
		out.println("    align-items: center;");
		out.println("    justify-content: center;");
		out.println("  }");
		out.println("  a {");
		out.println("    padding: 0px 5px;");
		out.println("    text-decoration: none;");
		out.println("  }");
		out.println("  .content {");
		out.println("    width: 80vw;");
		out.println("    margin-left: 10vw;");
		out.println("    min-height: calc(100vh - 124px);");
		out.println("  }");
		out.println("  .submitButton {");
		out.println("    display: flex;");
		out.println("    align-items: center;");
		out.println("    justify-content: flex-end;");
		out.println("  }");
		out.println("  </style>");
		out.println("  <title>Concluido</title>");
		out.println("</head>");
		out.println("<body>");
		out.println("  <div class=\"centeredColumn\">");
		out.println("    <img src=\"https://sambatech.com/wp-content/themes/tema-sambatech/home-samba-play-2017/img/logo-sambatech-rodape.png\" alt=\"logo\">");
		out.println("    <div class=\"\">");
		out.println("      <a href=\"/\">Home</a>");
		out.println("      <a href=\"/result\">Catalogo</a>");
		out.println("    </div>");
		out.println("  </div>");
		out.println("  <div class=\"content centeredColumn\">");
		out.println("    <div class=\"jumbotron\">");
		out.println("        <video width=\"640\" height=\"360\" controls>");
		out.println("             <source src=\""+ signed + "\" type=\"video/mp4\">") ;
		out.println("        </video>");
		out.println("    </div>");
		out.println("  </div>");
		out.println("  </body>");
		out.println(" </html>");
	}

}
