import java.io.*;
import java.net.Socket;
import java.util.Random;

public class Client 
{
    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.out.println("Usage: client serverip serverport");
            return;
        }

        String server = args[0];
        int port = Integer.parseInt(args[1]);

        System.out.println("Connecting to "+server+":"+port);

        try
        {
            long chunk = 0; // our chunk size
            long radius = 0; // radius
            Socket socket = new Socket(server, port);
            ObjectOutputStream outs = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ins = new ObjectInputStream(socket.getInputStream());
            
            Message in = (Message) ins.readObject();
            chunk = in.chunk;
            radius = in.radius;
            Random rand = new Random();

            System.out.println("Starting worker with chunk size of "+chunk+" with radius "+radius);

            while(true) // keep going forever
            {
                System.out.println("Starting loop for chunk of "+chunk);
                long incount = 0;
                // do the loop
                for (long i=0; i<chunk; ++i)
                {
                    long x = rand.nextLong(radius);
                    long y = rand.nextLong(radius);
                    double h = Math.sqrt((x*x)+(y*y)); // precision nightmare but... who cares for this demo
                    if (h<=radius) incount++;
                }
                System.out.println("Found: "+incount+" / "+chunk);
                Message resp = new Message();
                resp.incount = incount;
                resp.chunk = chunk;
                resp.radius = radius;
                System.out.println("Sending to server");
                outs.writeObject(resp);
                outs.flush();
                System.out.println("Sent!");
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return;
        }



    }
}
