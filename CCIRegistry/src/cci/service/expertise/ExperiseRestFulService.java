package cci.service.expertise;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cci.model.Expertise;
import cci.model.member.BelCCIMember;
import cci.repository.expertise.ExpertiseDAO;
import cci.repository.member.MemberDAO;

@Service
public class ExperiseRestFulService {

	@Autowired
	private	ExpertiseDAO dao;

	public Expertise findExpertiseByNumber(String number) throws Exception {
		return dao.findExpertiseByNumber(number);
	}
	
	public int saveExpertise(Expertise expertise) {
		return dao.saveExpertise(expertise);
	}
}
