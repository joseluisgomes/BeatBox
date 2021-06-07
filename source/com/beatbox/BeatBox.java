package source.com.beatbox;

import java.io.*;
import java.net.Socket;
import java.util.*;
import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;

public class BeatBox {
   private JFrame theFrame;
   private JPanel mainPanel;
   private JList incomingList;
   private JTextField userMessage;
   private ArrayList<JCheckBox> checkBoxList;   

   private int nextNumb;
   private Vector<String> listVector;
   private String userName;
   private ObjectOutputStream out;
   private ObjectInputStream in;
   private HashMap<String, boolean[]> otherSeqsMap;

   private Sequencer sequencer;
   private Sequence sequence;
   private Sequence mySequence;
   private Track track; 

   private String[] instrumentsNames= {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymball", "Hand Clap", "High Tom", 
   "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo", "Open Hi Conga"};

   private int[] instruments= {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58, 47, 67, 63};

    public BeatBox() {
        this.otherSeqsMap = new HashMap<>();
        this.listVector= new Vector<>();
    }

   public static void main(String[] args) {
       
   }

   public void startUp(String name) {
      this.userName = name;
      try (var sock= new Socket("127.0.0.1", 4242);) {
         out= new ObjectOutputStream(sock.getOutputStream());
         in= new ObjectInputStream(sock.getInputStream());
        
         /*Thread remote= new Thread(new RemoteReader());
            remote.start();
         */
      } catch (Exception e) { System.out.println("Couldn't connect You will have to play alone !"); }  

      /*
        setUpMidi();
        buildGUI();
      */
   }

   public void buildGUI() {
       this.theFrame= new JFrame("Cyber BeatBox");
       var layout= new BorderLayout();
       var background= new JPanel(layout);

       background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

       this.checkBoxList= new ArrayList<>();
       var buttonBox= new Box(BoxLayout.Y_AXIS);

       var start= new JButton("Stop");
       start.addActionListener(new MyStartListener());
       buttonBox.add(start);

       var stop= new JButton("Stop");
       stop.addActionListener(new MyStopActionListener());
       buttonBox.add(stop);

       var upTempo= new JButton("Tempo Up");
       upTempo.addActionListener(new MyUpTempoListener());
       buttonBox.add(upTempo);

       var downTempo= new JButton("Tempo Down");
       downTempo.addActionListener(new MyDownTempoListener());
       buttonBox.add(downTempo);

       var sendIt= new JButton("Send It");
       sendIt.addActionListener(new MySendListener());
       buttonBox.add(sendIt);
       
       this.userMessage= new JTextField();
       buttonBox.add(userMessage);

       JList incomingList= new JList();
       incomingList.addListSelectionListener(new MyListSelectionLister());
       incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION );
       JScrollPane listScroller= new JScrollPane(incomingList);
       

   }
}