package com.brianroadifer.mercuryfeed.Models;

import java.io.Serializable;

/**
 * Created by Brian Roadifer on 6/30/2016.
 */
public class Article implements Serializable {

    public String Title;
    public String Content;
    public String MetaData;
    public Tag[] Tags;
}
