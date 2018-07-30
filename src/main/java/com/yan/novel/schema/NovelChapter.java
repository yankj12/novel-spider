package com.yan.novel.schema;

import java.io.Serializable;

public class NovelChapter implements Serializable{

	// 序号
	private int serialNo;
	
	// 章节的urlToken
	private String chapterUrlToken;
	
	// 章节序号的中文名
	private String chapterSerialName;
	
	// 章节名
	private String chapterName;
	
	// 第X章 章节名
	private String chapterFullName;
	
	// 章节url
	private String chapterUrl;

	// 内容
	private String chapterContent;
	
	public int getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(int serialNo) {
		this.serialNo = serialNo;
	}

	public String getChapterUrlToken() {
		return chapterUrlToken;
	}

	public void setChapterUrlToken(String chapterUrlToken) {
		this.chapterUrlToken = chapterUrlToken;
	}

	public String getChapterSerialName() {
		return chapterSerialName;
	}

	public void setChapterSerialName(String chapterSerialName) {
		this.chapterSerialName = chapterSerialName;
	}

	public String getChapterName() {
		return chapterName;
	}

	public void setChapterName(String chapterName) {
		this.chapterName = chapterName;
	}

	public String getChapterFullName() {
		return chapterFullName;
	}

	public void setChapterFullName(String chapterFullName) {
		this.chapterFullName = chapterFullName;
	}

	public String getChapterUrl() {
		return chapterUrl;
	}

	public void setChapterUrl(String chapterUrl) {
		this.chapterUrl = chapterUrl;
	}

	public String getChapterContent() {
		return chapterContent;
	}

	public void setChapterContent(String chapterContent) {
		this.chapterContent = chapterContent;
	}
	
}