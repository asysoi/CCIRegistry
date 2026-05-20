package cci.service.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cci.model.member.BelCCIMember;
import cci.repository.member.MemberDAO;

@Service
public class BelCCIMemberRestFulService {

	@Autowired
	private	MemberDAO memberDAO;

	public BelCCIMember findMemberByTIN(String tin) throws Exception {
		return memberDAO.findMemberByTIN(tin);
	}
}
