package cci.repository.expertise;


import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import cci.model.Expertise;
import cci.model.member.BelCCIMember;
import cci.web.exception.BadRequestNumberException;
import cci.web.exception.EnterpriseReadFileException;

@Repository
public class JDBCExpertiseDAO implements ExpertiseDAO {

	private static final Logger LOG = Logger
			.getLogger(JDBCExpertiseDAO.class);
	
	private NamedParameterJdbcTemplate template;

	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.template = new NamedParameterJdbcTemplate(dataSource);
	}

			
	/* ---------------------------------------------------------------
	 * 
	 * 
	 * 
	 *  --------------------------------------------------------------- */
	public Expertise findExpertiseByNumber(String number)  {

		String sql = "select * from expertise where actnumber = ?"; 
		Expertise experise = null;
		
		try {		
			experise = template.getJdbcOperations()
				.queryForObject(
						sql,
						new Object[] { number.trim()},
						new ExpertiseMapper<Expertise>());  	
			LOG.info("Expertise: result found") ;
		} catch (EmptyResultDataAccessException ex) {
			experise  = new Expertise();
		} catch (Exception ex) {
			LOG.info("Experise find: Error - " + ex.getMessage()) ;
			throw ex;
			// throw(new EnterpriseReadFileException(ex.getMessage()));
		}
		return experise;	
	}


	/* ---------------------------------------------------------------
	 * 
	 * 
	 * 
	 *  --------------------------------------------------------------- */
	public int saveExpertise(Expertise expertise) {
		String sql = "insert into expertise values (:actnumber, :filename)";
	    int row = 0;
	    
	    Map<String, Object> parameters = new HashMap<String, Object>();
	    parameters.put("actnumber", expertise.getActnumber());
	    parameters.put("filename", expertise.getFilename());

	    try {
	    	row = template.update(sql, parameters);
	    } catch (Exception ex) {
	    	LOG.error("Eroor source info saving during certificate adding: " + ex.getMessage());
	    }
	    return row;
   }
	
}
