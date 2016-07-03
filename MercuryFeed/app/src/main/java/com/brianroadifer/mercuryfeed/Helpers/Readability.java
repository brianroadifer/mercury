package com.brianroadifer.mercuryfeed.Helpers;

import android.net.Uri;

import com.brianroadifer.mercuryfeed.Models.Article;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Brian Roadifer on 6/29/2016.
 */


public class Readability {
    final int FLAG_STRIP_UNLIKELYS = 0x1;
    final int FLAG_WEIGHT_UNLIKELYS = 0x2;
    final int FLAG_CLEAN_CONDITIONALLY = 0x4;

    final int DEFAULT_MAX_ELEMS_TO_PARSE = 0;
    final int DEFAULT_N_TOP_CANDIDATES = 0;
    final int DEFAULT_MAX_PAGES = 0;
    final String[] DEFAULT_TAGS_TO_SCORE = "section,h2,h3,h4,h5,h6,p,td,pre".toUpperCase().split(",");
    final String[] DIV_TO_P_ELEMS = "a,blockquote,dl,div,img,ol,p,table,ul,select".toUpperCase().split(",");
    final String[] ALTER_DIV_TO_P_EXCEPTIONS = "div,article,section,p".toUpperCase().split(",");

    Uri uri;
    Document document;
    JSONObject options = new JSONObject();
    Boolean biggestFrame = false;
    String articleByLine = null;
    String articleDir = null;
    boolean debug;
    int maxElemsToParse;
    int nbTopCandidates;
    int maxPages;
    int flags;
    List<Article> parsedPages = new ArrayList<>();
    List<Object> pageETags = new ArrayList<>();
    int curPageNum = 1;
    String logEl = "";

    /**
     * @param uri URI descriptor object
     * @param document Document to parse
     */
    public Readability(Uri uri, Document document) throws JSONException {
        this.uri = uri;
        this.document = document;
        processOptions(this.options);
    }

    /**
     * @param uri URI descriptor object
     * @param document Document to parse
     * @param options optional features
     */
    public Readability(Uri uri, Document document, JSONObject options) throws JSONException{
        this(uri, document);
        this.options = options;
        processOptions(this.options);
    }
    private void processOptions(JSONObject options) throws JSONException{
        this.debug = (boolean) options.get("debug");
        this.maxElemsToParse = (int) options.get("maxElemsToParse") | this.DEFAULT_MAX_ELEMS_TO_PARSE;
        this.nbTopCandidates = (int) options.get("nbTopCandidates") | this.DEFAULT_N_TOP_CANDIDATES;
        this.maxPages = (int) options.get("maxPages") | this.DEFAULT_MAX_PAGES;
        this.flags = this.FLAG_STRIP_UNLIKELYS | this.FLAG_WEIGHT_UNLIKELYS | this.FLAG_CLEAN_CONDITIONALLY;
    }

    private void Logger(){}

    private void onPostProcessContent(Element articleContent){
        fixRelativeUris(articleContent);
    }

    private void forEachNode(List<Node> nodeList, IReadability s){
        for (Node node : nodeList) {
            s.execute(node);
        }
    }
    private boolean someNode(List<Node> nodeList, IReadability s){
        for (Node node : nodeList) {
            if(s.fn(node)){
                return true;
            }else{
                return false;
            }
        }
        return false;
    }

    private List<Element> getAllNodesWithTag(Element node, String[] tagNames){
        List<Element> collection = new ArrayList<>();
        if(node.select("") != null){
            return node.select(tagNames.toString());
        }
        for (String tag: tagNames) {
            for (Element el: node.select(tag)) {
                collection.add(el);
            }
        }
        return collection;
    }

    private void fixRelativeUris(Element articleContent){
        ChangeLinks(articleContent);
        ChangeImages(articleContent);

    }

    private String toAbsoulteUri(String uri){
        String scheme = this.uri.getScheme();
        String prePath = this.uri.getAuthority();
        String pathBase = this.uri.getPath();
        String regex = "^[a-zA-Z][a-zA-Z0-9\\+\\-\\.]*:";
        if(uri.matches(regex)){
            return uri;
        }
        if(uri.substring(0,2) == "//"){
            return scheme + "://" + uri.substring(2);
        }
        if(uri.startsWith("/", 0)){
            return prePath + uri;
        }
        if(uri.indexOf("./") == 0){
            return pathBase + uri.substring(2);
        }
        if(uri.startsWith("#", 0)){
            return uri;
        }
        return prePath + uri;

    }

    private void ChangeLinks(Element articleContent){
        Elements links = articleContent.getElementsByTag("a");
        for(Element link : links){
            String href = link.attr("href");
            if( href != null || !href.isEmpty()){
                if(href.indexOf("javascript:") == 0){
                    link.replaceWith(document.createElement("a").attr("href", href).text(link.text()));
                }
                else{
                    link.attr("href", href);
                }
            }
        }

    }

    private void ChangeImages(Element articleContent){
        Elements imgs = articleContent.getElementsByTag("img");
        for(Element img : imgs){
            String src = img.attr("src");
            if(src != null || !src.isEmpty()){
                img.attr("src", src);
            }
        }
    }


}
