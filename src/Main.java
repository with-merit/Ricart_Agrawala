import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;


public class Main {
    public static void main(String[] args)
    {
        Runtime runtime = Runtime.instance();
        Profile settings = new ProfileImpl();
        settings.setParameter(Profile.MAIN_HOST, "localhost");
        settings.setParameter(Profile.CONTAINER_NAME, "MainContainer");
        ContainerController container = runtime.createMainContainer(settings);

        try
        {
            container.createNewAgent("NodeOne", "NodeOne", null).start();
            container.createNewAgent("NodeTwo", "NodeTwo", null).start();
            container.createNewAgent("NodeThree", "NodeThree", null).start();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

