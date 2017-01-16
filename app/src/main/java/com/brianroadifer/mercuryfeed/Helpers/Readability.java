package com.brianroadifer.mercuryfeed.Helpers;

import android.app.Notification;
import android.support.v4.app.NotificationCompat;

import com.brianroadifer.mercuryfeed.Models.Article;
import com.brianroadifer.mercuryfeed.Models.Metadata;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Brian Roadifer on 6/29/2016.
 */


public class Readability {
    private static final String CONTENT_SCORE = "readabilityContentScore";
    private final Document document;
    private String cache;
    private String byline;

    public Readability(Document document) {
        this.document = document;
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
    public final String html(){
        return document.html();
    }
    public final String outerHtml(){
        return document.outerHtml();
    }
    private static void placeContentScore(Element node){
        node.attr(CONTENT_SCORE, Integer.toString(0));

        int readability = 0;

        switch(node.tagName().toUpperCase()) {
            case "DIV":
                incrementContentScore(node, 5);
                break;

            case "PRE":
            case "TD":
            case "BLOCKQUOTE":
                incrementContentScore(node, 3);
                break;

            case "ADDRESS":
            case "OL":
            case "UL":
            case "DL":
            case "DD":
            case "DT":
            case "LI":
            case "FORM":
                incrementContentScore(node, -3);
                break;

            case "H1":
            case "H2":
            case "H3":
            case "H4":
            case "H5":
            case "H6":
            case "TH":
                incrementContentScore(node, -5);
                break;
        }
        incrementContentScore(node, getClassWeight(node));
    }
    private static int getClassWeight(Element node){
        int weight = 0;
        if(node.className() != null && !node.className().isEmpty()){
            Matcher negM = Patterns.get(Patterns.RegEx.NEGATIVE).matcher(node.className());
            Matcher posM = Patterns.get(Patterns.RegEx.POSITIVE).matcher(node.className());
            if(negM.find()){
                weight -= 25;
            }
            if(posM.find()){
                weight += 25;
            }
        }
        if(node.id() != null && !node.id().isEmpty()){
            Matcher negM = Patterns.get(Patterns.RegEx.NEGATIVE).matcher(node.id());
            Matcher posM = Patterns.get(Patterns.RegEx.POSITIVE).matcher(node.id());
            if(negM.find()){
                weight -= 25;
            }
            if(posM.find()){
                weight += 25;
            }
        }
        return weight;
    }
    protected void prepDocument(){
        if(document.body() == null){
            document.body().appendElement("body");
        }
        document.getElementsByTag("script").remove();
        document.getElementsByTag("noscript").remove();
        document.getElementsByTag("style").remove();
        destroyBreaks(document.body());
        document.getElementsByTag("font").tagName("span");
    }
    public static void destroyBreaks(Element element){
        element.html(element.html().replaceAll(Patterns.REGEX_KILL_BREAKS, "<br />"));
    }
    private static String getInnerText(Element element, boolean normalizeSpaces){
        String textContent = element.text().trim();
        if(normalizeSpaces){
            return textContent.replaceAll(Patterns.REGEX_NORMALIZE,"");
        }
        return textContent;
    }
    private static int getCharCount(Element element, String split){
        if(split == null || split.length() == 0){
            split = ",";
        }
        return getInnerText(element,true).split(split).length;
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
    private static void clean(Element element, String tag){
        Elements targets = getElementsByTag(element, tag);
        boolean isEmbed = "object".equalsIgnoreCase(tag)|| "embed".equalsIgnoreCase(tag)|| "iframe".equalsIgnoreCase(tag);
        for(Element tar : targets){
            Matcher matcher = Patterns.get(Patterns.RegEx.VIDEO).matcher(tar.outerHtml());
            if(isEmbed && matcher.find()){
                continue;
            }
            tar.remove();
        }
    }
    private void cleanConditionally(Element element, String tag){
            Elements tagList = getElementsByTag(element, tag);
            int curTagsLength = tagList.size();

            for (Element node: tagList){
                debug("Cleaning Conditionally (" + node.className() + ":" + node.id() + ")" + getContentScore(node));
                int weight = getClassWeight(node);
                if(weight < 0){
                    node.remove();
                }else if(getCharCount(node, ",") < 10){
                    int p = node.getElementsByTag("p").size();
                    int img = node.getElementsByTag("img").size();
                    int li = node.getElementsByTag("li").size();
                    int input = node.getElementsByTag("input").size();

                    int embedCount = 0;
                    Elements embeds = getElementsByTag(node,"embed");
                    for(Element embed:embeds){
                        if(!Patterns.get(Patterns.RegEx.VIDEO).matcher(embed.absUrl("src")).find()){
                            embedCount += 1;
                        }
                    }

                    float linkDensity = getLinkDensity(node);
                    int contentLength = getInnerText(node, true).length();
                    boolean toRemove = false;
                    if(img > p){
                        toRemove = true;
                    } else  if (li > p && !"ul".equalsIgnoreCase(tag) && !"ol".equalsIgnoreCase(tag)){
                        toRemove = true;
                    } else if(input > Math.floor(p/3)){
                        toRemove = true;
                    }else if (contentLength < 25 && (img == 0 || img > 2)) {
                        toRemove = true;
                    } else if (weight < 25 && linkDensity > 0.2f) {
                        toRemove = true;
                    } else if (weight >= 25 && linkDensity > 0.5f) {
                        toRemove = true;
                    } else if ((embedCount == 1 && contentLength < 75) || embedCount > 1) {
                        toRemove = true;
                    }
                    if(toRemove){
                        node.remove();
                    }
                }
            }
    }
    private static void cleanHeaders(Element element){
        for (int i = 1; i < 7; i++){
            Elements headers = element.getElementsByTag("h" + i);
            for (Element header: headers){
                if(getClassWeight(header) < 0 || getClassWeight(header) > 0.33f){
                    headers.remove();
                }
            }
        }
    }

    private static float getLinkDensity(Element element){
        int textLength = getInnerText(element, true).length();
        if(textLength == 0){
            return 0;
        }
        float linkLength = 0.0f;
        for (Element linkNode :getElementsByTag(element, "a")) {
            linkLength += getInnerText(linkNode, true).length();
        }
        return linkLength / textLength;
    }

    protected Element grabArticle(boolean saveUnlikelyCanidates){
       for (Element node : document.getAllElements()) {

           if (!saveUnlikelyCanidates) {
               String unlikelyMatch = node.className() + node.id();
               articleByline(node, unlikelyMatch);
               Matcher ucm = Patterns.get(Patterns.RegEx.UNLIKELY_CANDIDATES).matcher(unlikelyMatch);
               Matcher ocm = Patterns.get(Patterns.RegEx.OK_MAYBE_ITS_A_CANDIDATE).matcher(unlikelyMatch);
               if (ucm.find() && ocm.find() && !"body".equalsIgnoreCase(node.tagName())) {
                   node.remove();
                   debug("Removing Unlikely Candidate - " + unlikelyMatch);
                   continue;
               }
           }
           if ("div".equalsIgnoreCase(node.tagName())) {
               Matcher dpm = Patterns.get(Patterns.RegEx.DIV_TO_P_ELEMENTS).matcher(node.html());
               if (!dpm.find()) {
                   debug("Changing " + node.tagName() + " to div: " + node);
                   try {
                       node.tagName("p");
                   } catch (Exception e) {
                       debug("Could not alter due to possible IE restriction, no changes made", e);
                   }
               }
           }
       }
        Elements parapgraphs = document.getElementsByTag("p");
        List<Element> canidates = new ArrayList<>();
        for (Element para : parapgraphs) {
            Element parent = para.parent();
            Element grandparent = parent.parent();
            String innerText = getInnerText(para, true);
            if (innerText.length() < 25) {
                continue;
            }
            if (!parent.hasAttr(CONTENT_SCORE)) {
                placeContentScore(parent);
                canidates.add(parent);
            }
            if (!grandparent.hasAttr(CONTENT_SCORE)) {
                placeContentScore(grandparent);
                canidates.add(grandparent);
            }
               int score = 0;
               score++;
               score += innerText.split(",").length;
               score += Math.min(Math.floor(innerText.length() / 100), 3);
               incrementContentScore(parent, score);
               incrementContentScore(grandparent, score / 2);
        }

        Element top = null;
        for(Element candidate: canidates){
            scaleContentScore(candidate, 1-getLinkDensity(candidate));
            debug("Candidate: ("+candidate.className()+":"+candidate.id()+":"+getContentScore(candidate)+")");
            if (top == null || getContentScore(candidate) > getContentScore(top)){
                top = candidate;
            }
        }
        if(top == null || "body".equalsIgnoreCase(top.tagName())){
            top = document.createElement("div");
            top.html(document.body().html());
            document.body().html("");
            document.body().appendChild(top);
            placeContentScore(top);
        }

        Element articleContent = document.createElement("div");
        articleContent.attr("id", "readability-content");
        int siblingScoreThreshold = Math.max(10, (int)(getContentScore(top) * 0.2f));
        Elements siblings = top.parent().children();
        for(Element sibling : siblings){
            boolean append = true;
            debug("Sibling Node: ("+sibling.className()+":"+sibling.id()+":"+getContentScore(sibling)+")");

            if(sibling == top){
                append = true;
            }
            if(getContentScore(sibling) >=siblingScoreThreshold){
                append =true;
            }
            if("p".equalsIgnoreCase(sibling.tagName())){
                float density = getLinkDensity(sibling);
                String content = getInnerText(sibling, true);
                int length = content.length();

                if(length > 80 && density < 0.25f){
                    append = true;
                }else if(length<80 && density == 0.0f){
                    append = true;
                }
            }
            if(append){
                debug("Appending Sibling: " + sibling);
                articleContent.appendChild(sibling);
                continue;
            }
        }
        prepArticle(articleContent);
        return articleContent;
    }

    public static void cleanStyles(Element element){
        if(element == null){
            return;
        }
        if (!"readability-styled".equals(element.className())) {
            element.removeAttr("style");
        }
        Element cur = element.children().first();
        while (cur != null){
            if (!"readability-styled".equals(cur.className())) {
                cur.removeAttr("style");
            }
            cleanStyles(cur);
            cur = cur.nextElementSibling();
        }

    }
    private void articleByline(Element node, String match){
        String rel = "";
        if(node.attributes().size() != 0){
            rel = node.attr("rel");
        }
        Matcher bym = Patterns.get(Patterns.RegEx.BYLINE).matcher(match);
        if(rel.equalsIgnoreCase("author") || bym.find() || isValidByline(node.text())){
            byline = node.text().trim();
        }
    }
    private boolean isValidByline(String text){
        if(text != null){
            text = text.trim();
            return (text.length() > 0) && (text.length() < 100);
        }
        return false;
    }

    private void prepArticle(Element articleContent){
        cleanStyles(articleContent);
        destroyBreaks(articleContent);

        clean(articleContent, "form");
        clean(articleContent, "object");
        clean(articleContent, "h1");

        if(articleContent.getElementsByTag("h2").size() == 1){
            clean(articleContent, "h2");
        }
        clean(articleContent, "iframe");
        cleanHeaders(articleContent);

        cleanConditionally(articleContent, "table");
        cleanConditionally(articleContent, "ul");
        cleanConditionally(articleContent, "div");

        for(Element paragraph: articleContent.getElementsByTag("p")){
            int imgCount = paragraph.getElementsByTag("img").size();
            int embedCount = paragraph.getElementsByTag("embed").size();
            int objectCount = paragraph.getElementsByTag("object").size();
            int totalCount = imgCount + embedCount + objectCount;

            if (totalCount == 0 && getInnerText(paragraph, false).isEmpty())
                paragraph.remove();
        }
        try{
            articleContent.html(articleContent.html().replaceAll("(?i)<br[^>]*>\\s<p", "<p"));
        }catch (Exception e){
            debug("Cleaning innerHTML breaks due to IE strict-block");
        }
    }
    public Article parse(){
        return parse(false);
    }
    private Article parse(boolean keepUnlikelys){
        if(document.body() == null && cache == null){
            cache = document.body().html();
        }
        this.prepDocument();

        Article article = new Article();
        Metadata metadata = this.getArticleMetadata();
        if(metadata.Title != null){
            article.Title =  metadata.Title;
        }else {
            article.Title = this.getArticleTitle();
        }
        if( metadata.ByLine != null){
            article.ByLine = metadata.ByLine;
        }else if(byline != null){
            article.ByLine = byline;
        }else {
            article.ByLine = "";
        }
        Element overlay = document.createElement("div");
        Element innerDiv = document.createElement("div");
        Element title = document.createElement("h1");
        title.html( );
        Element byline = document.createElement("h4");
        byline.html(article.ByLine);
        Element articleContent = this.grabArticle(keepUnlikelys);
        if(getInnerText(articleContent, false).isEmpty()){
            if(!keepUnlikelys){
                document.body().html(cache);
                return parse(true);
            }else{
                articleContent.html("<p>Sorry, unable to parse web page</p>");
            }
        }
        if(metadata.Excerpt == null){
            Elements paragraphs = articleContent.getElementsByTag("p");
            if(paragraphs.size() > 0){
                metadata.Excerpt = paragraphs.get(0).text().trim();
            }
        }
        innerDiv.appendChild(title);
        innerDiv.appendChild(byline);
        innerDiv.appendChild(articleContent);
        overlay.appendChild(innerDiv);
        article.URL = "";
        article.Content = articleContent.text();
        article.Excerpt = metadata.Excerpt;
        return article;

    }

    private static Elements getElementsByTag(Element e, String tag) {
        Elements es = e.getElementsByTag(tag);
        es.remove(e);
        return es;
    }
    private static int getContentScore(Element node) {
        try {
            return Integer.parseInt(node.attr(CONTENT_SCORE));
        }catch(NumberFormatException e) {
            return 0;
        }
    }
    private static Element incrementContentScore(Element node, int increment) {
        int score = getContentScore(node);
        score += increment;
        node.attr(CONTENT_SCORE, Integer.toString(score));
        return node;
    }
    private static Element scaleContentScore(Element node, float scale) {
        int score = getContentScore(node);
        score *= scale;
        node.attr(CONTENT_SCORE, Integer.toString(score));
        return node;
    }
    protected void debug(String message){
        debug(message, null);
    }
    protected void debug(String message, Throwable throwable){
        System.out.println(message + (throwable != null ?("\n" + throwable.getMessage()) : "" )+(throwable != null? ("\n" + throwable.getStackTrace()): ""));
    }
    private static class Patterns {
        private static Pattern unlikelyCandidates;
        private static Pattern okMaybeItsACandidate;
        private static Pattern positive;
        private static Pattern negative;
        private static Pattern divToPElements;
        private static Pattern videos;
        private static Pattern byline;
        private static final String REGEX_REPLACE_BRS = "(?i)(<br[^>]*>[ \n\r\t]*){2,1}";
        private static final String REGEX_REPLACE_FONTS = "(?i)<(\\/font[^>]*>)";
        private static final String REGEX_NORMALIZE = "\\s{2,}";
        private static final String REGEX_KILL_BREAKS = "(<br\\s*\\/?>(\\s|&nbsp;?)*){1,}";

        public enum RegEx{
            UNLIKELY_CANDIDATES,BYLINE, OK_MAYBE_ITS_A_CANDIDATE,POSITIVE,NEGATIVE,DIV_TO_P_ELEMENTS,VIDEO;
        }
        public static Pattern get(RegEx regEx){
            switch (regEx){
                case UNLIKELY_CANDIDATES:
                    if(unlikelyCandidates == null){
                        unlikelyCandidates = Pattern.compile("combox|comment|foot|header|menu|meta|nav|rss|shoutbox|sidebar|sponser", Pattern.CASE_INSENSITIVE);
                    }
                    return unlikelyCandidates;
                case OK_MAYBE_ITS_A_CANDIDATE:
                    if(okMaybeItsACandidate == null){
                        okMaybeItsACandidate = Pattern.compile("and|article|body|column|main", Pattern.CASE_INSENSITIVE);
                    }
                    return okMaybeItsACandidate;
                case POSITIVE: {
                    if (positive == null) {
                        positive = Pattern.compile("article|body|content|entry|hentry|page|pagination|post|text", Pattern.CASE_INSENSITIVE);
                    }
                    return positive;
                }
                case NEGATIVE: {
                    if (negative == null) {
                        negative = Pattern.compile("combx|comment|contact|foot|footer|footnote|link|media|meta|promo|related|scroll|shoutbox|sponsor|tags|widget", Pattern.CASE_INSENSITIVE);
                    }
                    return negative;
                }
                case DIV_TO_P_ELEMENTS: {
                    if (divToPElements == null) {
                        divToPElements= Pattern.compile("<(a|blockquote|dl|div|img|ol|p|pre|table|ul)", Pattern.CASE_INSENSITIVE);
                    }
                    return divToPElements;
                }
                case VIDEO: {
                    if (videos == null) {
                        videos = Pattern.compile("http:\\/\\/(www\\.)?(youtube|vimeo)\\.com",Pattern.CASE_INSENSITIVE);
                    }
                    return videos;
                }
                case BYLINE:
                    if(byline == null){
                        byline = Pattern.compile("/byline|author|ateline|writtenby/i", Pattern.CASE_INSENSITIVE);
                    }
                    return byline;
            }
            return null;
            }
        }
}
