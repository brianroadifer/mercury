package com.brianroadifer.mercuryfeed;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by Brian Roadifer on 5/28/2016.
 */
public class ReadRss extends AsyncTask<String, Void, Void>{
    Context context;
    String[] address = {"http://rss.nytimes.com/services/xml/rss/nyt/World.xml", "http://www.cgpgrey.com/blog?format=rss"};
    ProgressDialog progress;
    URL url;
    ArrayList<Feed> feeds;
    RecyclerView recyclerView;
    int channelSpot = 0;

    public ReadRss(Context context, RecyclerView recyclerView){
        this.context = context;
        this.recyclerView = recyclerView;
        progress = new ProgressDialog(context);
        progress.setMessage("Loading RSS...");
    }
    @Override
    protected Void doInBackground(String... params) {
        ProcessXml(GetData(address[1]));
        return null;
    }

    private void ProcessXml(Document data) {
        if(data != null){
            feeds = new ArrayList<>();
            Element root =  data.getDocumentElement();
            for (int i = 0 ; i < root.getChildNodes().getLength() -1; i++) {
                if(root.getChildNodes().item(i).getNodeName().equalsIgnoreCase("channel"))
                    channelSpot = i;
            }
            Node channel = root.getChildNodes().item(channelSpot);
            NodeList items = channel.getChildNodes();
            String TITLE = "";
            for (int i = 0; i< items.getLength(); i++) {
                Node node = items.item(i);
                if(node.getNodeName().equalsIgnoreCase("title")){
                    TITLE = node.getTextContent();
                }
                if(node.getNodeName().equalsIgnoreCase("item")){
                    NodeList nodeChilds = node.getChildNodes();
                    Feed item = new Feed();
                    item.setHeadTitle(TITLE);
                    for (int j = 0; j< nodeChilds.getLength(); j++){
                        Node current = nodeChilds.item(j);
                        if(current.getNodeName().equalsIgnoreCase("title")){
                            item.setTitle(current.getTextContent());
                        }else if(current.getNodeName().equalsIgnoreCase("link")){
                            item.setLink(current.getTextContent());
                        }
                        else if(current.getNodeName().equalsIgnoreCase("description")){
                            item.setDescription(current.getTextContent());
                        }
                        else if(current.getNodeName().equalsIgnoreCase("pubDate")) {
                            item.setPubDate(current.getTextContent());
                        }
                        else if(current.getNodeName().equalsIgnoreCase("media:thumbnail") || current.getNodeName().equalsIgnoreCase("media:content")) {
                            String url = current.getAttributes().getNamedItem("url").getTextContent();
                            item.setThumbnailUrl(url);
                        }
                        else if(current.getNodeName().equalsIgnoreCase("media:credit") || current.getNodeName().equalsIgnoreCase("dc:creator")) {
                            item.setAuthor(current.getTextContent());
                        }

                    }
                    feeds.add(item);
                }
            }
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        MyAdapter adapter = new MyAdapter(feeds, context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
    }

    public Document GetData(String rssUrl){
        try {
                url = new URL(rssUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStream inputStream = connection.getInputStream();
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                Document xmlDoc = builder.parse(inputStream);
                return xmlDoc;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
