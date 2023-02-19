import java.util.Vector;

public class NodeTwo extends Node
{
    public NodeTwo()
    {
        name = 2;
        time = 500;
        otherSites = new Vector<>();
        replies = new Vector<>();
        waiting_requests = new Vector<>();
        otherSites.add("nodeOne");
        otherSites.add("nodeThree");
    }
}
