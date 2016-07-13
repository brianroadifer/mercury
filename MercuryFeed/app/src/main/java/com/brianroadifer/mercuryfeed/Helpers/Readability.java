package com.brianroadifer.mercuryfeed.Helpers;

import android.text.TextUtils;

import com.brianroadifer.mercuryfeed.Models.Article;
import com.brianroadifer.mercuryfeed.Models.Metadata;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Brian Roadifer on 6/29/2016.
 */
enum REGEX {
    unlikelyCandidates("/banner|combx|comment|community|disqus|extra|foot|header|menu|modal|related|remark|rss|share|shoutbox|sidebar|skyscraper|sponsor|ad-break|agegate|pagination|pager|popup/i"),
    okMaybeItsACandidate("/and|article|body|column|main|shadow/i"),
    positive("/article|body|content|entry|hentry|main|page|pagination|post|text|blog|story/i"),
    negative("/hidden|^hid$| hid$| hid |^hid |banner|combx|comment|com-|contact|foot|footer|footnote|masthead|media|meta|modal|outbrain|promo|related|scroll|share|shoutbox|sidebar|skyscraper|sponsor|shopping|tags|tool|widget/i"),
    extraneous("/print|archive|comment|discuss|e[\\-]?mail|share|reply|all|login|sign|single|utility/i"),
    byline("/byline|author|dateline|writtenby/i"),
    replaceFonts("/<(\\/?)font[^>]*>/gi"),
    normalize("/\\s{2,}/g"),
    videos("/\\/\\/(www\\.)?(dailymotion|youtube|youtube-nocookie|player\\.vimeo)\\.com/i"),
    nextLink("/(next|weiter|continue|>([^\\|]|$)|»([^\\|]|$))/i"),
    prevLink("/(prev|earl|old|new|<|«)/i"),
    whitespace("/^\\s*$/"),
    hasContent("/\\S$/");

    private final String pattern;

    private REGEX(String p){
        pattern = p;
    }
    public boolean equalsName(String otherpattern){
        return (otherpattern == null) ? false : pattern.equalsIgnoreCase(otherpattern);
    }
    public String toString(){
        return this.pattern;
    }
}

public class Readability {

    final int FLAG_STRIP_UNLIKELYS = 0x1;
    final int FLAG_WEIGHT_CLASSES = 0x2;
    final int FLAG_CLEAN_CONDITIONALLY = 0x4;

    final int DEFAULT_MAX_ELEMS_TO_PARSE = 0;
    final int DEFAULT_N_TOP_CANDIDATES = 0;
    final int DEFAULT_MAX_PAGES = 0;
    final String DEFAULT_TAGS_TO_SCORE = "section,h2,h3,h4,h5,h6,p,td,pre";
    final String DIV_TO_P_ELEMS = "a,blockquote,dl,div,img,ol,p,table,ul,select";
    final String ALTER_TO_DIV_EXCEPTIONS = "div,article,section,p";

    URI uri;
    private final Document document;
    private String cache;
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

    public Readability(URI uri, Document document) {
        this.uri = uri;
        this.document = document;
        this.maxElemsToParse = this.DEFAULT_MAX_ELEMS_TO_PARSE;
        this.nbTopCandidates =  this.DEFAULT_N_TOP_CANDIDATES;
        this.maxPages = this.DEFAULT_MAX_PAGES;
        this.flags = this.FLAG_STRIP_UNLIKELYS | this.FLAG_WEIGHT_CLASSES | this.FLAG_CLEAN_CONDITIONALLY;
    }
    private Metadata getArticleMetadata(){
        Metadata metadata = new Metadata();
        Map<String, String> values = new HashMap<>();
        Elements metaElements = this.document.getElementsByTag("meta");
        String namePattern = "/^\\\\s*((twitter)\\\\s*:\\\\s*)?(description|title)\\\\s*$/gi";
        String propertyPattern = "/^\\s*og\\s*:\\s*(description|title)\\s*$/gi";
        for(Element element: metaElements){
            String elementName = element.attr("name");
            String elementProperty = element.attr("property");
            List<String> value = new ArrayList<>();
            value.add(elementName);
            value.add(elementProperty);
            if(value.contains("author")){
                metadata.Author = element.attr("content");
                break;
            }
            String name = null;
            if(Pattern.matches(namePattern, elementName)){
                name = elementName;
            }else if(Pattern.matches(propertyPattern, elementProperty)){
                name = elementProperty;
            }
            if(name != null){
                String content = element.attr("content");
                if(content != null){
                    name = name.toLowerCase().replace("/\\s/g", "");
                    values.put(name, content.trim());
                }
            }

        }
        if(values.containsKey("description")){
            metadata.Excerpt = values.get("description");
        }else if(values.containsKey("og:description")){
            metadata.Excerpt = values.get("og:description");
        }else if(values.containsKey("twitter:description")){
            metadata.Excerpt = values.get("twitter:description");
        }

        if(values.containsKey("title")){
            metadata.Title = values.get("title");
        }else if(values.containsKey("og:title")){
            metadata.Title = values.get("og:title");
        }else if(values.containsKey("twitter:title")){
            metadata.Title = values.get("twitter:title");
        }

        return metadata;
    }
    private void removeScripts(Element element){
        element.getElementsByTag("script").remove();
        element.getElementsByTag("noscript").remove();

    }
    private float readablitiy(Element node){
        float readability = 0;

        switch(node.tagName().toUpperCase()) {
            case "DIV":
                readability += 5;
                break;

            case "PRE":
            case "TD":
            case "BLOCKQUOTE":
                readability += 3;
                break;

            case "ADDRESS":
            case "OL":
            case "UL":
            case "DL":
            case "DD":
            case "DT":
            case "LI":
            case "FORM":
                readability -= 3;
                break;

            case "H1":
            case "H2":
            case "H3":
            case "H4":
            case "H5":
            case "H6":
            case "TH":
                readability -= 5;
                break;
        }

        readability += getClassWeight(node);
        return readability;
    }
    private float getClassWeight(Element node){
        float weight = 0;
        if(!flagIsActive(FLAG_WEIGHT_CLASSES)){
            return weight;
        }
        if(node.className() != null && !node.className().isEmpty()){
            if(Pattern.matches(REGEX.negative.toString(), node.className())){
                weight -= 25;
            }
            if(Pattern.matches(REGEX.positive.toString(), node.className())){
                weight += 25;
            }
        }
        if(node.id() != null && !node.id().isEmpty()){
            if(Pattern.matches(REGEX.negative.toString(), node.id())){
                weight -= 25;
            }
            if(Pattern.matches(REGEX.positive.toString(), node.id())){
                weight += 25;
            }
        }
        return weight;
    }
    private boolean flagIsActive(int flag) {
        return (this.flags & flag) > 0;
    }
    private void addFlag(int flag) {
        this.flags = this.flags | flag;
    }
    private void removeFlag(int flag) {
        this.flags = this.flags & ~flag;
    }
    private Element removeAndGetNext(Element node){
        Element nextNode = getNextNode(node, true);
        node.remove();
        return nextNode;
    }
    private Element getNextNode(Element node, boolean ingnoreSelfAndKids){
        if(!ingnoreSelfAndKids && node.children().first() != null){
            return node.children().first();
        }
        if(node.nextElementSibling() != null){
            return node.nextElementSibling();
        }
        do{
            node = node.parent();
        }while(node != null && node.nextElementSibling() != null);
        if(node != null){
            return node;
        }else if(node.nextElementSibling() != null){
            return node.nextElementSibling();
        }else{
            return null;
        }
    }
    private Element nextSibling(Element node){
        do{
            node = node.nextElementSibling();
        }while (node != null && node.nextElementSibling() != null);
        if(node != null){
            return node;
        }else if(node.nextElementSibling() != null){
            return node.nextElementSibling();
        }else{
            return null;
        }
    }
    private Element getNextNodeNoElementProperties(Element node, boolean ignoreSelfAndKids){
        if(!ignoreSelfAndKids && node.children().get(0)!=null ){
            return node.children().get(0);
        }
        Element next = nextSibling(node);
        if(next != null){
            return next;
        }
        do{
            node = node.parent();
            if(node != null){
                next = nextSibling(node);
            }
        }while (node != null && next == null);
        if(node != null){
            return node;
        }else {
            return null;
        }
    }
    public void prepDocument(){
        if(document.body() == null){
            document.body().appendElement("body");
        }
        document.getElementsByTag("style").remove();
        replaceBrs(document.body());
        document.getElementsByTag("font").tagName("span");
    }
    private Element nextElement(Element node){
        Element next = node;
        while (next != null && Pattern.matches(REGEX.whitespace.toString(), next.text())){
            next = next.nextElementSibling();
        }
        return next;
    }
    public void replaceBrs(Element element){
        for (Element br: element.getElementsByTag("br")) {
            Element next = br.nextElementSibling();
            boolean replaced = false;
            while (next != null && next.equals(nextElement(next)) && (next.tagName().equalsIgnoreCase("BR"))){
                replaced = true;
                Element brSibling = next.nextElementSibling();
                next.remove();
                next = brSibling;
            }
            if(replaced){
                Element p = this.document.createElement("p");
                br.replaceWith(p);
                next = p.nextElementSibling();
                while (next!= null){
                    if(next.tagName().equalsIgnoreCase("BR")){
                        Element nextElem = nextElement(next);
                        if(nextElem != null && nextElem.tagName().equalsIgnoreCase("BR")){
                            break;
                        }
                    }
                    Element sibling = next.nextElementSibling();
                    p.appendChild(next);
                    next = sibling;
                }
            }
        }
    }
    private String getInnerText(Element element, boolean normalizeSpaces){
        String textContent = element.text().trim();
        if(normalizeSpaces){
            return textContent.replace(REGEX.normalize.toString()," ");
        }else {
            return textContent;
        }
    }
    private int getCharCount(Element element, String split){
        return getInnerText(element,true).split(split).length -1;
    }
    private String getArticleTitle(){
        Document document = this.document;
        String curTitle = "";
        String oriTitle = "";
        curTitle = oriTitle = document.title();
        if(curTitle == null){
            curTitle = oriTitle = getInnerText(document.getElementsByTag("title").get(0), true);
        }
        if(Pattern.matches("/ [\\|\\-] /", curTitle)){
            curTitle = oriTitle.replace("/(.*)[\\|\\-] .*/gi", "$1");
        }
        if(curTitle.split(" ").length < 3){
            curTitle = oriTitle.replace("/[^\\|\\-]*[\\|\\-](.*)/gi", "$1");
        }else if(curTitle.indexOf(": ") != -1){
            Elements headings = document.select("h1,h2");
            String match = "";
            for (Element heading: headings){
                if(heading.text().equalsIgnoreCase(curTitle)){
                    match = heading.text();
                }
            }
            if(match.isEmpty()){
                curTitle = oriTitle.substring(oriTitle.lastIndexOf(":") + 1);
                if(curTitle.split(" ").length < 3){
                    curTitle = oriTitle.substring(oriTitle.indexOf(":") + 1);
                }
            }
        }else if(curTitle.length() > 150 || curTitle.length() < 15){
            Elements hOnes = document.getElementsByTag("h1");
            if(hOnes.size() == 1){
                curTitle = getInnerText(hOnes.first(), true);
            }
        }
        curTitle = curTitle.trim();
        if(curTitle.split(" ").length <= 4){
            curTitle = oriTitle;
        }
        return curTitle;
    }
    private void postProcessContent(Element articleContent){
        fixRelativeUris(articleContent);
    }
    private void fixRelativeUris(Element articleContent){
        for (Element anchor: articleContent.getElementsByTag("a")) {
            String href = anchor.attr("href");
            if(href != null){
                if(href.indexOf("javascript:") == 0){
                    anchor.replaceWith(document.createElement("p").text(anchor.text()));
                }else {
                    anchor.attr("href", toAbsoluteUri(href));
                }
            }
        }
        for (Element img: articleContent.getElementsByTag("img")){
            String src = img.attr("src");
            if(src != null){
                img.attr("src", toAbsoluteUri(src));
            }
        }

    }
    private String toAbsoluteUri(String uri){
        return this.uri.resolve(uri).toString();
    }
    private void clean(Element element, String tag){
        boolean isEmbed = "object,embed,iframe".contains(tag);
        for(Element embed : element.getElementsByTag(tag)){
            if(isEmbed){
                String attritbutes = TextUtils.join("|",embed.attributes().dataset().values());
                if (Pattern.matches(REGEX.videos.toString(), attritbutes)) {
                    break;
                }
                if(Pattern.matches(REGEX.videos.toString(), element.html())){
                    break;
                }
            }
            embed.remove();
        }
    }
    private void cleanConditionally(Element element, String tag){
        if(flagIsActive(FLAG_CLEAN_CONDITIONALLY)){
            Elements tagList = element.getElementsByTag(tag);
            int curTagsLength = tagList.size();
            boolean isList = tag.equalsIgnoreCase("ul") || tag.equalsIgnoreCase("ol");

            for (int i = curTagsLength -1; i >= 0; i -= 1){
                Element curTag = tagList.get(i);
                float weight = getClassWeight(curTag);
                int contentScore = 0;

                if(weight + contentScore < 0){
                    curTag.remove();
                }else if(getCharCount(curTag, ",") < 10){
                    int p = curTag.getElementsByTag("p").size();
                    int img = curTag.getElementsByTag("img").size();
                    int li = curTag.getElementsByTag("li").size();
                    int input = curTag.getElementsByTag("input").size();

                    int embedCount = 0;
                    Elements embeds = curTag.getElementsByTag("embed");
                    for(int ei = 0, il = embeds.size(); ei<il;ei += 1){
                        if(!Pattern.matches(REGEX.videos.toString(), embeds.get(ei).attr("src"))){
                            embedCount += 1;
                        }
                    }

                    float linkDensity = getLinkDensity(curTag);
                    int contentLength = getInnerText(curTag, true).length();
                    boolean toRemove = false;
                    if(img > p && !hasAncestorTag(curTag, "figure", 3)){
                        toRemove = true;
                    } else  if (!isList && li > p){
                        toRemove = true;
                    } else if(input > Math.floor(p/3)){
                        toRemove = true;
                    }else if (!isList && contentLength < 25 && (img == 0 || img > 2)) {
                        toRemove = true;
                    } else if (!isList && weight < 25 && linkDensity > 0.2) {
                        toRemove = true;
                    } else if (weight >= 25 && linkDensity > 0.5) {
                        toRemove = true;
                    } else if ((embedCount == 1 && contentLength < 75) || embedCount > 1) {
                        toRemove = true;
                    }
                    if(toRemove){
                        curTag.remove();
                    }
                }
            }
        }
    }
    private void cleanHeaders(Element element){
        for (int i = 0; i < 3; i++){
            Elements headers = element.getElementsByTag("h" + i);
            for (int j = headers.size() -1; i >= 0; i--){
                if(getClassWeight(headers.get(j)) < 0){
                    headers.get(j).remove();
                }
            }
        }
    }
    private boolean isValidByline(String byline){
        byline = byline.trim();
        return (byline.length() > 0) && (byline.length() < 100);
    }
    private boolean checkByline(Element node, String matchString){
        if(this.articleByLine != null){
            return false;
        }
        String rel = null;
        if(node.attr("rel") != null){
            rel = node.attr("rel");
        }
        if(rel != null && ((rel.equals("author") || Pattern.matches(REGEX.byline.toString(),matchString)) && isValidByline(node.text()))){
            this.articleByLine = node.text().trim();
            return true;
        }
        return false;
    }
    private float getLinkDensity(Element element){
        int textLength = getInnerText(element, true).length();
        if(textLength == 0){
            return 0;
        }
        int linkLength = 0;
        for (Element linkNode :element.getElementsByTag("a")) {
            linkLength += getInnerText(linkNode, true).length();
        }
        return linkLength / textLength;
    }
    private boolean hasAncestorTag(Element node, String tagName, int maxDepth){
        int depth = 0;
        while(node.parent() != null){
            if(depth > maxDepth){
                return false;
            }
            if(node.parent().tagName().equalsIgnoreCase(tagName)){
                return true;
            }
            node = node.parent();
            depth++;
        }
        return false;
    }
    private Element[] getNodeAncestors(Element node, int maxDepth){
        Element[] ancestors = new Element[maxDepth];
        int i = 0;
        while (node.parent() != null){
            ancestors[i] = node.parent();
            if(maxDepth > 0 && ++i == maxDepth){
                break;
            }
            node = node.parent();
        }
        return ancestors;
    }

    private Element grabArticle(){
        Document document = this.document;
        Element page = this.document.body();
        boolean isPaging = page != null;
        if(page == null){
            return null;
        }
        String pageCacheHtml = page.html();
        this.articleDir = document.ownerDocument().attr("dir");
        while (true){
            boolean stripUnlikelyCandidates = this.flagIsActive(this.FLAG_STRIP_UNLIKELYS);
            Elements elementsToScore = new Elements();
            Element node = this.document.parent();
            while (node != null){
                String matchString = node.className() + " " + node.id();
                if(checkByline(node, matchString)){
                    node = this.removeAndGetNext(node);
                }

                if(stripUnlikelyCandidates){
                    if(Pattern.matches(REGEX.unlikelyCandidates.toString(), matchString) &&
                            !Pattern.matches(REGEX.okMaybeItsACandidate.toString(),matchString) &&
                            !node.tagName().equalsIgnoreCase("BODY") && !node.tagName().equalsIgnoreCase("A")){
                        node = removeAndGetNext(node);
                    }
                }
                if(DEFAULT_TAGS_TO_SCORE.contains(node.tagName())){
                    elementsToScore.add(node);
                }
                if(node.tagName().equalsIgnoreCase("DIV")){
                    if(this.hasSinglePInsideElement(node)){
                        node = node.children().get(0);
                    }else if(!this.hasChildBlockElement(node)){
                        node = node.tagName("P");
                        elementsToScore.add(node);
                    }else{
                        for(Element child : node.children()){
                            Element p = document.createElement("p");
                            p.text(child.text());
                            p.attr("style", "display:inline");
                            child.replaceWith(p);
                        }
                    }
                }
                node = this.getNextNode(node, true);
            }
            Map<Element, Float> candidates = new HashMap<>();
            for (Element elementToScore: elementsToScore){
                if(elementToScore.parent() == null || elementToScore.parent().tagName() == null){
                    break;
                }
                String innerText = getInnerText(elementToScore, true);
                if(innerText.length() < 25){
                    break;
                }
                Element[] ancestors = getNodeAncestors(elementToScore, 3);
                if (ancestors.length == 0){
                    break;
                }
                int contentScore = 0;
                contentScore += 1;
                contentScore += innerText.split(",").length;
                contentScore += Math.min(Math.floor(innerText.length()/ 100), 3);
                for (int level = 0 ; level < ancestors.length; level++) {
                    Element ancestor = ancestors[level];
                    if(ancestor.tagName() != null){
                        candidates.put(ancestor, readablitiy(ancestor));
                    }
                    int scoreDivider;
                    if(level == 0){
                        scoreDivider = 1;
                    }else if(level == 1){
                        scoreDivider = 2;
                    }else{
                        scoreDivider = level * 3;
                    }
                    float ancestorScore = candidates.get(ancestor);
                    ancestorScore += contentScore / scoreDivider;
                    candidates.put(ancestor, ancestorScore );
                }
            }
            Map<Element, Float> topCandidates = new HashMap<>();
            for (int c = 0; c < candidates.size(); c++){
                Element candidate = (Element) candidates.keySet().toArray()[c];
                float score = (float) candidates.values().toArray()[c];
                float candidateScore = score * (1 - getLinkDensity(candidate));
                candidates.put(candidate, candidateScore);

                for(int t = 0; t < this.nbTopCandidates; t++){
                    Element aTopCandidate = (Element) topCandidates.keySet().toArray()[t];
                    float topScore = (float) topCandidates.values().toArray()[t];
                    if(aTopCandidate == null || candidateScore > topScore){
                        topCandidates.put(aTopCandidate, topScore);
                        if(topCandidates.size() > this.nbTopCandidates){
                            topCandidates.remove(aTopCandidate);
                        }
                        break;
                    }
                }
            }
            Element topCandidate;
            float topScore;
            if(topCandidates.size() != 0){
                topCandidate = (Element) topCandidates.keySet().toArray()[0];
                topScore = (float) topCandidates.values().toArray()[0];
            }else{
                topCandidate = null;
                topScore = 0f;
            }


            boolean neededToCreateTopCanidate = false;

            if(topCandidate == null || topCandidate.tagName().equalsIgnoreCase("BODY")){
                topCandidate = document.createElement("div");
                neededToCreateTopCanidate = true;
                Elements kids = page.children();
                while (kids.size() != 0){
                    topCandidate.appendChild(kids.get(0));
                }
                page.appendChild(topCandidate);
            }else if(topCandidate != null){
                Element parent = topCandidate.parent();
                float lastScore = topScore;
                float scoreThreshold = lastScore / 3;
                while (parent != null){
                    float parentScore = readablitiy(parent);
                    if (parentScore < scoreThreshold){
                        break;
                    }
                    if(parentScore > lastScore){
                        topCandidate = parent;
                        break;
                    }
                    lastScore = parentScore;
                    parent = parent.parent();
                }
            }

            Element articleContent = document.createElement("DIV");

            float siblingScoreThreshold =  Math.max(10f, readablitiy(topCandidate) * 0.2f);
            Elements siblings = topCandidate.siblingElements();
            for(int s = 0, sl = siblings.size(); s < sl; s++){
                Element sibling = siblings.get(s);
                boolean append = false;

                if(sibling == topCandidate){
                    append = true;
                }else{
                    float contentBonus = 0;
                    if(sibling.className().equalsIgnoreCase(topCandidate.className()) && !topCandidate.className().isEmpty()){
                        contentBonus += topScore * 0.2;
                    }
                    if(readablitiy(sibling) >= siblingScoreThreshold){
                        append = true;
                    }else if(sibling.tagName().equalsIgnoreCase("P")){
                        float density = getLinkDensity(sibling);
                        String content = getInnerText(sibling, true);
                        int contentLength = content.length();

                        if(contentLength > 80 && density < 0.25f){
                            append = true;
                        }else if(contentLength < 80 && contentLength > 0 && density == 0f && content.split("/\\.( |$)/").length > 0){
                            append = true;
                        }
                    }
                }
                if(append){
                    if(ALTER_TO_DIV_EXCEPTIONS.contains(sibling.tagName())){
                        sibling.tagName("DIV");
                    }
                    articleContent.appendChild(sibling);
                    s--;
                    sl--;
                }
                prepArticle(articleContent);
                Element div = document.createElement("DIV");
                Elements children = articleContent.children();
                while (children.size() != 0){
                    div.appendChild(children.get(0));
                }
                articleContent.appendChild(div);

                if(getInnerText(articleContent, true).length() < 500){
                    page.html(pageCacheHtml);

                    if (this.flagIsActive(this.FLAG_STRIP_UNLIKELYS)) {
                        this.removeFlag(this.FLAG_STRIP_UNLIKELYS);
                    } else if (this.flagIsActive(this.FLAG_WEIGHT_CLASSES)) {
                        this.removeFlag(this.FLAG_WEIGHT_CLASSES);
                    } else if (this.flagIsActive(this.FLAG_CLEAN_CONDITIONALLY)) {
                        this.removeFlag(this.FLAG_CLEAN_CONDITIONALLY);
                    } else {
                        return null;
                    }
                } else {
                    return articleContent;
                }
            }
        }


    }

    private boolean hasSinglePInsideElement(Element node){
        if(node.children().size() != 1 || !node.children().get(0).tagName().equalsIgnoreCase("P")){
            return false;
        }
        return !Pattern.matches(REGEX.hasContent.toString(), node.children().text());
    }

    private boolean hasChildBlockElement(Element element){
        for(Element node: element.children()){
            boolean hasChild = hasChildBlockElement(node);
            if(this.DIV_TO_P_ELEMS.contains(node.tagName()) || hasChild){
                return this.DIV_TO_P_ELEMS.contains(node.tagName()) || hasChild;
            }
        }
        return false;
    }

    public void cleanStyles(Element element){
        Element cur = element.child(0);
        element.removeAttr("style");
        while (cur != null){
            cleanStyles(cur);
            cur = cur.nextElementSibling();
        }

    }

    private void prepArticle(Element articleContent){
        this.cleanStyles(articleContent);

        this.cleanConditionally(articleContent, "form");
        this.clean(articleContent, "object");
        this.clean(articleContent, "embed");
        this.clean(articleContent, "h1");
        this.clean(articleContent, "footer");

        if(articleContent.getElementsByTag("h2").size() == 1){
            this.clean(articleContent, "h2");
        }
        this.clean(articleContent, "iframe");
        this.cleanHeaders(articleContent);

        this.cleanConditionally(articleContent, "table");
        this.cleanConditionally(articleContent, "ul");
        this.cleanConditionally(articleContent, "div");

        for(Element paragraph: articleContent.getElementsByTag("p")){
            int imgCount = paragraph.getElementsByTag("img").size();
            int embedCount = paragraph.getElementsByTag("embed").size();
            int objectCount = paragraph.getElementsByTag("object").size();
            int iframeCount = paragraph.getElementsByTag("iframe").size();
            int totalCount = imgCount + embedCount + objectCount + iframeCount;

            if (totalCount == 0 && this.getInnerText(paragraph, false) == null)
                paragraph.parent().children().remove(paragraph);
        }
        for (Element br: articleContent.getElementsByTag("br")){
            Element next = nextElement(br.nextElementSibling());
            if(next != null && next.tagName().equalsIgnoreCase("p")){
                br.parent().children().remove(br);
            }
        }
    }
    public Article parse() throws Exception {
        if(maxElemsToParse > 0){
            int numTags = this.document.select("*").size();
            if(numTags > maxElemsToParse){
                throw new Exception("Aborting parsing document; " + numTags + " elements found");
            }
        }
//        if(this.document.children().first() == null){
//            getNextElement() = getNextElementNoProperties();
//        }
        removeScripts(this.document);

        this.prepDocument();
        Metadata metadata = this.getArticleMetadata();
        String articleTitle;
        if(metadata.Title != null){
            articleTitle =  metadata.Title;
        }else {
            articleTitle = this.getArticleTitle();
        }

        Element articleContent = this.grabArticle();
        if(articleContent == null){
            return null;
        }
        postProcessContent(articleContent);
        if(metadata.Excerpt == null){
            Elements paragraphs = articleContent.getElementsByTag("p");
            if(paragraphs.size() > 0){
                metadata.Excerpt = paragraphs.get(0).text().trim();
            }
        }
        Article article = new Article();
        article.URL = "";
        article.Title = articleTitle;
        if(metadata.ByLine != null){
            article.ByLine = metadata.ByLine;
        }else if(this.articleByLine != null){
            article.ByLine = this.articleByLine;
        }
        article.Dir = this.articleDir;
        article.Content = articleContent.text();
        article.Excerpt = metadata.Excerpt;
        return article;

    }
}
