package cci.model.member;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlType(propOrder = {"code_unn", ", numbercard", "name_full_ru"})
@JsonInclude(JsonInclude.Include.NON_NULL)

public class BelCCIMember {
	@JsonProperty("TIN")
	private String code_unn;
	
	@JsonProperty("OrgName")
	private String name_full_ru;
	
	@JsonIgnore
	private Integer numbercard;
	@JsonProperty("BelCCIMember")
	private boolean status;
		
	public String getName_full_ru() {
		return name_full_ru;
	}
	public void setName_full_ru(String name_full_ru) {
		this.name_full_ru = name_full_ru;
	}
	
	public boolean getStatus() {
		return this.numbercard > 0 ? true : false;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public Integer getNumbercard() {
		return numbercard;
	}
	public void setNumbercard(Integer numbercard) {
		this.numbercard = numbercard;
	}
	public String getCode_unn() {
		return code_unn;
	}
	public void setCode_unn(String code_unn) {
		this.code_unn = code_unn;
	}
	
}
