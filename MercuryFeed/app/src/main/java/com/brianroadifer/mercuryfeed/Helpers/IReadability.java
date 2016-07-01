package com.brianroadifer.mercuryfeed.Helpers;

import org.jsoup.nodes.Node;

/**
 * Created by Brian Roadifer on 6/30/2016.
 */
public interface IReadability {

    void execute(Node node);

    boolean fn(Node node);
}
