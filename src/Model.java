import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by pandhu on 30/03/16.
 */
public class Model {
    private ArrayList<String> users;
    private ArrayList<Item> items;
    private HashMap<Item, Double> priorProbs;
    private ArrayList<Transaction> transactions;
    private HashMap<String, ArrayList<Item>> userInterests;
    private HashMap<TupleItem, Double> conditionalProbs;
    private static final int CN= 3;
    public Model() {
        this.users = new ArrayList<>();
        this.items = new ArrayList<>();
        this.priorProbs = new HashMap<>();
        this.transactions = new ArrayList<>();
        this.userInterests = new HashMap<>();
        this.conditionalProbs = new HashMap<>();

    }

    public void assignUserInterests(){
        //initial user interests
        for(String user : users){
            this.userInterests.put(user, new ArrayList<Item>());
        }
        for(Transaction transaction : transactions){
            if(!this.userInterests.get(transaction.user).contains(transaction.item))
                this.userInterests.get(transaction.user).add(transaction.item);
        }
    
    }
    public void calculatePriorProb(){
        System.out.println("Start calculate prior probabilistic");
        HashMap<String, HashMap<String, Integer>> hasVote = new HashMap<>();
        HashMap<Item, Integer> countProbs = new HashMap<>();

        for(Transaction transaction : this.transactions){
            //kalo si item baru muncul di transaksi
            if(hasVote.get(transaction.item) == null){
                hasVote.put(transaction.item, new HashMap<String, Integer>());
            }
            //kalo user belom vote
            if(hasVote.get(transaction.item).get(transaction.user) == null){
                //kalo belom ada hasnya
                if(countProbs.get(transaction.item) == null)
                    countProbs.put(transaction.item, 1);
                else
                    countProbs.put(transaction.item, countProbs.get(transaction.item)+1);

            }
        }

        Iterator it = countProbs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            double priorProbability = ((int) pair.getValue()/1.0)/users.size();
            this.priorProbs.put((Item)pair.getKey(), priorProbability);
        }

    }

    public void calculateConditionalProb(){
        HashMap<TupleItem, Integer> count = new HashMap<>();
        for(String user : users){
            for(Item itemA: userInterests.get(user)){
                for(Item itemB: userInterests.get(user)) {
                    if (!itemA.equals(itemB)) {
                        //kalo belom ada
                        TupleItem tuple = new TupleItem();
                        tuple.x = itemA;
                        tuple.y = itemB;
                        if(count.get(tuple) == null){
                            count.put(tuple, 1);
                        } else {
                            count.put(tuple, count.get(tuple)+1);
                        }
                    }
                }
            }
        }

        Iterator it = count.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            double jointProb = ((int) pair.getValue()/1.0)/users.size();
            double conditionalProb = jointProb/this.priorProbs.get(((TupleItem)pair.getKey()).x);
            this.conditionalProbs.put((TupleItem) pair.getKey(), conditionalProb);
        }
    }

    public HashMap<Item, Double> makeTopNRecommendation(String user, int n){
        HashMap<Item, Double> recommendedItems = new HashMap<>();

        for(Item item: this.items){
            double priorProbs;
            if (this.priorProbs.get(item) == null){
                priorProbs = 0;
            } else{
                priorProbs = this.priorProbs.get(item);
            }
            double recProbs = priorProbs;
            for(Item itemInterest: this.userInterests.get(user)){
                double conditionalProbs;
                TupleItem tuple = new TupleItem();
                tuple.x = item;
                tuple.y = itemInterest;
                if(this.conditionalProbs.get(tuple) == null){
                    conditionalProbs = 0;
                } else {
                    conditionalProbs = this.conditionalProbs.get(tuple);
                }
                recProbs = recProbs * Math.pow(conditionalProbs/priorProbs, CN/this.userInterests.get(user).size());
            }
            recommendedItems.put(item, recProbs);
        }
        HashMap<Item, Double> sortedRecommendedItems = sortByComparator(recommendedItems);
        HashMap<Item, Double> nRecommendedItems = new HashMap<>();

        Iterator it = sortedRecommendedItems.entrySet().iterator();
        for (int ii = 0; ii < n; ii++) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey()+","+pair.getValue());
            nRecommendedItems.put((Item)pair.getKey(), (double)pair.getValue());
        }
        return nRecommendedItems;
    }
    public void printUsers(){
        for(String user: this.users){
            System.out.println(user);
        }
    }

    public void printItems(){
        for(Item item: this.items){
            System.out.println(item.id);
        }
    }

    public void printTransaction(){
        for(Transaction transaction : transactions){
            System.out.println(transaction);
        }
    }

    public void printUserInterests(){
        Map mp = this.userInterests;
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String user = (String) pair.getKey();
            System.out.print(user+": ");
            for(String item : (ArrayList<String>)pair.getValue()){
                System.out.print(item+" ");
            }
            System.out.println();
        }
    }

    public void setUsers(ArrayList<String> users){
        this.users = users;
    }
    public void setItems(ArrayList<Item> items){
        this.items = items;
    }
    public void setTransactions(ArrayList<Transaction> transasctons){
        this.transactions = transasctons;
    }

    public void printPriorProbs() {
        Map mp = this.priorProbs;
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if(!pair.getValue().equals("0.0"))
                System.out.println(pair.getKey() + " = " + pair.getValue());
        }
    }

    public void printConditionalProbs() {
        Map mp = this.conditionalProbs;
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if(!pair.getValue().equals("0.0"))
                System.out.println(pair.getKey() + " = " + pair.getValue());
        }
    }
    /*
    * UTILITIES
    * */

    private static HashMap<Item, Double> sortByComparator(HashMap<Item, Double> unsortMap) {

        // Convert Map to List
        List<Map.Entry<Item, Double>> list =
                new LinkedList<Map.Entry<Item, Double>>(unsortMap.entrySet());

        // Sort list with comparator, to compare the Map values
        Collections.sort(list, new Comparator<Map.Entry<Item, Double>>() {
            public int compare(Map.Entry<Item, Double> o1,
                               Map.Entry<Item, Double> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // Convert sorted map back to a Map
        HashMap<Item, Double> sortedMap = new LinkedHashMap<Item, Double>();
        for (Iterator<Map.Entry<Item, Double>> it = list.iterator(); it.hasNext();) {
            Map.Entry<Item, Double> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    private static HashMap sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue())
                        .compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public HashMap<Item, Double> getPriorProbs() {
        return priorProbs;
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public HashMap<String, ArrayList<Item>> getUserInterests() {
        return userInterests;
    }

    public HashMap<TupleItem, Double> getConditionalProbs() {
        return conditionalProbs;
    }
}

class Transaction{
    public String user;
    public String item;

    @Override
    public String toString(){
        return user+" buys "+item;
    }
}

class Item{
    public String name;
    public String id;

    @Override
    public boolean equals(Object o)
    {
        return this.id.equals(((Item)o).id);
    }
    @Override
    public int hashCode()
    {
        return this.id.hashCode();
    }
}

class TupleItem{
    public Item x;
    public Item y;

    @Override
    public int hashCode()
    {
        return (x.name+","+y.name).hashCode();
    }
    @Override
    public boolean equals(Object o)
    {
        Item oX = ((TupleItem)o).x;
        Item oY = ((TupleItem)o).y;
        return (x.name+","+y.name).equals(oX.name+","+oY.name);
    }
}