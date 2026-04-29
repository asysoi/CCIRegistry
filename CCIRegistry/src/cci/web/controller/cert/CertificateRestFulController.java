package cci.web.controller.cert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import cci.model.cert.Certificate;
import cci.model.owncert.OwnCertificate;
import cci.pdfbuilder.cert.CertificatePDFBuilder;
import cci.service.CountryConverter;
import cci.service.FieldType;
import cci.service.cert.CertService;
import cci.service.cert.CertificateRestFulService;
import cci.service.search.FullTextIndexService;
import cci.service.search.SearchResult;
import cci.web.controller.owncert.ViewOwnCertificateJSPHelper;
import cci.web.exception.BadRequestNumberException;
import cci.web.exception.CertificateGetFileException;
import cci.web.exception.NotFoundCertificateException;
import cci.web.validator.cert.CertificateValidator;

@Controller
@RestController
public class CertificateRestFulController {

	private static final Logger LOG=Logger.getLogger(CertificateRestFulController.class);
	
	@Autowired
	private CertificateRestFulService restservice;
	@Autowired
    private CertService certService;
	@Autowired
    private FullTextIndexService indexService;

	
	/* -----------------------------
	 * Get certificate by number (blanknumber & date are optional)
	 * ----------------------------- */
	@RequestMapping(value = "v1/certificate/origin/test", method = RequestMethod.GET, headers = "Accept=application/json")
	@ResponseStatus(HttpStatus.OK)
	public String checkCertificateAvailable (
			Authentication aut) {
		if (restservice.checkRegistryAvailable()) {
		   return "{\"Registry origin\": \"OK\"}";
		} else {
		   return "{\"Registry origin\": \"BAD\"}"; 
		}
	}
	
	/* -----------------------------
	 * Get certificate by number (blanknumber & date are optional)
	 * ----------------------------- */
	@RequestMapping(value = "v1/certificate/origin/{number}", method = RequestMethod.GET, headers = "Accept=application/json")
	@ResponseStatus(HttpStatus.OK)
	public Certificate getCertificateByNumber(
			@PathVariable String number, Authentication aut) {

		Certificate rcert = null;
		String blanknumber = null;
		String date = null;

		if (number != null && number.trim().length() == 14) {
			try {
				rcert = restservice.getCertificateByNumber(number, blanknumber, date);
			} catch (Exception ex) {
				throw (new NotFoundCertificateException(
						"Сертификат происхождения товаров номер " + number + " не найден."));
			}
		} else {
			throw (new BadRequestNumberException("Длина номера сертификата не равна 14 символам."));
		}
		return rcert;
	}

	// ---------------------------------------------------------------------------------------
	//
	// ---------------------------------------------------------------------------------------
	@RequestMapping(value = "v1/certificate/origin/doc/{number}", method = RequestMethod.GET)
	public void gocert(
			//@RequestParam(value = "number", required = true) String number,
			@PathVariable String number,
	        Authentication aut,
			HttpServletRequest request, HttpServletResponse response, ModelMap model)
			throws IOException, ServletException {

		String relativeWebPath = "/resources";
		String absoluteDiskPath = request.getSession().getServletContext().getRealPath(relativeWebPath);
		LOG.debug("Absolute path: " + absoluteDiskPath);
		
		Certificate cert = null;
		if (number != null && number.trim().length() == 14) {
			try {
				cert = restservice.getCertificateByNumber(number, null, null);
			} catch (Exception ex) {
				throw (new NotFoundCertificateException(
						"Сертификат происхождения товаров номер " + number + " не найден."));
			}
		} else {
			throw (new BadRequestNumberException("Длина номера сертификата не равна 14 символам."));
		}
		
		String filename = number + ".pdf";
		if (cert != null) { 
		    LOG.info("GoTo make file: ");
			ByteArrayOutputStream output = makepdffile(absoluteDiskPath, cert);;
			LOG.info("After make file: ");
			
			if (output != null) {
				response.setContentType("application/pdf");
				// response.setHeader("Accept-Ranges", "bytes");
				response.setHeader("Content-Disposition", "attachment; filename=" + filename);
				// response.setHeader("Content-Disposition", "attachment; filename=" + tempname);
				response.setContentLength(output.size());
				response.getOutputStream().write(output.toByteArray());
				response.getOutputStream().flush();
				response.getOutputStream().close();
				response.flushBuffer();
			    output.close();
			    
			} else {
				LOG.info("Exception PDF create");
				throw (new CertificateGetFileException ("Ошибка формирования копии сертификата происхождения товара " + number  ));
			}
		} else {
			LOG.debug("Certificate is null");
			throw (new NotFoundCertificateException("Серитификат происхождения товара номер " + number + " не найден. Проверьте номер сертификата." ));
		}
	}
		
	// ---------------------------------------------------------------------------------------
	//
	// ---------------------------------------------------------------------------------------
	private ByteArrayOutputStream makepdffile(String absoluteDiskPath, Certificate cert) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		CertificatePDFBuilder builder = new CertificatePDFBuilder();
		String fout = absoluteDiskPath + "/out/" + cert.getCert_id() + ".pdf";
		String fconf = absoluteDiskPath + "/config/pages.xml";
		String fpath = absoluteDiskPath + "/fonts/";

		CountryConverter.setCountrymap(certService.getCountriesList("RU"));

		try {
			output = builder.createPdfOutput(cert, fconf, fpath);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return output;
	}

	
	/* -----------------------------
	 * Exception handling 
	 * ----------------------------- */
	/*@ExceptionHandler(Exception.class)
	@ResponseBody
    public ResponseEntity<String> handleRESTException(Exception ex) {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Error Name", ex.getClass().getName());
		responseHeaders.set("Content-Type", "application/json;charset=utf-8");
		return new ResponseEntity<String>(ex.toString(), responseHeaders,  HttpStatus.BAD_REQUEST);
    }*/


	
	
}
