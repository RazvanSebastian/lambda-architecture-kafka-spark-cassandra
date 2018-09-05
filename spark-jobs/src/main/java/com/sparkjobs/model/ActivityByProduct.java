package com.sparkjobs.model;

import java.io.Serializable;

public class ActivityByProduct implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String product;
	private Long timestamp;
	private Long pageViewCount;
	private Long addToCartCount;
	private Long purchaseCount;

	public ActivityByProduct() {
		super();
	}

	public ActivityByProduct(String product) {
		super();
		this.product = product;
	}

	public ActivityByProduct(String product, Long timestamp, Long pageViewCount, Long addToCartCount,
			Long purchaseCount) {
		super();
		this.product = product;
		this.timestamp = timestamp;
		this.pageViewCount = pageViewCount;
		this.addToCartCount = addToCartCount;
		this.purchaseCount = purchaseCount;
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

	public Long getPageViewCount() {
		return pageViewCount;
	}

	public void setPageViewCount(Long pageViewCount) {
		this.pageViewCount = pageViewCount;
	}

	public Long getAddToCartCount() {
		return addToCartCount;
	}

	public void setAddToCartCount(Long addToCartCount) {
		this.addToCartCount = addToCartCount;
	}

	public Long getPurchaseCount() {
		return purchaseCount;
	}

	public void setPurchaseCount(Long purchaseCount) {
		this.purchaseCount = purchaseCount;
	}

	@Override
	public String toString() {
		return "ActivityByProduct [product=" + product + ", timestamp=" + timestamp + ", pageViewCount=" + pageViewCount
				+ ", addToCartCount=" + addToCartCount + ", purchaseCount=" + purchaseCount + "]";
	}

}
