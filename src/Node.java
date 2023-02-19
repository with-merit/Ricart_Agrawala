import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.List;
import java.util.Vector;

public class Node extends Agent
{
    protected List<String> replies;
    protected int name;
    protected int time;
    State state = State.OUT;
    protected List<ACLMessage> waiting_requests;
    int lastRequestTimeStamp = 0;
    private final ThreadedBehaviourFactory tb = new ThreadedBehaviourFactory();
    private int counter = 0;
    protected Vector<String> otherSites;

    @Override
    public void setup()
    {
        addBehaviour(tb.wrap(new Server(this, 5000)));
        addBehaviour(tb.wrap(new Client(this, 10000)));
        addBehaviour(tb.wrap(new LamportClock(this, time)));
    }

    class LamportClock extends TickerBehaviour
    {
        public LamportClock(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            counter += 1;
        }
    }

    class Server extends TickerBehaviour
    {

        public Server(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage request = blockingReceive(mt);

            int timeStamp = Integer.parseInt(request.getContent().split(",")[0]);
            int siteName = Integer.parseInt(request.getContent().split(",")[1]);

            counter = Math.max(counter, timeStamp) + 1;  // modify the time stamp

            AID sender = request.getSender();
            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
            reply.addReceiver(sender);
            reply.setContent(Integer.toString(counter));

            if(state == State.OUT)
                send(reply);
            else if(state == State.READY)
            {
                if(lastRequestTimeStamp > timeStamp)
                    send(reply);
                else if(lastRequestTimeStamp < timeStamp)
                    waiting_requests.add(request);
                else {
                    if(name < siteName)
                        waiting_requests.add(request);
                    else
                        send(reply);
                }
            }
            else
                waiting_requests.add(request);
        }
    }


    class Client extends TickerBehaviour
    {
        public Client(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick()
        {
            state = State.READY;

            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);

            for(String site : otherSites)
                request.addReceiver(new AID(site, AID.ISLOCALNAME));

            lastRequestTimeStamp = counter;
            request.setContent(lastRequestTimeStamp + "," + name);
            send(request);

            MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

            //grab the first reply
            ACLMessage msg1 = blockingReceive(messageTemplate);
            AID sender1 = msg1.getSender();
            replies.add(sender1.getLocalName());
            counter = Math.max(counter, Integer.parseInt(msg1.getContent())) + 1;  // modify the time stamp
            Print();

            //grab the second reply
            ACLMessage msg2 = blockingReceive(messageTemplate);
            AID sender2 = msg2.getSender();
            replies.add(sender2.getLocalName());
            counter = Math.max(counter, Integer.parseInt(msg1.getContent())) + 1;  // modify the time stamp

            Print();

            state = State.IN;
            Print();
            blockingReceive(1000);

            //send back replies
            for(ACLMessage req : waiting_requests)
            {
                AID sender = req.getSender();
                ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
                reply.addReceiver(sender);
                reply.setContent(Integer.toString(counter)); // send a message with the current timestamp so we can update the other clock
                send(reply);
            }
            waiting_requests.clear();
            replies.clear();
            state = State.OUT;
            Print();
        }
    }

    private String PrintRequests()
    {
        StringBuilder b = new StringBuilder();
        for(ACLMessage req : waiting_requests)
        {
            String[] parts = req.getContent().split(",");
            b.append("[" + "Timestamp: ").append(parts[0]).append(", ").append("Site :").append(parts[1]).append("]").append(" ");
        }
        return b.toString();
    }

    private String PrintReplies()
    {
        StringBuilder builder = new StringBuilder();
        for(String reply : replies)
            builder.append(reply).append(" ,");
        return builder.toString();
    }

    private void Print()
    {
        StringBuilder build = new StringBuilder();
        build.append("------------").append("Site ").append(name).append("-------------").append("\n");
        build.append("Time Stamp : ").append(counter).append("\n");
        build.append("State : ").append(state).append("\n");
        if(state == State.READY)
            build.append("Req_").append(name).append(" Timestamp : ").append(lastRequestTimeStamp).append("\n");
        build.append("Replies :").append("{ ").append(PrintReplies()).append(" }").append("\n");
        build.append("Queued requests : ").append("{").append(PrintRequests()).append(" }").append("\n");
        if(state == State.IN)
            build.append("Site ").append(name).append(" enters its critical section").append("\n");
        String message = build.toString();

        System.out.println(message);
    }
}
