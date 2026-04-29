package cci.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus (HttpStatus.BAD_REQUEST)
public class CertificateGetException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private String error;
    
	public  CertificateGetException(String err) {
		   super(err);
    }

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
	
	
}
