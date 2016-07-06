package com.brianroadifer.mercuryfeed.Models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Brian Roadifer on 6/30/2016.
 */
public class Article implements Serializable {

    public String ID;
    public String Title;
    public String Content;
    public String MetaData;
    public transient List<Tag> Tags;
}
