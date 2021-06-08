package source.com.beatbox;

import java.io.*;
import java.net.Socket;
import java.util.*;
import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class BeatBox {
   private JFrame theFrame;
   private JPanel mainPanel;
   private JList<String> incomingList;
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
         this.out= new ObjectOutputStream(sock.getOutputStream());
         this.in= new ObjectInputStream(sock.getInputStream());
        
         /*Thread remote= new Thread(new RemoteReader());
            remote.start();
         */
      } catch (Exception e) { System.out.println("Couldn't connect You will have to play alone !"); }  
      
      setUpMIDI();
      buildGUI();  
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

       this.incomingList= new JList<String>();
       this.incomingList.addListSelectionListener(new MyListSelectionLister());
       this.incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION );
       var theList= new JScrollPane(incomingList);
       buttonBox.add(theList);

       this.incomingList.setListData(this.listVector);

       var nameBox= new Box(BoxLayout.Y_AXIS);
       for (var i = 0; i < this.instruments.length; i++) {
          nameBox.add(new Label(this.instrumentsNames[i]));
       }

       background.add(BorderLayout.EAST, buttonBox);
       background.add(BorderLayout.WEST, nameBox);

       theFrame.getContentPane().add(background);
       var grid= new GridLayout(16,16);
       grid.setVgap(1);
       grid.setHgap(2);

       this.mainPanel= new JPanel(grid);
       background.add(BorderLayout.CENTER, mainPanel);

       for (var i = 0; i < 256; i++) {
          var c= new JCheckBox();
          c.setSelected(false);
          this.checkBoxList.add(c);
          mainPanel.add(c);
       }

       this.theFrame.setBounds(50, 50, 300, 300);
       this.theFrame.pack();
       this.theFrame.setVisible(true);
   }

   public void setUpMIDI() {
      try {
         this.sequencer= MidiSystem.getSequencer();
         this.sequencer.open();
         this.sequence= new Sequence(Sequence.PPQ, 4);
         
         this.track= this.sequence.createTrack();
         this.sequencer.setTempoInBPM(120);
      } catch (Exception e) { e.printStackTrace(); } 
      finally { this.sequencer.close(); } 
   }

   public void buildTrackAndStart() {
      ArrayList<Integer> trackList= null;
      this.sequence.deleteTrack(this.track);
      this.track= this.sequence.createTrack();

      /*
         Build a track by walking through the checkboxes to get their state,
         and mapping that to an instrument (and making the MidiEvent for it)
      */
      for (var i = 0; i < this.instruments.length; i++) { 
         trackList= new ArrayList<>();

         for (var j = 0; j < 16; j++) { //Loop through the 16 columns of the line 'i'
            JCheckBox jc= checkBoxList.get(j + (16*i));

            if (jc.isSelected()) {
               trackList.add(this.instruments[i]);
            } else {
               trackList.add(null); //Because this slot should be empty in the track
            }
         }
         //makeTracks(trackList);
      }
      //this.track.add(makeEvent(192,9,1,0,15));
      try {
         this.sequencer.setSequence(this.sequence);
         this.sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
         this.sequencer.start();
         this.sequencer.setTempoInBPM(120);
      } catch (Exception e) { e.printStackTrace(); }
   }

   public class MyStartListener implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent e) { buildTrackAndStart(); }
   }

   public class MyStopActionListener implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent e) { sequencer.stop(); }
   }
   
   public class MyUpTempoListener implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent e) {
         float tempoFactor= sequencer.getTempoFactor();
         sequencer.setTempoFactor((float) (tempoFactor * 1.03));
      }
   }
   public class MyDownTempoListener implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent e) {
         float tempoFactor= sequencer.getTempoFactor();
         sequencer.setTempoFactor((float) (tempoFactor * .97));
      }
   }

   public class MySendListener implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent e) {
         //make an arraylist of just the STATE of the checkboxes
         var checkboxState= new boolean[256];
         for (var i = 0; i < 256; i++) {
            JCheckBox jc= checkBoxList.get(i);

            if (jc.isSelected()) {
               checkboxState[i]= true;
            }
         }

         String messageToSend= null;
         try {
            out.writeObject(userName + nextNumb++ +": " + userMessage.getText());
            out.writeObject(checkboxState);
         } catch (Exception ex) {
            System.out.println("Sorry dude. Could not send it to the server !");
         }
      }
   }

   public class MyListSelectionLister implements ListSelectionListener {
      @Override
      public void valueChanged(ListSelectionEvent le) {
          if (!le.getValueIsAdjusting()) {
             String selected= incomingList.getSelectedValue();

             if (selected != null) {
                //Now go to the map, and change the sequence
                boolean[] selectedState= otherSeqsMap.get(selected);
                //changeSequence(selectedState);
                sequencer.stop();
                //buildTrackAndStart();
             }
          }
      }
   }
}