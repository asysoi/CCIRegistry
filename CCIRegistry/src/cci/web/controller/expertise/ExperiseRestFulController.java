package cci.web.controller.expertise;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import cci.web.exception.EnterpriseGetFileException;
import cci.web.exception.EnterpriseReadFileException;

@RestController
public class ExperiseRestFulController {
	private static final Logger LOG=Logger.getLogger(ExperiseRestFulController.class);
	
	/*
	 * -------------------------------------------------------------------------
	 * Return Expertise as file PDF 
	 * -------------------------------------------------------------------------
	 */
	@RequestMapping(value = "v1/expertise/{number}", method = RequestMethod.GET)
	public void getAct(@PathVariable String number, Authentication aut, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) throws IOException, ServletException {

		String filename = number + ".pdf";
		String pdfFilePath = request.getSession().getServletContext().getInitParameter("upload.location");
		String pdffile = pdfFilePath + System.getProperty("file.separator") + "enterprise"
				+ System.getProperty("file.separator") + filename;
		LOG.info("File name expertise: " + pdffile);

		ByteArrayOutputStream output = null;
		try {
			output = new ByteArrayOutputStream();
			File file = new File(pdffile);

			FileInputStream fis = new FileInputStream(file);
			byte[] buffer = new byte[1024];
			int bytesRead;

			while ((bytesRead = fis.read(buffer)) != -1) {
				output.write(buffer, 0, bytesRead);
			}
			fis.close();
		} catch (FileNotFoundException ex) {
			throw (new EnterpriseGetFileException("Акт экспертиз № " + number + " не найден"));
		} catch (IOException e) {
			throw (new EnterpriseReadFileException("Ошибка чтения файла акта экспертиз № " + number + e.getMessage()));
		}
		
		try {
			if (output != null) {
				response.setContentType("application/pdf");
				// response.setHeader("Accept-Ranges", "bytes");
				response.setHeader("Content-Disposition", "attachment; filename=" + filename);
				response.setContentLength(output.size());
				response.getOutputStream().write(output.toByteArray());
				response.getOutputStream().flush();
				response.getOutputStream().close();
				response.flushBuffer();
				output.close();
			} else {
				throw (new EnterpriseReadFileException(
						"Ошибка формирования выходного потока файла акта экспертиз № " + number));
			}
		} catch (Exception ex) {
			throw (new EnterpriseReadFileException(
					"Ошибка формирования выходного потока файла акта экспертиз № " + number));
		}
	}
	
}
