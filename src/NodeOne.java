import java.util.Vector;

public class NodeOne extends Node
{
    public NodeOne()
    {
        name = 1;
        time = 1000;
        otherSites = new Vector<>();
        replies = new Vector<>();
        waiting_requests = new Vector<>();
        otherSites.add("nodeTwo");
        otherSites.add("nodeThree");
    }
}

