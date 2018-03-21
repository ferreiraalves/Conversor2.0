import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
//

@WebServlet("/s3")
public class S3Servicelet extends HttpServlet {
	private String bucketName = "bucketconversorsamba";
	private String keyName = "marioza.jpg";
	private String uploadFileName = "C:\\Users\\ferre\\Pictures\\21740635_1124692240997517_6652591090726829763_n.jpg";

	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String uploadFile = request.getParameter("file");

		PrintWriter out = response.getWriter();
		AmazonS3 s3 = new AmazonS3Client(new ProfileCredentialsProvider());

		try {
			System.out.println("Uploading a new object to S3 from a file\n");
			File file = new File(uploadFileName);
			System.out.println("teste");
			PutObjectResult reqResult = s3.putObject(new PutObjectRequest(bucketName,keyName, file));
			System.out.println(reqResult.getETag());
			

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which " + "means your request made it "
					+ "to Amazon S3, but was rejected with an error response" + " for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which " + "means the client encountered "
					+ "an internal error while trying to " + "communicate with S3, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}

		out.println("<html>");
		out.println("<body>");
		out.println("Request realizado com sucesso5");
		out.println("</body>");
		out.println("</html>");

	}

}
