import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class DataCluster {

    public static void main (String [] args) throws IOException {
        
        //Create a HashMap to store every single item, key is the item, value is a list
        HashMap<Integer, ArrayList> idToConnectlist = new HashMap<Integer, ArrayList>();
        
        //Read in and Parse the reducedCell.csv file
        try {
            BufferedReader in = new BufferedReader(new FileReader("reducedCell.csv"));
            String str;
            
            while ((str = in.readLine()) != null) {
                //Parsing the numbers on every line, reverse id1 and id2 to get cluster from different direction
                String[] tokens = str.split(",");
                
                //First run, comment out second run
                int id1 = Integer.parseInt(tokens[0]);
                int id2 = Integer.parseInt(tokens[1]);
                
                //Second run, comment out first run
                //int id2 = Integer.parseInt(tokens[0]);
                //int id1 = Integer.parseInt(tokens[1]);
                
                //Put the number and its corresponding number into map
                if (idToConnectlist.get(id1) == null){
                    ArrayList<Integer> list = new ArrayList<Integer>();
                    list.add(id2);
                    idToConnectlist.put(id1, list);
                } else {
                    (idToConnectlist.get(id1)).add(id2);
                }
            }
                
            in.close();
        } catch (IOException e) {
            System.out.println("Reading file exception");
        }
        
        //Create a Set list to store all the cluster sets
        ArrayList<Set<Integer>> setList = new ArrayList<Set<Integer>>();
        
        //Iterate the occurrenceToWordlist map
        Iterator<Entry<Integer, ArrayList>> iterator = idToConnectlist.entrySet().iterator() ;
       
        while(iterator.hasNext()){
            Map.Entry<Integer, ArrayList> listEntry = iterator.next();
            
            //Get the leading number of a set
            int num = listEntry.getKey();
            
            //Create a individual set to store all the numbers of a cluster
            ArrayList<Integer> innerList = listEntry.getValue();
            
            Set<Integer> cSet = new HashSet<Integer>();
            cSet.add(num);
             
            //Add newly created cSet to setList
            createSet(cSet, innerList, idToConnectlist);
            setList.add(cSet);

            iterator.remove();
        }
        
        //Sort setList from largest to smallest
        Collections.sort(setList, new SizeComarator());
        
        //File index
        int index = 1;
        
        //Iterating all the set from setList
        for (int i = 0; i < setList.size(); i++){
            Set<Integer> s = setList.get(i);

            //Find circle structure among all sets
            Set<Integer> s2 = new HashSet<Integer>();
            s2.addAll(s);
            
            do {
                s.addAll(s2);
                BufferedReader in = new BufferedReader(new FileReader("reducedCell.csv"));
                String line;
                
                while ((line = in.readLine()) != null) {
                    String[] anotherTokens = line.split(",");
                    int id1 = Integer.parseInt(anotherTokens[0]);
                    int id2 = Integer.parseInt(anotherTokens[1]);
                    
                      if( !s2.contains(id1) && s2.contains(id2)) { 
                          s2.add(id1);
                      }
                      if( !s2.contains(id2) && s2.contains(id1)) { 
                          s2.add(id2);
                      }
                }   
                
            }while(!s.equals(s2));
            
            
            //Remove duplicated smaller set
            for (int z = i+1; z < setList.size(); z++) {                
                if (s.containsAll(setList.get(z))){
                    setList.remove(z);
                    //setList size decrease by 1, so decrease iterator by 1
                    z--;
                }
            }
                        
            //Print out the bigger clusters (Testing use)
            
//          if (s.size() > 18) {
//              System.out.println("cluster" + index + ".csv");
//              for (Integer n : s) {
//                  System.out.print(n + " ");
//              }
//              System.out.println();
//              index++;
//          } 

        
            
        
            
            if (s.size() > 8) {
                //Create a new csv file to store the data
                Path path = Paths.get("cluster" + index + ".csv");
                
                if (Files.notExists(path)){
                     File file = new File("cluster" + index + ".csv");       
                } 
                
                System.out.println("generating cluster" + index + ".csv...");
                BufferedWriter fileWriter = new BufferedWriter(new FileWriter("cluster" + index + ".csv"));
                fileWriter.write("idORIG,idYIEFD"+"\n");
                fileWriter.flush();
                for (Integer n : s) {
                    
                    //Scan the whole reducedCell.csv to see if any line contains Integer n, if it does, write the line into the new file
                    BufferedReader in = new BufferedReader(new FileReader("reducedCell.csv"));
                    String line;
                    
                    while ((line = in.readLine()) != null) {
                        String[] anotherTokens = line.split(",");
                        int id3 = Integer.parseInt(anotherTokens[0]);
                        int id4 = Integer.parseInt(anotherTokens[1]);
                        
                          if( (id3==n) || (id4==n) ) { 
                              fileWriter.write(line+"\n");
                              fileWriter.flush();
                          }
                    }
                }
                fileWriter.close();
                index++;
            }
        
            
        }
    }
    
    //Recursively creating Cluster Set -- cSet
    private static void createSet(Set<Integer> cSet, ArrayList<Integer> innerList, HashMap<Integer, ArrayList> idToConnectlist) {   
        
        //Add all the numbers in innerList to cSet
        for (int i = 0; i < innerList.size(); i++){
            int innerNum = innerList.get(i);
            if (cSet.contains(innerNum)){
                return;
            }
            cSet.add(innerNum);
        }
        
        //Traverse every number in the innerList, and add their children
        for (int i = 0; i < innerList.size(); i++){
            int innerNum = innerList.get(i);
            
            //If innerNum has a list, traverse the innerNum's list
            if (idToConnectlist.get(innerNum) != null){
                ArrayList<Integer> anotherInnerList = idToConnectlist.get(innerNum);
                createSet(cSet, anotherInnerList, idToConnectlist);
            }   
        }
        return;
    }
    
}

class SizeComarator implements Comparator<Set<?>> {

    @Override
    public int compare(Set<?> o1, Set<?> o2) {
        return Integer.valueOf(o2.size()).compareTo(o1.size());
    }
}

