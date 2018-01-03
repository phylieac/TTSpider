package edu.pantek.spider.taobao;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import edu.pantek.databases.serialization.DBSerialization;
import edu.pantek.databases.serialization.MySQLSerialization;

public class Spider extends edu.pantek.thread.Thread {

	String sword = null;
	String sURL = null;
	CookieStore cookies = new BasicCookieStore();
	CloseableHttpClient client = null;
	DBSerialization dbs = null;

	String pageup = "&bcoffset=4&p4ppushleft=2%2C48&ntoffset=4&s=";
	int pagenum = 0;
	int pagesize = 0;
	int pagetotal = 0;

	String commentURL = "https://rate.taobao.com/feedRateList.htm?auctionNumId=";// auctionNumId=553186297820&currentPageNum=1&pageSize=20
	int commentPageSize = 20;
	int currentPageNum = 1;
	int commentSize = 0;
	int cpagetotal = 0;

	public Spider(boolean setCookie) {
		// 设置cookies
		for (Entry<String, String> entry : TTCookies.cookies.entrySet()) {
			Cookie ck = new BasicClientCookie2(entry.getKey(), entry.getValue());
			cookies.addCookie(ck);
		}
		if (setCookie)
			client = HttpClients.custom().setDefaultCookieStore(cookies).build();
		else
			client = HttpClients.createDefault();
	}

	public Spider(String kword) {
		this.sword = kword;
	}

	public void setSerialization(DBSerialization db) {
		this.dbs = db;
	}

	/**
	 * 按搜索关键词与销售量倒序
	 * 
	 * @param kw
	 */
	public void setSearchKw(String kw) {
		this.sword = kw;
		this.sURL = "https://s.taobao.com/search?q=" + kw
				+ "&imgfile=&js=1&stats_click=search_radio_all%3A1&initiative_id=staobaoz_20171120&ie=utf8";
	}

	public JSONObject getTBJSON() {
		String line = null;
		try {
			Document doc = Jsoup.connect(this.sURL + this.pageup + this.pagenum).timeout(5000).get();
			if (doc == null)
				return new JSONObject();
			Scanner scan = new Scanner(doc.toString());
			while (scan.hasNextLine()) {
				line = scan.nextLine();
				if (line.contains("g_page_config")) {
					break;
				}
			}
			scan.close();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!line.contains("{") || !line.contains("}"))
			return new JSONObject();
		String js = line.substring(line.indexOf('{'), line.lastIndexOf('}') + 1);
		JSONObject json = JSONObject.parseObject(js);
		return json;
	}

	public void getItemSeed() {
		JSONObject json = this.getTBJSON();
		if (this.pagenum == 0) {
			this.dbs.serialize("search", this.getSearchInfo(json));
		}
		for (Object j : this.getPageGoods(json))
			this.dbs.serialize("goods", (JSONObject) j);
		for (int p = 1; p < this.pagetotal; p++) {
			System.out.println("page:" + p);
			json = this.getTBJSON();
			for (Object j : this.getPageGoods(json))
				this.dbs.serialize("goods", (JSONObject) j);
			this.pagenum = p * this.pagesize + 1;
		}
	}

	public void threadSleep(long millis) {
		try {
			sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		JSONObject json = this.getTBJSON();
		if (this.pagenum == 0) {
			this.dbs.serialize("search", this.getSearchInfo(json));
		}
		JSONObject jj = null;
		for (Object j : this.getPageGoods(json)) {
			jj = (JSONObject) j;
			this.dbs.serialize("goods", jj);
			this.pageComment(jj);
		}
		this.threadSleep(this.mlongwait);
		for (int p = 1; p < this.pagetotal; p++) {
			System.out.println("page:" + p);
			json = this.getTBJSON();
			for (Object j : this.getPageGoods(json)) {
				jj = (JSONObject) j;
				this.dbs.serialize("goods", (JSONObject) j);
				this.pageComment(jj);
			}
			this.pagenum = p * this.pagesize + 1;
			this.threadSleep(this.mlongwait);
		}
		this.exit();
	}

	public JSONArray getPageGoods(JSONObject json) {
		if (json.size() == 0)
			return new JSONArray();
		JSONArray js = null;
		JSONObject t = null;
		if (json.containsKey("mods")) {
			t = json.getJSONObject("mods");
			if (t.containsKey("itemlist")) {
				t = t.getJSONObject("itemlist");
				if (t.containsKey("data")) {
					t = t.getJSONObject("data");
					if (t.containsKey("auctions")) {
						js = t.getJSONArray("auctions");
					} else
						return new JSONArray();
				} else
					return new JSONArray();
			} else
				return new JSONArray();
		} else
			return new JSONArray();
		JSONArray result = new JSONArray();
		for (Object j : js) {
			// 一个商品
			JSONObject jj = (JSONObject) j;
			JSONObject jss = new JSONObject();
			jss.put("nid", jj.getString("nid"));
			jss.put("category", jj.getString("category"));
			jss.put("pid", jj.getString("pid"));
			jss.put("title", jj.getString("title"));
			jss.put("raw_title", jj.getString("raw_title"));
			jss.put("detail_url", jj.getString("detail_url"));
			jss.put("view_price", jj.getString("view_price"));
			jss.put("view_fee", jj.getString("view_fee"));
			jss.put("item_loc", jj.getString("item_loc"));
			jss.put("view_sales", jj.getString("view_sales"));
			jss.put("comment_count", jj.getString("comment_count"));
			jss.put("user_id", jj.getString("user_id"));
			jss.put("nick", jj.getString("nick"));
			jss.put("comment_url", jj.getString("comment_url"));
			jss.put("shopLink", jj.getString("shopLink"));
			jss.put("risk", jj.getString("risk"));
			result.add(jss);
		}
		js = null;
		return result;
	}

	public void exit() {
		this.dbs.close();
	}

	public JSONObject getSearchInfo(JSONObject json) {
		JSONObject js = json.getJSONObject("mainInfo").getJSONObject("traceInfo").getJSONObject("traceData");
		JSONObject njs = new JSONObject();
		njs.put("word", this.sword);
		njs.put("page_size", js.get("page_size"));
		njs.put("totalHits", js.get("totalHits"));
		njs.put("rsKeywords", js.getString("rsKeywords"));
		this.pagesize = Integer.parseInt(js.getString("page_size"));
		this.pagetotal = (int) Math.ceil((double) Integer.parseInt(js.getString("totalHits")) / this.pagesize);
		System.out.println(this.pagetotal);
		return njs;
	}

	public JSONArray getCommentJSON(String auctionNumId, int currentPageNum) {
		JSONArray result = new JSONArray();
		try {
			Document doc = Jsoup.connect(this.commentURL + auctionNumId + "&currentPageNum=" + currentPageNum
					+ "&pageSize=" + this.commentPageSize).timeout(5000).get();
			if (doc == null)
				return result;
			System.out.println(this.commentURL + auctionNumId + "&currentPageNum=" + currentPageNum + "&pageSize="
					+ this.commentPageSize);
			String ccontent = doc.body().html();
			System.out.println(ccontent);
			if (ccontent.startsWith("(") && ccontent.endsWith(""))
				ccontent = ccontent.substring(ccontent.indexOf('(') + 1, ccontent.lastIndexOf(')'));
			else {
				System.out.println("未获取到评论数据！");
				return result;
			}
			if (ccontent == null)
				return result;
			JSONObject json = null;
			try {
				json = JSONObject.parseObject(ccontent);
			} catch (Exception e) {
				// e.printStackTrace();
				return result;
			}
			this.commentSize = json.getIntValue("total");
			this.cpagetotal = (int) Math.ceil(((double) this.commentSize) / this.commentPageSize);
			if(!json.containsKey("comments")||json.get("comments")==null) return result;
			for (Object js : json.getJSONArray("comments")) {
				JSONObject j = (JSONObject) js;
				JSONObject jj = new JSONObject();
				jj.put("nid", auctionNumId);
				jj.put("date", j.getString("date"));
				jj.put("content", j.getString("content"));
				jj.put("rateId", j.getIntValue("rateId"));
				jj.put("auction", j.getString("auction"));
				jj.put("rate", j.getString("rate"));
				jj.put("useful", j.getString("useful"));
				jj.put("user", j.getString("user"));
				jj.put("append", j.getString("append"));
				jj.put("buyAmount", j.getString("buyAmount"));
				result.add(jj);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public void pageComment(JSONObject jj) {
		// 商品评论
		if (jj.getString("comment_count").equals("0"))
			return;
		String id = jj.getString("nid");
		JSONArray comment = this.getCommentJSON(id, 1);
		for (Object c : comment)
			this.dbs.serialize("comment", (JSONObject) c);
		this.threadSleep(this.mlongwait);
		for (int i = 2; i < this.cpagetotal; i++) {
			comment = this.getCommentJSON(id, i);
			for (Object c : comment)
				this.dbs.serialize("comment", (JSONObject) c);
			this.threadSleep(this.mlongwait);
		}
	}

	public static void main(String[] args) {
		MySQLSerialization ms = new MySQLSerialization("localhost", "3306", "taobao");
		ms.setUserPasswd("root", "123456");
		System.out.println("Connect to MySQL:" + ms.getConnection());
		Spider sp = new Spider(false);
		sp.setSerialization(ms);
		sp.setSearchKw("男装");
		sp.start();
	}
}
