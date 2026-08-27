package cci.repository.expertise;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import cci.model.Expertise;
import cci.model.member.BelCCIMember;

public class ExpertiseMapper<T> implements RowMapper<Expertise> {
	   	 
	 private static final Logger LOG = Logger
			.getLogger(JDBCExpertiseDAO.class);
  	 	
	 public Expertise mapRow(ResultSet resultSet, int row) throws SQLException {
		 Expertise expertise = new Expertise();
		 
		 try {
			 //expertise.setId(resultSet.getInt("id"));
			 expertise.setActnumber(resultSet.getString("actnumber"));
			 expertise.setFilename(resultSet.getString("filename"));
		 } catch (Exception ex ) {
			LOG.error("Mapping certificate error: " + ex.getMessage());
		    throw new SQLException ("Mapping certificate error: " + ex.getMessage());  	 
		 }
			
	    return expertise;
	  }
}

