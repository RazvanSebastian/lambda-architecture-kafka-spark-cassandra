package com.sparkjobs.model;

import java.io.Serializable;

import com.datastax.driver.mapping.annotations.Column;

public class ActivityByProduct implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String product;

	private Long timestamp;

	private Long pageviewcount;

	private Long addtocartcount;

	private Long purchasecount;

	public ActivityByProduct() {
		super();
	}

	public ActivityByProduct(String product) {
		super();
		this.product = product;
	}

	public ActivityByProduct(String product, Long timestamp, Long pageviewcount, Long addtocartcount,
			Long purchasecount) {
		super();
		this.product = product;
		this.timestamp = timestamp;
		this.pageviewcount = pageviewcount;
		this.addtocartcount = addtocartcount;
		this.purchasecount = purchasecount;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public Long getPageviewcount() {
		return pageviewcount;
	}

	public void setPageviewcount(Long pageviewcount) {
		this.pageviewcount = pageviewcount;
	}

	public Long getAddtocartcount() {
		return addtocartcount;
	}

	public void setAddtocartcount(Long addtocartcount) {
		this.addtocartcount = addtocartcount;
	}

	public Long getPurchasecount() {
		return purchasecount;
	}

	public void setPurchasecount(Long purchasecount) {
		this.purchasecount = purchasecount;
	}

	@Override
	public String toString() {
		return "ActivityByProduct [product=" + product + ", timestamp=" + timestamp + ", pageviewcount=" + pageviewcount
				+ ", addtocartcount=" + addtocartcount + ", purchasecount=" + purchasecount + "]";
	}

}
