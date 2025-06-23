package tw.com.ispan.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "product")
public class ProductBean {
	// @JsonIgnoreProperties("product")
	@JsonBackReference("product")
	@OneToOne(
			cascade = CascadeType.ALL,
			mappedBy = "product"
	)
	private DetailBean detail;
	public DetailBean getDetail() {
		return detail;
	}
	public void setDetail(DetailBean detail) {
		this.detail = detail;
	}
	
	@Id
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "name")
	private String name;
	
	@Column(name = "price")
	private Double price;

	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Taipei")
	@Column(name = "make")
	private java.util.Date make;
	
	@Column(name = "expire")
	private Integer expire;
	@Override
	public String toString() {
		return "ProductBean [id=" + id + ", name=" + name + ", price=" + price + ", make=" + make + ", expire=" + expire
				+ "]";
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public java.util.Date getMake() {
		return make;
	}
	public void setMake(java.util.Date make) {
		this.make = make;
	}
	public Integer getExpire() {
		return expire;
	}
	public void setExpire(Integer expire) {
		this.expire = expire;
	}
}
