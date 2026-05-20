package cci.repository.member;

import cci.model.member.BelCCIMember;

public interface MemberDAO {
	
	public BelCCIMember findMemberByTIN(String tin);
	
}
