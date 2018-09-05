package com.sparkjobs.model;

import java.io.Serializable;

public class Activity implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long timestamp;
	private String referrer;
	private String action;
	private String previewspage;
	private String visitor;
	private String page;
	private String product;
	private String properties;

	public Activity() {
		super();
	}

	public Activity(long timestamp, String referrer, String action, String previewspage, String visitor, String page,
			String product, String properties) {
		super();
		this.timestamp = timestamp;
		this.referrer = referrer;
		this.action = action;
		this.setPreviewspage(previewspage);
		this.visitor = visitor;
		this.page = page;
		this.product = product;
		this.setProperties(properties);
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getReferrer() {
		return referrer;
	}

	public void setReferrer(String referrer) {
		this.referrer = referrer;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getVisitor() {
		return visitor;
	}

	public void setVisitor(String visitor) {
		this.visitor = visitor;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getPreviewspage() {
		return previewspage;
	}

	public void setPreviewspage(String previewspage) {
		this.previewspage = previewspage;
	}

	public String getProperties() {
		return properties;
	}

	public void setProperties(String properties) {
		this.properties = properties;
	}

	@Override
	public String toString() {
		return "Activity [timestamp=" + timestamp + ", referrer=" + referrer + ", action=" + action + ", previewspage="
				+ previewspage + ", visitor=" + visitor + ", page=" + page + ", product=" + product + ", properties="
				+ properties + "]";
	}

}
