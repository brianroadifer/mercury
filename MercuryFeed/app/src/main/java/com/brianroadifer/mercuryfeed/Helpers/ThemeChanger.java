package com.brianroadifer.mercuryfeed.Helpers;

import android.content.Context;
import android.graphics.Typeface;

import com.brianroadifer.mercuryfeed.R;

public class ThemeChanger {

    private final static String ACCENT = "blue";
    private final static String PRIMARY = "blue";
    private final static String STATUS = "blue";
    private final static String NAVIGATION = "black";

    private String accent = "blue";
    private String primary = "blue";
    private String status = "blue";
    private String navigation = "black";
    private final Context context;

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

    private String modString(String string){
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
            this.navigation = NAVIGATION;
        }

    }

    public void statusColor(String status){
        if(!status.isEmpty()){
            this.status = modString(status);
        }else{
            this.status = STATUS;
        }

    }

    public Typeface selectTypeFace(String typeface){
        return Typeface.createFromAsset(context.getAssets(), "fonts/"+typeface+".ttf");
    }

}
