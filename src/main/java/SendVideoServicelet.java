import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;

@WebServlet("/sendFile")
public class SendVideoServicelet extends HttpServlet{
	

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		// busca o writer
		PrintWriter out = response.getWriter();
		
		try {
			String arquivo = request.getParameter("file");
			System.out.println(arquivo);
		} catch (ParseException e) {
			out.println("Erro de conversão da data");
			return; //para a execução do método
		}
		
		
		
		
		out.println("<html>");
		out.println("<body>");
		out.println("Request realizado com sucesso");
		out.println("<a href=\"http://localhost:8080/Conversor\">HOME</a>");
		out.println("</body>");
		out.println("</html>");
		
	}
	
}
