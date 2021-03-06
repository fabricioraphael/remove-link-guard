package br.com.jspace.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import br.com.jspace.util.LinkUtil;

public class RemoveLinkServiceImpl implements RemoveLinkService {

	String BEETWEN_QUERY_AND_EQUALS = "\\?[\\w]++\\=";
	String BEETWEN_QUERY_AND_EXCLAMATION = "\\?[\\w]++\\!";
	String HTTP_ASCII = "687474703";

	public String breakUrl(String protectedUrl) {
		String url = "";

		if (protectedUrl.trim().contains(" ")) {
			throw new IllegalArgumentException("Link inválido");
		} else if (!protectedUrl.contains("http://")) {
			throw new IllegalArgumentException("Url no formato inválido. 'necessita de http://'");
		}

		url = parseUrl(protectedUrl.trim());

		if (url.startsWith(HTTP_ASCII)) {
			url = LinkUtil.decodeAsciiLink(url);
		} else if (Base64.isArrayByteBase64(url.getBytes())) {
			url = LinkUtil.decodeBase64(url);
		} else if (url.contains("//:ptth")) {
			url = LinkUtil.decodeInvertedUrl(url);
		}

		return url;
	}

	private String parseUrl(String protectedUrl) {
		String url = "";

		if (LinkUtil.isBaixeAquiFilmes(protectedUrl)) {
			return protectedUrl.substring(protectedUrl.indexOf("/link/") + 7);
		} else if (LinkUtil.isEncurtador(protectedUrl)) {
			String base64Url = protectedUrl.substring(protectedUrl
					.indexOf("com/?") + 5);
			String reverseUrl = LinkUtil.decodeBase64(base64Url);
			
			return LinkUtil.reverteUrl(reverseUrl);
		} else if (LinkUtil.isProteLink(protectedUrl)) {
			String base64Url = protectedUrl.substring(protectedUrl.indexOf("/id/") + 4);
			protectedUrl = LinkUtil.decodeBase64(base64Url);
		} else if (LinkUtil.isVinXp(protectedUrl)){
			String downloadTitle = LinkUtil.decodeAsciiLink(protectedUrl.substring(protectedUrl.indexOf("/d/") + 6));
			downloadTitle = LinkUtil.vinXpRemoveInvalidChars(downloadTitle);
			
			return "http://www.vinxp.com/"+downloadTitle;
		}

		if (!isASimpleProtectedUrl(protectedUrl)
				&& containsSomethingBetweenQueryAndEqualsOrExclamation(protectedUrl)) {
			url = afterQueryHaveNumber(protectedUrl);
			if (url == null) {
				url = getStringAfterQueryAndEqualsOrExclamation(protectedUrl);

				if (protectedUrl.endsWith("=/")) {
					Pattern p = Pattern.compile("(\\w)*=/");
					Matcher m = p.matcher(protectedUrl);
					m.find();
					url = m.group();
				}
			}
		} else if (protectedUrl.contains("http://")) {
			url = protectedUrl;
		} else {
			throw new IllegalArgumentException("Link inválido");
		}

		return url;
	}

	private String afterQueryHaveNumber(String protectedUrl) {
		int query = protectedUrl.indexOf("?");
		String afterQuery = protectedUrl.substring(query + 1);

		try {
			Integer.parseInt(Character.toString(afterQuery.charAt(0)));
		} catch (NumberFormatException e) {
			return null;
		}

		return afterQuery;
	}

	private boolean isASimpleProtectedUrl(String protectedUrl) {
		boolean simpleProtectedUrl = false;

		int i = StringUtils.countMatches(protectedUrl, "http://");

		if (i == 1
				&& (protectedUrl.contains(":ptth")
						|| protectedUrl.contains(HTTP_ASCII) || StringUtils
						.countMatches(protectedUrl, "/") > 3)) {
			simpleProtectedUrl = false;
		} else if (i == 1) {
			simpleProtectedUrl = true;
		}

		return simpleProtectedUrl;
	}

	private boolean containsSomethingBetweenQueryAndEqualsOrExclamation(
			String protectedUrl) {
		Pattern p = null;

		if (protectedUrl.contains("?") && protectedUrl.contains("=")) {
			p = Pattern.compile(BEETWEN_QUERY_AND_EQUALS);
		} else if (protectedUrl.contains("?") && protectedUrl.contains("!")) {
			p = Pattern.compile(BEETWEN_QUERY_AND_EXCLAMATION);
		}
		Matcher m = p.matcher(protectedUrl.toLowerCase());

		if (m.find()) {
			return true;
		}

		return false;
	}

	private String getStringAfterQueryAndEqualsOrExclamation(String protectedUrl) {
		String url = "";
		String param = "";

		Pattern p = null;
		if (protectedUrl.contains("=")) {
			p = Pattern.compile("\\?[\\w]++\\=");
		} else if (protectedUrl.contains("!")) {
			p = Pattern.compile("\\?[\\w]++\\!");
		}

		Matcher m = p.matcher(protectedUrl);

		while (m.find()) {
			param = m.group();
			break;
		}

		url = protectedUrl.substring(protectedUrl.indexOf(param)
				+ param.length());

		return url;
	}
}