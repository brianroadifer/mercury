package com.brianroadifer.mercuryfeed.Helpers;

import android.text.TextUtils;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Brian Roadifer on 7/31/2016.
 */
public class ColorConverter {

    int R;
    int G;
    int B;

    public String getHEX() {
        return HEX;
    }

    public void setHEX(String HEX) {
        this.HEX = HEX;
    }

    String HEX;


    public int getR() {
        return R;
    }

    public void setR(int r) {
        R = r;
    }

    public int getG() {
        return G;
    }

    public void setG(int g) {
        G = g;
    }

    public int getB() {
        return B;
    }

    public void setB(int b) {
        B = b;
    }

    public ColorConverter(int R, int G, int B){
        setR(R);
        setG(G);
        setB(B);
        toHex();
    }
    public ColorConverter(String HEX){
        setHEX(HEX);
        toRGB();
    }

    public String toHex(){
        setHEX( "#"+componentToHex(getR())+componentToHex(getG())+componentToHex(getB()));
        return getHEX();
    }
    public Triple<Integer,Integer,Integer> toRGB(){
        String h = getHEX().replace("#","");
        String r = "";
        String g = "";
        String b = "";
        if(isValidedHex(h)){
            if(getHEX().length() == 6){
                r = h.substring(0,1);
                g = h.substring(2,3);
                b = h.substring(4,5);
            }else if(getHEX().length() == 3){
                r = h.substring(0,0);
                g = h.substring(1,1);
                b = h.substring(2,2);
            }
            setR(componentToRGB(r));
            setG(componentToRGB(g));
            setB(componentToRGB(b));

            return new Triple<>(getR(), getG(), getB());
        }
        return new Triple<>(0,0,0);

    }

    private String componentToHex(int c){
        String hex = Integer.toHexString(c);
        hex = hex.length() == 1? "0" + hex : hex;
        return hex;
    }
    private int componentToRGB(String hex){
        return Integer.parseInt(hex, 16);
    }
    private boolean isValidedHex(String hex){
        Pattern pattern = Pattern.compile("#?([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})");
        Matcher matcher = pattern.matcher(hex);
        if(matcher.matches())
            return true;
        return false;
    }
}

class Triple<T,U,V>{

    private final T t;
    private final U u;
    private final V v;

    Triple(T t, U u, V v){
        this.t = t;
        this.u = u;
        this.v = v;
    }

    public T getT() {
        return t;
    }

    public U getU() {
        return u;
    }

    public V getV() {
        return v;
    }
}
