package tw.com.ispan.repository;

import java.util.List;

import org.json.JSONObject;

import tw.com.ispan.domain.ProductBean;

public interface ProductDAO {
	List<ProductBean> find(JSONObject obj);
	long count(JSONObject obj);
}