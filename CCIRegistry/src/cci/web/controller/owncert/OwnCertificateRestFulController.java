package cci.web.controller.owncert;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import cci.model.owncert.OwnCertificate;
import cci.model.owncert.OwnCertificateHeaders;
import cci.model.owncert.OwnCertificates;
import cci.pdfbuilder.PDFUtils;
import cci.service.owncert.OwnCertificateService;
import cci.service.utils.XMLService;
import cci.web.exception.BadRequestNumberException;
import cci.web.exception.CertificateGetFileException;
import cci.web.exception.CertificateUpdateException;
import cci.web.exception.CheckOwnCertificateException;
import cci.web.exception.NotFoundCertificateException;
import cci.web.exception.NotUpdatedOwnCertificateFileNameException;

@RestController
public class OwnCertificateRestFulController {
	private static final Logger LOG=Logger.getLogger(OwnCertificateRestFulController.class);
	private String relativeWebPath = "/resources/in";
	
	@Autowired
	private PDFUtils pdfutils;
	@Autowired
	private cci.service.owncert.OwnCertificateService service;
	@Autowired
	private XMLService xmlService;

	/* ---------------------------------------------------
	 * Get certificate by Number Return JSON
	 * ------------------------------------------------- */
	@RequestMapping(value = "v1/certificate/own/{number}", method = RequestMethod.GET, 
			headers = "Accept=application/json")
		
	@ResponseStatus(HttpStatus.OK)
	public OwnCertificate getOwnCertificateByNumber(
			//@RequestParam(value = "number", required = true) String number,
			@PathVariable String number,
			Authentication aut)  {
		
		if (number != null && number.trim().length() == 14 ) {
			try {
				return service.getOwnCertificateByNumber(number, null, null);
			} catch (Exception ex) {
				throw(new NotFoundCertificateException("Сертификат собственного производства номер " + number + " не найден."));			
			}
		} else {
			throw(new BadRequestNumberException("Номер сертификата не соответствует требованиям длины в 14 символов."));
		}
	}
		
	/*
	 * -------------------------------------------------------------------------
	 * Return Certificate  as file  (PDF or TXT)
	 * -------------------------------------------------------------------------
	 */
	@RequestMapping(value = "v1/certificate/own/doc/{number}", method = RequestMethod.GET)
	public void gocert(
			          @PathVariable String number,
			          Authentication aut,
			          HttpServletRequest request, 
			          HttpServletResponse response, 
			          ModelMap model) throws IOException, ServletException {
		
		OwnCertificate owncert = null;
		
		if (number != null && number.trim().length() == 14 ) {
			try {
				owncert = service.getOwnCertificateByNumber(number, null, null);
			} catch (Exception ex) {
				throw(new NotFoundCertificateException("Сертификат собственного производства номер " + number + " не найден."));			
			}
		} else {
			throw(new BadRequestNumberException("Длина номера сертификата не равна 14 символам."));
		}
		
		String filename = number + ".pdf";
		
		if (owncert != null && owncert.getFilename() != null && !owncert.getFilename().isEmpty()) {
			try {
				LOG.info("File name: " + owncert.getFilename());
				String templateDiskPath = request.getSession().getServletContext().getRealPath(relativeWebPath);
				String pdfFilePath = request.getSession().getServletContext().getInitParameter("upload.location");
				String checksize = request.getSession().getServletContext().getInitParameter("pdfimage.checksize");
		
				String pagefirst = null;
				String pagenext = null;

				if ("с/п".equals(owncert.getType())) {
					pagefirst = templateDiskPath + System.getProperty("file.separator") + "ownproductfirst.pdf";
					pagenext = templateDiskPath + System.getProperty("file.separator") + "ownproductnext.pdf";
				} else if ("р/у".equals(owncert.getType())) {
					pagefirst = templateDiskPath + System.getProperty("file.separator") + "ownservicefirst.pdf";
					pagenext = templateDiskPath + System.getProperty("file.separator") + "ownservicenext.pdf";
				} else if ("б/у".equals(owncert.getType())) {
					pagefirst = templateDiskPath + System.getProperty("file.separator") + "ownbankfirst.pdf";
					pagenext = templateDiskPath + System.getProperty("file.separator") + "ownbanknext.pdf";
				} else {
					throw new CertificateGetFileException("Для данного типа сертификата не определены формы бланков. Нарушение целостности данных.");
				}

				String pdffile = pdfFilePath + System.getProperty("file.separator") + owncert.getFilename();
				List<String> numbers = service.splitOwnCertNumbers(owncert.getBlanknumber(),
						owncert.getAdditionalblanks());
				LOG.info("GoTo make file: " + pdffile);
				ByteArrayOutputStream output = pdfutils.mergePdf(pdffile, pagefirst, pagenext, numbers, Integer.parseInt(checksize));
				LOG.info("After make file: " + pdffile);
				
				if (output != null) {
					response.setContentType("application/pdf");
					//response.setHeader("Accept-Ranges", "bytes");
					response.setHeader("Content-Disposition", "attachment; filename=" + filename);
					response.setContentLength(output.size());
					response.getOutputStream().write(output.toByteArray());
					response.getOutputStream().flush();
					response.getOutputStream().close();
					response.flushBuffer();
				    output.close();
				} else {
					throw (new CertificateGetFileException ("Ошибка формирования копии сертификата собственного производства № " + number  ));
				}
			} catch (Exception ex) {
				LOG.info("Go to make copy reproduction");
				
				ByteArrayOutputStream output = pdfutils.createPDF(new ViewOwnCertificateJSPHelper(owncert)); 
				if (output != null) {
					response.setContentType("application/pdf");
					//response.setHeader("Accept-Ranges", "bytes");
					response.setHeader("Content-Disposition", "attacment; filename=" + filename);
					response.setContentLength(output.size());
					response.getOutputStream().write(output.toByteArray());
					response.getOutputStream().flush();
					response.getOutputStream().close();
					response.flushBuffer();
				    output.close();
				} else {
					throw (new CertificateGetFileException ("Ошибка формирования копии сертификата собственного производства № " + number  ));
				}
			}
		} else if (owncert != null) {
			//model.addAttribute("owncert", owncert);
			
			ByteArrayOutputStream output = pdfutils.createPDF(new ViewOwnCertificateJSPHelper(owncert)); 
			if (output != null) {
				response.setContentType("application/pdf");
				//response.setHeader("Accept-Ranges", "bytes");
				response.setHeader("Content-Disposition", "attachment; filename=" + filename);
				response.setContentLength(output.size());
				response.getOutputStream().write(output.toByteArray());
				response.getOutputStream().flush();
				response.getOutputStream().close();
				response.flushBuffer();
			    output.close();
			} else {
				throw (new CertificateGetFileException ("Ошибка формирования копии сертификата собственного производства № " + number  ));
			}
		}
	}
	
	
	/* ---------------------------------------------------------------------------------------
	*  Another version returning certificate file in PDF 
	* ---------------------------------------------------------------------------------------*/
	@RequestMapping(value = "rowncertfile.do", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	public void ownCertificateFileDownload (
			@RequestParam(value = "number", required = true) String number,
			@RequestParam(value = "blanknumber", required = false) String blanknumber,
			@RequestParam(value = "datecert", required = false) String datecert,
			Authentication aut,
			HttpServletRequest request,
			HttpServletResponse response, 
			ModelMap model) {
		
		try {
			OwnCertificate owncert = service.getOwnCertificateByNumber(number, blanknumber, datecert);

			if (owncert != null && !owncert.getFilename().trim().isEmpty()) {
				
				response.setContentType("application/pdf");
				response.setHeader("Content-Disposition", "attachment; filename=" + owncert.getFilename());
				
				String templateDiskPath = request.getSession().getServletContext().getRealPath(relativeWebPath);
				String pdfFilePath = request.getSession().getServletContext().getInitParameter("upload.location");
				String checksize = request.getSession().getServletContext().getInitParameter("pdfimage.checksize");
				
				String pagefirst = null;
				String pagenext = null;

				if ("с/п".equals(owncert.getType()) ) {
					pagefirst = templateDiskPath + System.getProperty("file.separator") + "ownproductfirst.pdf";
					pagenext = templateDiskPath + System.getProperty("file.separator") + "ownproductnext.pdf";
				} else if ("р/у".equals(owncert.getType()) ) {
					pagefirst = templateDiskPath + System.getProperty("file.separator") + "ownservicefirst.pdf";
					pagenext = templateDiskPath + System.getProperty("file.separator") + "ownservicenext.pdf";
				} else if ("б/у".equals(owncert.getType()) ) {
					pagefirst = templateDiskPath + System.getProperty("file.separator") + "ownbankfirst.pdf";
					pagenext = templateDiskPath + System.getProperty("file.separator") + "ownbanknext.pdf";
				} else {
					throw new NotFoundCertificateException("Для данного типа сертификата не определены формы бланков.");
				}
								
				String pdffile = pdfFilePath + System.getProperty("file.separator") + owncert.getFilename();
				List<String> numbers = service.splitOwnCertNumbers(owncert.getBlanknumber(), owncert.getAdditionalblanks());
				LOG.info("GoTo merge file: " + pdffile);
				ByteArrayOutputStream  output = pdfutils.mergePdf(pdffile, pagefirst, pagenext, numbers, Integer.parseInt(checksize));
				LOG.info("After merge file: " + pdffile);
				
				if (output != null) { 
				   response.getOutputStream().write(output.toByteArray());
				   output.close();
				} else {
				   throw new NotFoundCertificateException("Ошибка обработки файла печатной формы сертификата.");	
				}
			} else {
				LOG.info("No file name: " + owncert.getFilename());
				throw new NotFoundCertificateException("Сертификат не найден или у сертификата нет печатной формы.");
			}
		} catch (Exception ex) {
			LOG.info("Try catch exception");
			throw new NotFoundCertificateException(ex.getMessage());
		}
	}
	
	
	// ==================================================================================================================================================
	
		/* -----------------------------
		 * Exception handling 
		 * ----------------------------- */
		/*@ExceptionHandler(Exception.class)
		@ResponseBody
	    public ResponseEntity<String> handleIOException(Exception ex) {
			LOG.info("Own Exception handling");			
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.setContentType(MediaType.APPLICATION_JSON);
			//responseHeaders.set("Error-Code", "");
			responseHeaders.set("Content-Type", "application/json;charset=utf-8");
			String msg = ex.toString();
			return new ResponseEntity<String>(msg, responseHeaders,  HttpStatus.NOT_FOUND);
	    }
*/	
	
	// =======================================================================================================================================
	
		
	/* -------------------------------------------------
	 * Upload certificate's file 
	 * ------------------------------------------------- */
	@RequestMapping(value = "rowncertupfile.do", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public String addCertificateAndFileUpload(
			            @RequestParam("number") String number,
			            @RequestParam("blanknumber") String blanknumber,
			            @RequestParam("datecert") String datecert,
						@RequestParam("file") MultipartFile multipartFile,
						HttpServletRequest request,
			            Authentication aut) {
		
		try {
		     String otd_id = service.getOtd_idByRole(aut);
			 datecert = convertDateToMySQLFormat(datecert);
			 
			 OwnCertificate owncert = service.getOwnCertificateByNumber(number, blanknumber, datecert);
		     
			 if  (owncert != null) {
				 LOG.info("Сертификат найден");
				 
				 String fileName = multipartFile.getOriginalFilename();
				 
				 String absoluteDiskPath = request.getSession().getServletContext().getInitParameter("upload.location");
			     fileName = datecert.replaceAll("..\\...\\.", "").trim() + System.getProperty("file.separator") 
			     			+ owncert.getOtd_id().trim() + System.getProperty("file.separator") 
			    		    + (number+"_" + blanknumber).replaceAll("[\\.\\/\\\\]", "_") 
			    		    + "." + FilenameUtils.getExtension(fileName);
			     
				 File certFile = new File(absoluteDiskPath, fileName);
				 LOG.info("File path: " + certFile.getAbsolutePath());
				 multipartFile.transferTo(certFile);

				 if (! service.updateOwnCertificateFileName(number, blanknumber, datecert, fileName)) {
					 throw(new NotUpdatedOwnCertificateFileNameException("Сcылка на загруженный файл не сохранена для сертификата. Ошибка обновление имени файла")); 
				 };
			 } else {
				 LOG.info("Не найдено сертификата для загрузки файла");
				 throw(new NotFoundCertificateException("Не найдено сертификата для загрузки файла"));
			 }
		} catch (EmptyResultDataAccessException ex) {
			LOG.info("Не найдено сертификата для загрузки файла");
			throw(new CertificateUpdateException(" Не найден сертификат с номером: " + number + " на бланке: " + blanknumber + ", выданный  "+ datecert));
		} catch (Exception ex) {
			LOG.info(ex.getLocalizedMessage());
			throw(new CertificateUpdateException(ex.getLocalizedMessage()));
		}
		return "File uploaded and joint to certificate";
    }
	
		
	
	// =====================================================================================================================================================
	
	/* ---------------------------------------------------
	 * Convert date from format dd.mm.yyyy to yyyy-mm-dd  
	 * It return original date now. Modification is ignored.
	 * --------------------------------------------------- */
	private String convertDateToMySQLFormat(String datecert) {
		String retdate = datecert;
		try {
			 retdate = new SimpleDateFormat("yyyy-mm-dd").format(
					   new SimpleDateFormat("dd.mm.yyyy").parse(datecert));
			 LOG.debug("Coverted date: " + retdate);
		} catch (Exception ex) {
			LOG.error("DateStringConverter: " + ex.getMessage());
		}
		return datecert;
	}
	
	
	/* -----------------------------
	 * Check date
	 * ----------------------------- */
	private boolean isDate(String datecert) {   
	    boolean ret = false;
	    
	    if (datecert != null && ! datecert.trim().isEmpty()) {
			try {
				 //LocalDate date = LocalDate.parse(datecert);
				 //date.
				 ret = true;
			} catch (Exception ex) {
				System.out.println("Строка не интерпретируется как дата");
				ret = false; 
			}
	    }
	    return ret;
	}

	

}
