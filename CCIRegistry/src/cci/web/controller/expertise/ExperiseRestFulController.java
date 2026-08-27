package cci.web.controller.expertise;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cci.model.Expertise;
import cci.service.expertise.ExperiseRestFulService;
import cci.web.exception.EnterpriseGetFileException;
import cci.web.exception.EnterpriseReadFileException;

@RestController
public class ExperiseRestFulController {
	private static final Logger LOG=Logger.getLogger(ExperiseRestFulController.class);
	
	@Autowired
    private ExperiseRestFulService service;
	
	/*
	 * -------------------------------------------------------------------------
	 * Return Expertise as file PDF 
	 * -------------------------------------------------------------------------
	 */
	@RequestMapping(value = "v1/expertise/{number}", method = RequestMethod.GET)
	public void getAct(@PathVariable String number, Authentication aut, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) throws IOException, ServletException {
		
		String filename = "";
		try {
			Expertise expertise = service.findExpertiseByNumber(number);
			filename = expertise.getFilename();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		
		// filename = number+".pdf";
		String pdfFilePath = request.getSession().getServletContext().getInitParameter("upload.location");
		String pdffile = pdfFilePath + System.getProperty("file.separator") + "expertise"
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
	
	// -------------------------------------------------
	// Upload file expertise
	// -------------------------------------------------
	 @RequestMapping(value = "v1/expertise/add", method = RequestMethod.POST)
	 public ResponseEntity<String> addExpertise(
	            @RequestParam("number") String number,
	            @RequestParam("file") MultipartFile file,
	            HttpServletRequest request) {

	        if (file.isEmpty()) {
	            return ResponseEntity.badRequest().body("Файл не выбран или пуст.");
	        }

	        // 1. Генерация безопасного имени файла (UUID + .pdf)
	        String filename = UUID.randomUUID().toString() + ".pdf";

	        // 2. Формирование пути согласно вашему требованию
	        String pdfFilePath = request.getSession().getServletContext().getInitParameter("upload.location");
	        String pdffile = pdfFilePath + System.getProperty("file.separator") + "expertise"
	                + System.getProperty("file.separator") + filename;

	        try {
	            // 3. Сохранение файла на сервере
	            File dest = new File(pdffile);
	            // Создаем директорию expertise, если ее не существует
	            if (!dest.getParentFile().exists()) {
	                dest.getParentFile().mkdirs();
	            }
	            file.transferTo(dest);

	            // 4. Создание записи в БД
	            Expertise expertise = new Expertise();
	            expertise.setActnumber(number);
	            expertise.setFilename(filename);
	            
	            service.saveExpertise(expertise); 
	            
	            return ResponseEntity.ok("Акт экспертизы успешно загружен и сохранен.");

	        } catch (IOException e) {
	            e.printStackTrace();
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body("Ошибка при сохранении файла на сервере.");
	        } catch (Exception e) {
	            e.printStackTrace();
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body("Ошибка при сохранении в базу данных.");
	        }
	    }
	
}
