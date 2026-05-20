package cci.repository.member;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import cci.model.member.BelCCIMember;

public class BelCCIMemberMapper<T> implements RowMapper<BelCCIMember> {
	   	 
	 private static final Logger LOG = Logger
			.getLogger(JDBCMemberDAO.class);
  	 	
	 public BelCCIMember mapRow(ResultSet resultSet, int row) throws SQLException {
		 BelCCIMember member = new BelCCIMember();
		 
		 try {
		    member.setCode_unn(resultSet.getString("code_unn"));
		    member.setName_full_ru(resultSet.getString("Name_full_ru"));
		    member.setNumbercard(resultSet.getInt("numbercard"));
		 } catch (Exception ex ) {
			LOG.error("Mapping certificate error: " + ex.getMessage());
		    throw new SQLException ("Mapping certificate error: " + ex.getMessage());  	 
		 }
			
	    return member;
	  }
}

