import java.io.*;
import java.net.*;
import java.util.*;

public class FileServer implements Runnable
{

    Thread sender;
    Thread reciever;

    DatagramSocket sendSkt;
    DatagramSocket recvSkt;

    Queue<DatagramPacket> q;

    public void send(){

        try{

            System.out.println("@FileServer.send");

            String fileName ;
            byte[] buff = new byte[1024];
            FileInputStream fin;

            while(true){

                try{

                    System.out.println("Wrok Ahead :: "+q.size());

                    if(q.size() == 0)Thread.sleep(10000);

                    else {

                        DatagramPacket dpkt = q.remove();

                        System.out.println(dpkt);

                        System.out.println("Progress on hold ");

                        fileName = new String(dpkt.getData(),0,dpkt.getLength());

                        System.out.println("File Name :: "+fileName);

                        try{

                            fin = new FileInputStream(fileName);

                            int FileLength = fin.available();

                            System.out.println("File size :: "+FileLength);

                            sendSkt.send(new DatagramPacket(String.valueOf(fin.available()).getBytes(),String.valueOf(fin.available()).length(),dpkt.getAddress(),dpkt.getPort()));

                            int n = 0;

                            while(n != -1){

                                n = fin.read(buff);

                                if(n != -1){

                                    sendSkt.send(new DatagramPacket(buff,n,dpkt.getAddress(),dpkt.getPort()));

                                }
                            }

                            fin.close();

                        }

                        catch(Exception ex){

                            System.out.println("File "+fileName +"Doesnt Exists");

                            sendSkt.send(new DatagramPacket("0".getBytes(),1,dpkt.getAddress(),dpkt.getPort()));   
                        }
                    }

                }

                catch(Exception ex){

                    ex.printStackTrace();
                    System.out.println("Exception@FileServer.send :: "+ex.getMessage());
                }

                
            }

        }

        catch(Exception ex){

            ex.printStackTrace();
            System.out.println("Exception@FileServer.send :: "+ex.getMessage());
        }
    }

    public void recv(){

        try{

            System.out.println("@FileServer.recv");

            while(true){

                byte[] buff = new byte[1024];

                DatagramPacket dpkt = new DatagramPacket(buff,1024);

                recvSkt.receive(dpkt);

                System.out.println(new String(dpkt.getData(),0,dpkt.getLength()));

                q.add(dpkt);

                sender.interrupt();
            }

        }

        catch(Exception ex){

            ex.printStackTrace();
            System.out.println("Exception@FileServer.recv :: "+ex.getMessage());
        }

        
    }

    public void run(){

        if(Thread.currentThread() == sender)send();

        else recv();
    }

    public FileServer(int port)throws Exception
    {

        System.out.println("@FileServer.FileServer");

        q = new LinkedList<DatagramPacket>();

        recvSkt = new DatagramSocket(port);
        sendSkt = new DatagramSocket();

        sender = new Thread(this);
        reciever = new Thread(this);

        sender.start();
        reciever.start();
    }

    public static void main(String args[]){

        System.out.println("@FileServer.main");

        try{
            
            new FileServer(Integer.parseInt(args[0]));

        } 

        catch(ArrayIndexOutOfBoundsException ex){

            System.out.println("Usage :: java FileServer <portNo>");
        }
        
        catch(Exception ex){
        
            ex.printStackTrace();
            System.out.println("Exception@FileServer.main :: "+ex.getMessage());
            
        }
    }
}