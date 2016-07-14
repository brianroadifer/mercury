package com.brianroadifer.mercuryfeed.Helpers;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;

import com.brianroadifer.mercuryfeed.Models.Feed;
import com.brianroadifer.mercuryfeed.Models.Item;

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
public class ReadRss extends AsyncTask<Feed, Void, Feed>{
    Context context;
    Feed feed;
    ProgressDialog progress;
    URL url;
    public ArrayList<Item> feedItems;
    RecyclerView recyclerView;
    boolean atomFeed = false;
    int channelSpot = 0;

    public ReadRss(Context context, RecyclerView recyclerView){
        this.context = context;
        this.recyclerView = recyclerView;
        progress = new ProgressDialog(context, ProgressDialog.STYLE_SPINNER);
        progress.setMessage("Loading Feed...");
    }
    @Override
    protected Feed doInBackground(Feed... params) {
        feed = params[0];
        ProcessXml(GetData(params[0].FeedUrl));
        return feed;
    }

    public void ProcessXml(Document data) {
        if(data != null){
            feedItems = new ArrayList<>();
            Element root =  data.getDocumentElement();
            Node channel;
            if(root.getNodeName().equalsIgnoreCase("feed")){
                channel = root;
                atomFeed = true;
            }else{
                for (int i = 0 ; i < root.getChildNodes().getLength(); i++) {
                    if(root.getChildNodes().item(i).getNodeName().equalsIgnoreCase("channel"))
                        channelSpot = i;
                }
                channel = root.getChildNodes().item(channelSpot);
            }

            NodeList items = channel.getChildNodes();
            String TITLE = "";
            for (int i = 0; i< items.getLength(); i++) {
                Node node = items.item(i);
                if(node.getNodeName().equalsIgnoreCase("title")){
                    feed.Title = (node.getTextContent());
                }
                if(node.getNodeName().equalsIgnoreCase("item") || node.getNodeName().equalsIgnoreCase("entry")){
                    NodeList nodeChilds = node.getChildNodes();
                    Item item = new Item();
                    item.setHeadTitle(TITLE);
                    for (int j = 0; j< nodeChilds.getLength(); j++){
                        Node current = nodeChilds.item(j);
                        if(current.getNodeName().equalsIgnoreCase("title")){
                            item.setTitle(current.getTextContent());
                        }else if(current.getNodeName().equalsIgnoreCase("link")){

                            if(atomFeed){
                                String url = current.getAttributes().getNamedItem("href").getTextContent();
                                item.setLink(url);
                            }else{
                                item.setLink(current.getTextContent());
                            }
                        }
                        else if(current.getNodeName().equalsIgnoreCase("description") || current.getNodeName().equalsIgnoreCase("content")){
                            item.setDescription(current.getTextContent());
                        }
                        else if(current.getNodeName().equalsIgnoreCase("pubDate") || current.getNodeName().equalsIgnoreCase("published")) {
                            item.setPubDate(current.getTextContent());
                        }
                        else if(current.getNodeName().equalsIgnoreCase("media:thumbnail") || current.getNodeName().equalsIgnoreCase("media:content")) {
                            String url = current.getAttributes().getNamedItem("url").getTextContent();
                            item.setThumbnailUrl(url);
                        }
                        else if(current.getNodeName().equalsIgnoreCase("media:credit") || current.getNodeName().equalsIgnoreCase("dc:creator") || current.getNodeName().equalsIgnoreCase("author")) {
                            if(atomFeed){
                                item.setAuthor(current.getChildNodes().item(0).getTextContent());
                            }else{
                                item.setAuthor(current.getTextContent());
                            }

                        }

                    }
                    this.feedItems.add(item);
                }
            }
            feed.Items = (feedItems);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progress.show();
    }


    @Override
    protected void onPostExecute(Feed aVoid) {
        super.onPostExecute(aVoid);
        progress.dismiss();
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
