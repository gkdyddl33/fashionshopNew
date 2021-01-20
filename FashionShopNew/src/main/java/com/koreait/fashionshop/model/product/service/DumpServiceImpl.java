package com.koreait.fashionshop.model.product.service;

import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.koreait.fashionshop.admin.controller.AdminProductController;
import com.koreait.fashionshop.model.common.FileManager;
import com.koreait.fashionshop.model.domain.Color;
import com.koreait.fashionshop.model.domain.Product;
import com.koreait.fashionshop.model.domain.Psize;
import com.koreait.fashionshop.model.excel.ProductConverter;
import com.koreait.fashionshop.model.product.repository.ColorDAO;
import com.koreait.fashionshop.model.product.repository.ProductDAO;
import com.koreait.fashionshop.model.product.repository.PsizeDAO;
@Service
public class DumpServiceImpl implements DumpService{
	private static final Logger logger=LoggerFactory.getLogger(AdminProductController.class);
	@Autowired
	private ProductConverter productConverter;
	@Autowired
	private ProductDAO productDAO;
	@Autowired
	private ColorDAO colorDAO;
	@Autowired
	private PsizeDAO psizeDAO;
	
	@Override
	public void regist(String path) {
	      //엑셀을 읽어서 데이터로 넣기
		List<Product> productList = productConverter.convertFromFile(path);
		logger.debug("엑셀파일을 분석하여 나온 결과 리스트 "+productList.size());
		
		for(int i=0;i<productList.size();i++) {
			Product product = productList.get(i);
			productDAO.insert(product);	// product_id가 결정!
			// 따라서 이 라인서부터는 product vo에 product_id가 채워져 있다.
			// 인서트 하자마자 이 시점부터는 product vo에 이미 pk값이 채워져 있는 상태이다.
			
			// 색상넣기 - 하나의 상품에 딸려있는 여러개의 색상을 넣자 그러기 위해서는 product_id가 필요함
			for(Color color :product.getColorList()) {
				// 하나의 상품에 컬러를 여러개 가지고 잇으니깐 - 근데 이건 list가 가지고 있다.
				color.setProduct_id(product.getProduct_id()); 		// 이 시점부터는 컬러가 fk 보유했으므로 색상테이블에 데이터 넣어보자.
				colorDAO.insert(color);
			}
			
			// 사이즈 넣기
			for(Psize psize : product.getPsizeList()) {
				psize.setProduct_id(product.getProduct_id()); // fk 넣어주기
				psizeDAO.insert(psize);				
			}
			
			// 이미 들어간 파일명을  product_id + 확장자조합으로 교체
			product.setFilename(product.getProduct_id()+"."+FileManager.getExtend(product.getFilename()));
			productDAO.update(product);
		}
	}
}
