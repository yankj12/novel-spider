package com.yan.novel.spider.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.yan.novel.schema.NovelChapter;
import com.yan.novel.schema.NovelInfo;
import com.yan.novel.service.facade.NovelInfoDaoService;
import com.yan.novel.service.impl.NovelInfoDaoServiceSpringImpl;
import com.yan.novel.spider.service.facade.NovelCrawlService;
import com.yan.novel.util.NovelHtmlUtil;
import com.yan.novel.util.NovelToFilesUtil;

public class NovelCrawlServiceBiQuGeImpl extends AbstrateNovelCrawlServiceImpl implements NovelCrawlService{

	public String crawlNovel(String webRootUrl, String novelUrlToken) throws Exception{
		String novelUrl = webRootUrl + "/" + novelUrlToken + "/";

		// 请求这个url，获取返回的html内容
		String content = this.requestUrlByGetMethod(novelUrl);
		
		//TODO
		NovelInfo novelInfo = new NovelInfo();
		novelInfo.setNovelUrlToken(novelUrlToken);
		novelInfo.setNovelUrl(novelUrl);
		
		// 使用jsoup解析html内容
		Document document=Jsoup.parse(content);
		Element element = document.select("div#maininfo").first();
//		for(Element childElement : element.children()) {
//			System.out.println("#############################");
//			System.out.println(childElement.wholeText());
//		}
		// 小说标题、作者、最后更新时间、最后更新章节
		Element infoElement = element.select("div#info").first();
		// 标题
		String novelName = infoElement.child(0).text();
		novelInfo.setNovelName(novelName);
		
		// 作者
		String authorName = infoElement.child(1).text();
		String[] ary = authorName.split("[:：]");
		authorName = (ary != null && ary.length>=2?ary[1]:authorName);
		novelInfo.setAuthorName(authorName);
		
		// 最后更新
		String lastUpdateTime = infoElement.child(3).text();
		ary = lastUpdateTime.split("[:：]");
		lastUpdateTime = (ary != null && ary.length>=2?ary[1]:lastUpdateTime);
		novelInfo.setLastUpdateTime(lastUpdateTime);
		
		// 最新章节
		String lastUpdateChapterFullName = infoElement.child(4).text();
		ary = lastUpdateChapterFullName.split("[:：]");
		lastUpdateChapterFullName = (ary != null && ary.length>=2?ary[1]:lastUpdateChapterFullName);
		novelInfo.setLastUpdateChapterFullName(lastUpdateChapterFullName);
		
		// 小说简介信息
		Element introElement = element.select("div#intro").first();
		String novelSummary = introElement.text();
		novelInfo.setNovelSummary(novelSummary);
		// 1 表示下载完成
		novelInfo.setDownloadFlag("1");
		
		// 首先应该初始化文件夹
		if(this.isWriteToLocal()) {
			//System.out.println("初始化小说文件夹:" + novelInfo.getNovelName());
			NovelToFilesUtil.initLocalDirsAndFiles(this.getWorkRootDirName(), novelName);
		}
		
		// 小说信息写入到本地文件
		if(this.isWriteToLocal()) {
			//System.out.println("小说信息写入到文件:" + novelInfo.getNovelName());
			NovelToFilesUtil.writeNovelInfoToLocalFile(novelInfo, this.getWorkRootDirName());
		}
		
		List<NovelChapter> novelChapters = new ArrayList<NovelChapter>();
		// 使用jsoup获取章节链接
		Element chapterListDivElement = document.select("div#list").first();
		List<Element> chapterLinkElementList = chapterListDivElement.select("a");
		if(chapterLinkElementList != null) {
			int serialNo = 1;
			for(Element linkElement:chapterLinkElementList) {
				String linkText = linkElement.html();
				String[] ary1 = linkText.split("\\s+");
//				System.out.println(Arrays.toString(ary));
				//  /2_2144/1268254.html
				String chapterRelativeUrl = linkElement.attr("href");
				String chapterUrl = webRootUrl + chapterRelativeUrl;
//				System.out.println(chapterRelativeUrl);
				
				// 从章节相对链接中截取章节的urlToken
				int index = chapterRelativeUrl.lastIndexOf("/");
				int index2 = chapterRelativeUrl.lastIndexOf(".html");
				String chapterUrlToken = chapterRelativeUrl.substring(index+1, index2);
				
				NovelChapter chapter = new NovelChapter();
				chapter.setNovelUrlToken(novelUrlToken);
				chapter.setSerialNo(serialNo);
				chapter.setChapterFullName(linkText);
				chapter.setChapterUrlToken(chapterUrlToken);
				chapter.setChapterUrl(chapterUrl);
				if(ary1 != null && ary1.length >= 2){
					chapter.setChapterSerialName(ary1[0].trim());
					chapter.setChapterName(ary1[1].trim());
				}else if(ary1 != null && ary1.length >= 1){
					chapter.setChapterName(ary1[0].trim());
				}
				
				novelChapters.add(chapter);
				serialNo++;
				
				System.out.println("获取章节链接:" + chapter.getChapterFullName());
			}
		}

		NovelInfoDaoService novelInfoDaoService = new NovelInfoDaoServiceSpringImpl();
				NovelInfo novelInfoTmp = novelInfoDaoService.queryNovelInfoByNovelUrlToken(novelUrlToken);
		if(novelInfoTmp != null) {
			
		}else {
			novelInfoDaoService.insertNovelInfo(novelInfo);
			
		}
		
		// 爬取并保存章节内容，保存到数据库和文件
		this.crawlAndSaveNovelChapters(novelUrlToken, novelInfoTmp, novelChapters);
		
		System.out.println("保存结束");

		return null;
	}
	
	/**
	 * 爬取章节内容
	 * @param chapterUrl
	 * @return
	 */
	public NovelChapter crawNovelChapter(String chapterUrl) {
		NovelChapter novelChapter = new NovelChapter();
		
		if(chapterUrl == null || "".equals(chapterUrl.trim())){
			return novelChapter;
		}
		
		String html = this.requestUrlByGetMethod(chapterUrl);
		// 解析html
		// div id=content
		Document document=Jsoup.parse(html);
		Element element = document.select("div#content").first();
		String chapterContent = element.html();
		//System.out.println(chapterContent);
		
		// 去掉章节内容中的html标签
		chapterContent = NovelHtmlUtil.removeHtmlTags(chapterContent);
		
		novelChapter.setChapterContent(chapterContent);
		
		return novelChapter;
	}
	
	/**
	 * 通过get的方式请求一个url
	 * @param url
	 * @return
	 */
	public String requestUrlByGetMethod(String url) {
		Map<String, String> requestHeaders = new HashMap<>();
		
		requestHeaders.put("Host", "www.biquge.com.tw");
		requestHeaders.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:61.0) Gecko/20100101 Firefox/61.0");
		requestHeaders.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		requestHeaders.put("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
		requestHeaders.put("Accept-Encoding", "gzip, deflate");
		requestHeaders.put("Referer", "https://www.baidu.com/link?url=RJ7MpkvYpENEfeXkw7-VTDKT39DFECqQvmQYvHsJLNr9dSfdCxuOQR7_646eQs9a&wd=&eqid=ea3343c700047d65000000025b5ac858");
		requestHeaders.put("Cookie", "__cdnuid=c5d84123c59bad6d9f3437f188427588");
		requestHeaders.put("Connection", "keep-alive");
		requestHeaders.put("Upgrade-Insecure-Requests", "1");
		requestHeaders.put("If-Modified-Since", "Wed, 25 Jul 2018 07:17:07 GMT");
		requestHeaders.put("If-None-Match", "80dba87ee723d41:0");
		requestHeaders.put("Cache-Control", "max-age=0");
		
		String html = this.requestUrlByGetMethod(url, requestHeaders, "gbk");
		return html;
	}
	
}
