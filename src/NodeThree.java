import java.util.Vector;

public class NodeThree extends Node
{
    public NodeThree()
    {
        name = 3;
        time = 1764;
        otherSites = new Vector<>();
        replies = new Vector<>();
        waiting_requests = new Vector<>();
        otherSites.add("nodeOne");
        otherSites.add("nodeTwo");
    }
}
