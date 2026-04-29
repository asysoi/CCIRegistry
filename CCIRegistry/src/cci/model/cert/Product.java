package cci.model.cert;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@XmlRootElement(name="row")
@XmlType(propOrder = {"numerator", "package", "tovar", "kriter", "ves", "schet", "fobvalue"})
@JsonPropertyOrder({"number","product","package", "criterion", "weight", "invoice"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Component
public class Product {
	@JsonIgnore
	private Long product_id;
	@JsonProperty("number")
	private String numerator;
	@JsonProperty("product")
	private String tovar;
	@JsonProperty("package")
	private String vidup;
	@JsonProperty("criterion")
	private String kriter;
	@JsonProperty("weight")
	private String ves;
	@JsonProperty("invoice")
	private String schet;
	@JsonIgnore
	private String fobvalue;
	
	@XmlTransient
	@JsonIgnore
	public Long getProduct_id() {
		return product_id;
	}
	public void setProduct_id(Long product_id) {
		this.product_id = product_id;
	}
		
	public String getNumerator() {
		return numerator;
	}
	public void setNumerator(String numerator) {
		this.numerator = numerator;
	}
	
	public String getTovar() {
		return tovar;
	}
	public void setTovar(String tovar) {
		this.tovar = tovar;
	}
	public String getVidup() {
		return vidup;
	}
	public void setVidup(String vidup) {
		this.vidup = vidup;
	}
	public String getKriter() {
		return kriter;
	}
	public void setKriter(String kriter) {
		this.kriter = kriter;
	}
	public String getVes() {
		return ves;
	}
	public void setVes(String ves) {
		this.ves = ves;
	}
	public String getSchet() {
		return schet;
	}
	public void setSchet(String schet) {
		this.schet = schet;
	}

	public String getFobvalue() {
		return fobvalue;
	}
	public void setFobvalue(String fobvalue) {
		this.fobvalue = fobvalue;
	}
}
