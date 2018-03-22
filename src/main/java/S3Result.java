import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
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
		AmazonS3 s3 = new AmazonS3Client(new ProfileCredentialsProvider());
		System.out.println("teste1");
		
        java.util.Date expiration = new java.util.Date();
        long msec = expiration.getTime();
        msec += 1000 * 60 * 60; // 1 hour.
        expiration.setTime(msec);
		
        URL signed = null;
        
		out.println("<html>");
		out.println("<body>");
		out.println("</body>");
		out.println("</html>");
        
        ObjectListing objectListing = s3.listObjects(new ListObjectsRequest()
                .withBucketName(bucketName));
        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
        	if (objectSummary.getKey().contains(".mp4")) {
            	System.out.println(" - " + objectSummary.getKey() + "  " +
                        "(size = " + objectSummary.getSize() + ")");
            	
                GeneratePresignedUrlRequest generatePresignedUrlRequest = 
                        new GeneratePresignedUrlRequest(bucketName, objectSummary.getKey());
                generatePresignedUrlRequest.setMethod(HttpMethod.GET); // Default.
                generatePresignedUrlRequest.setExpiration(expiration);
                signed = s3.generatePresignedUrl(generatePresignedUrlRequest);
                //out.println("<iframe src=\"" + signed.toString() + "?rel=0\" style=\"border:none\"></iframe>");
                out.println("<video width=\"320\" height=\"240\" controls>");
                out.println("<source src=\""+ signed + "\" type=\"video/mp4\">") ;
                out.println("</video>"); 
                out.println("<br>");
        	}        	
        }
        
		out.println("</body>");
		out.println("</html>");
        
        
        System.out.println("teste2");
		out.println("<html>");
		out.println("<body>");
		out.println("Done");
		out.println("</body>");
		out.println("</html>");
		
		
	}
	
}
