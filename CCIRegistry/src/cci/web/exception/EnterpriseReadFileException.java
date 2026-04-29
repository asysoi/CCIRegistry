package cci.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus (HttpStatus.BAD_REQUEST)
public class EnterpriseReadFileException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public  EnterpriseReadFileException(String err) {
		   super(err);
    }
}
