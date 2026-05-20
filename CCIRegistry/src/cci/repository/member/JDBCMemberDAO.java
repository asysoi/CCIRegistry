package cci.repository.member;


import javax.sql.DataSource;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import cci.model.member.BelCCIMember;
import cci.web.exception.BadRequestNumberException;
import cci.web.exception.EnterpriseReadFileException;

@Repository
public class JDBCMemberDAO implements MemberDAO {

	private static final Logger LOG = Logger
			.getLogger(JDBCMemberDAO.class);
	
	private NamedParameterJdbcTemplate template;

	@Autowired
	public void setDataSource(DataSource dataSource) {
		this.template = new NamedParameterJdbcTemplate(dataSource);
	}

			
	/* ---------------------------------------------------------------
	 * 
	 * Find Member BELCCI by TIN
	 * 
	 *  --------------------------------------------------------------- */
	public BelCCIMember findMemberByTIN(String tin)  {

		String sql = "select * from enterprises WHERE code_unn = ? and numbercard > 0"; 
		LOG.info("findMemeberCCIByTIN TIN parameneter: " + tin.trim()) ;
		BelCCIMember member = null;
		
		try {		
		    member = template.getJdbcOperations()
				.queryForObject(
						sql,
						new Object[] { tin.trim()},
						new BelCCIMemberMapper<BelCCIMember>());  	
			LOG.info("findMemberByTIN: result found") ;
		} catch (EmptyResultDataAccessException ex) {
			member  = new BelCCIMember();
			member.setCode_unn(tin);
			member.setName_full_ru("n/a");
			member.setNumbercard(0);
		} catch (Exception ex) {
			LOG.info("findMemberByTIN: Error - " + ex.getMessage()) ;
			throw ex;
			// throw(new EnterpriseReadFileException(ex.getMessage()));
		}
		return member;	
	}
	
	
}
