import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

@WebServlet("/result")
public class S3Result extends HttpServlet {
	private String  bucketName    = "bucketconversorsamba";
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		//AmazonS3 s3 = new AmazonS3Client(new ProfileCredentialsProvider());
		AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new EnvironmentVariableCredentialsProvider())
                .build();
        
        ObjectListing listing = s3.listObjects(new ListObjectsRequest()
                .withBucketName(bucketName));
        
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
		out.println("  <title>HOME</title>");
		out.println("</head>");
		out.println("<body>");
		out.println("  <div class=\"centeredColumn\">");		
		out.println("    <img src=\"https://sambatech.com/wp-content/themes/tema-sambatech/home-samba-play-2017/img/logo-sambatech-rodape.png\" alt=\"logo\">");
		out.println("    <div class=\"\">");
		out.println("      <a href=\"/\">Home</a>");
		out.println("      <a href=\"/result\">Catalogo</a>");
		out.println("    </div>");
		out.println("  </div>");
		out.println("  	<h1 id=\"title\" class=\"display-4\" style=\"text-align: center;\">Envios recentes</h1>");
		out.println("  <div class=\"content centeredColumn\">");
		
		java.util.Date expiration = new java.util.Date();
        long msec = expiration.getTime();
        msec += 1000 * 60 * 60; // 1 hour.
        expiration.setTime(msec);
        URL signed = null;
        
		for (S3ObjectSummary objectSummary : listing.getObjectSummaries()) {
        	if (objectSummary.getKey().contains(".mp4")) {            	
                GeneratePresignedUrlRequest generatePresignedUrlRequest = 
                        new GeneratePresignedUrlRequest(bucketName, objectSummary.getKey());
                generatePresignedUrlRequest.setMethod(HttpMethod.GET); // Default.
                generatePresignedUrlRequest.setExpiration(expiration);
                
                signed = s3.generatePresignedUrl(generatePresignedUrlRequest);
                out.println("<div class=\"jumbotron\">");
                //out.println("<iframe src=\"" + signed.toString() + "?rel=0\" style=\"border:none\"></iframe>");
                out.println("	<video width=\"640\" height=\"360\" controls>");
                out.println("		<source src=\""+ signed + "\" type=\"video/mp4\">") ;
                out.println("	</video>"); 
                out.println("</div>");
        	}        	
        }
		
		out.println("  </div>");
		out.println("  </body>");
		out.println(" </html>");
		out.close();
	}

	
}
