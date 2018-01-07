package com.brianroadifer.mercuryfeed.Helpers;

import android.content.Context;
import android.util.Log;

import com.brianroadifer.mercuryfeed.Models.Tag;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class TagHelper {

    public final static String FILENAME = "mercury_tags_";
    private FileOutputStream fos;
    private ObjectOutputStream os;
    private FileInputStream fis;
    private ObjectInputStream is;
    private final Context context;

    public TagHelper(Context context){
        this.context = context;
    }

    /**
     * Save a single tag to read offline later
     * @param tag Article that is saved to the device
     */
    private void SaveTag(Tag tag){
        try {
            this.fos = this.context.openFileOutput(FILENAME + tag.ID, Context.MODE_PRIVATE);
            this.os = new ObjectOutputStream(this.fos);
            this.os.writeObject(tag);
            this.os.close();
            this.fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("Tag:Save", tag.Name + " was saved successfully");
    }

    public void SaveTags(List<Tag> tags){
        for(Tag tag: tags){
            SaveTag(tag);
        }
    }

    private Tag LoadTag(String fileName){
        Log.d("Tag:Load", "Loading " + fileName);
        try{
            this.fis = this.context.openFileInput(fileName);
            this.is = new ObjectInputStream(this.fis);
            Tag tag = (Tag) this.is.readObject();
            this.is.close();
            this.fis.close();
            Log.d("Tag:Load", tag.Name + "was successfully loaded");
            return tag;
        }catch (IOException | ClassNotFoundException e){
            Log.e("Tag:ERROR", e.toString());
            e.printStackTrace();
        }
        return null;
    }

    public List<Tag> LoadTags(){
        List<Tag> tags = new ArrayList<>();
        File[] files = this.context.getFilesDir().listFiles();
        for (File file: files) {
            if (file.isFile() && file.getName().contains(FILENAME)){
                Tag tag = LoadTag(file.getName());
                if(tag != null){
                    tags.add(tag);
                }

            }
        }
        return  tags;
    }

    /**
     * Delete a specific article
     * @param filename file that will be deleted
     * @return boolean on state of tag
     */
    public boolean DeleteTag(String filename){
        Log.d("Tag:Delete", filename + " was successfully deleted" );
        return context.deleteFile(filename);
    }



    /**
     * Deletes all articles stored on the device
     *
     */
    public void DeleteTags(){
        File[] files = context.getFilesDir().listFiles();
        for (File file: files) {
            if(file.isFile() && file.getName().contains(FILENAME)){
                DeleteTag(file.getName());
            }
        }

    }
}
