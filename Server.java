import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server 
{
    private ServerSocket socket;
    private int port = 8000;
    private long chunk = 100000000; // default chunk length
    private long radius = 1000; // default radius size

    private long incount = 0;
    private long executions = 0;

    public class Worker extends Thread 
    {
        private Socket clientSocket;
        private ObjectOutputStream outs;
        private ObjectInputStream ins;
        private Server server;

        public Worker(Socket s, Server srv) // prep a new client connection
        {
            this.clientSocket = s;
            this.server = srv;
        }

        public void run() // Thread code to actually handle workers
        {
            System.out.println("Worker thread started");
            try
			{
				outs = new ObjectOutputStream(clientSocket.getOutputStream());
				ins = new ObjectInputStream(clientSocket.getInputStream());
                System.out.println("Client Initialised");
                
                // set the worker up to keep going for chunk size
                Message startM = new Message();
                startM.chunk = this.server.getChunk();
                startM.radius = this.server.getRadius();
                outs.writeObject(startM);
                outs.flush();
                
                // now loop forever waiting for responses from the client
                while(true)
                {
                    Message in = (Message) ins.readObject();
                    System.out.println("Client update: "+in.incount+"/"+in.chunk);
                    // Process
                    this.server.Result(in.incount, in.chunk); // report back to main server class
                }
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
        }
    }

    public void setPort(int p) { this.port = p; }
    public int getPort() { return this.port; }
    public void setChunk(long c) { this.chunk = c; }
    public long getChunk() { return this.chunk; }
    public void setRadius(long r) { this.radius = r; }
    public long getRadius() { return this.radius; }

    public void RunServer() // start the server and ServerSocket
    {
        System.out.println("Opening Server on Port "+this.port);
        try
        {
            this.socket = new ServerSocket(this.port);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return;
        }
        System.out.println("Server Port Opened");

        while(true) // just keep on truckin'
        {
            System.out.println("Server waiting for connection...");
            try
            {
                Socket client = this.socket.accept();
                System.out.println("Client has made connection");
                Worker w = new Worker(client, this);
                w.start();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public synchronized void Result(long in, long chunk) // avoid race conditions!
    {
        incount += in;
        executions += chunk;

        double pi = (double)((4 * incount) / (double)executions);

        System.out.println("PI = 4 x ("+incount+"/"+executions+") = "+pi);
    }
    
    public static void main(String[] args)
    {
        Server s = new Server();
        if (args.length > 1)
			s.setPort(Integer.parseInt(args[1]));
        s.RunServer();
    }
}
