package com.brianroadifer.mercuryfeed.Helpers;

import android.content.Context;
import android.content.res.Resources;

import com.brianroadifer.mercuryfeed.R;


/**
 * Created by Brian Roadifer on 8/1/2016.
 */
public class ThemeChanger {

    final static String ACCENT = "blue";
    final static String PRIMARY = "blue";
    final static String STATUS = "blue";
    final static String NAVIGATION = "black";

    String accent = "blue";
    String primary = "blue";
    String status = "blue";
    String navigation = "black";
    private Context context;

    public ThemeChanger(Context context){
        this.context = context;
    }

    public void screenColor(String screen){
        switch (screen.toLowerCase()){
            case "light":
                context.setTheme(R.style.App_Screen_Light);
                break;
            case "dark":
                context.setTheme(R.style.App_Screen_Dark);
                break;
            case "white":
                context.setTheme(R.style.App_Screen_White);
                break;
            case "black":
                context.setTheme(R.style.App_Screen_Black);
                break;
            default:
                context.setTheme(R.style.App_Screen_Light);
        }
        changeTheme();
    }
    private int getId(){
        String name = accent + "_" + primary + "_" + status + "_" + navigation;
        return context.getResources().getIdentifier(name, "style", context.getPackageName());
    }

    public void changeTheme(){
        int id = getId();
        context.getTheme().applyStyle(id, true);
    }

    public String modString(String string){
      return  string.toLowerCase().replace(" ", "_");
    }

    public void primaryColor(String primary){
        if(!primary.isEmpty()){
            this.primary = modString(primary);
        }else{
            this.primary = PRIMARY;
        }
    }

    public void accentColor(String accent) {
        if(!accent.isEmpty()){
            this.accent = modString(accent);
        }else {
            this.accent = ACCENT;
        }

    }

    public void navigationColor(String navigation){
        if(!navigation.isEmpty()){
            this.navigation = modString(navigation);
        }else{
            this.navigation = navigation;
        }

    }

    public void statusColor(String status){
        if(!status.isEmpty()){
            this.status = modString(status);
        }else{
            this.status = STATUS;
        }

    }

}
