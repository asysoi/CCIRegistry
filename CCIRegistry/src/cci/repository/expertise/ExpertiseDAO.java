package cci.repository.expertise;

import cci.model.Expertise;

public interface ExpertiseDAO {
	
	public int saveExpertise(Expertise expertise);
	public Expertise findExpertiseByNumber(String number);
	
}
