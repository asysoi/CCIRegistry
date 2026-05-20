package cci.web.controller.member;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import cci.model.member.BelCCIMember;
import cci.web.controller.expertise.ExperiseRestFulController;
import cci.web.exception.BadRequestNumberException;
import cci.web.exception.EnterpriseReadFileException;
import cci.web.exception.NotFoundCertificateException;



@RestController
public class MemberRestFulController {
	    private static final Logger LOG=Logger.getLogger(ExperiseRestFulController.class);
	    
	    @Autowired
	    private cci.service.member.BelCCIMemberRestFulService service;
	    
	    /*
		 * -------------------------------------------------------------------------
		 * Return CCI MEMEBER by UNP  number
		 * -------------------------------------------------------------------------
		 */
		@RequestMapping(value = "v1/membership/{tin}", method = RequestMethod.GET, headers = "Accept=application/json")
        @ResponseStatus(HttpStatus.OK)
		
		public BelCCIMember checkMember(@PathVariable String tin, Authentication aut, HttpServletRequest request,
				HttpServletResponse response, ModelMap model) throws IOException, ServletException {

			LOG.info("Check CCI Member for: " + tin);
			
			if (tin != null && tin.trim().length() == 9) {
				try {
					return service.findMemberByTIN(tin);
				} catch (CannotGetJdbcConnectionException ex) {
					throw(new EnterpriseReadFileException("База данных недоступна: обратитесь к администратору сервиса"));			
				} catch (Exception ex) {
					throw(new EnterpriseReadFileException("Внутренняя ошибка сервиса: " + ex.getMessage()));			
				}
			} else {
				throw(new BadRequestNumberException("Некорректный УНП: длина УНП - 9 символов."));
			}

			

		}
}
