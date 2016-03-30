import java.io.*;
import java.util.*;

/**
 * Created by pandhu on 30/03/16.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        BufferedReader productReader = new BufferedReader(new FileReader("../product.csv"));
        BufferedReader transactionReader = new BufferedReader(new FileReader("../purchase.csv"));
        BufferedReader memberReader = new BufferedReader(new FileReader("../member.csv"));

        Model model = new Model();
        model.setItems(readProduct(productReader));
        model.setUsers(readMember(memberReader));
        model.setTransactions(readTransaction(transactionReader));
        model.calculatePriorProb();
        model.assignUserInterests();
        //Read Input
        Scanner sc = new Scanner(System.in);
        String input;
        do{
            input = sc.nextLine();
            String user = input.split(" ")[0];
            int n = Integer.parseInt(input.split(" ")[1]);
            HashMap<String, Double> recommendedItems = model.makeTopNRecommendation(user, n);
            printHashMap(recommendedItems);
        }while(!input.equals("q"));

        System.out.println(model.getTransactions().size());
        System.out.println(model.getConditionalProbs().size());
    }

    public static ArrayList<String> readProduct(BufferedReader productReader) throws IOException {
        String line;
        ArrayList<String> products = new ArrayList<>();
        while((line = productReader.readLine()) != null){
            String product = line.split(",")[0];
            products.add(product.substring(1,product.length()-1));
        }
        return products;
    }

    public static ArrayList<String> readMember(BufferedReader memberReader) throws IOException {
        String line;
        ArrayList<String> members = new ArrayList<>();
        while((line = memberReader.readLine()) != null){
            String product = line.split(",")[0];
            members.add(product.substring(1,product.length()-1));
        }
        return members;
    }

    public static ArrayList<Transaction> readTransaction(BufferedReader transactionReader) throws IOException {
        String line;
        ArrayList<Transaction> transactions = new ArrayList<>();
        while((line = transactionReader.readLine()) != null){
            String user = line.split(",")[1];
            String item = line.split(",")[2];
            Transaction transaction = new Transaction();
            transaction.item = item.substring(1,item.length()-1);
            transaction.user = user.substring(1,user.length()-1);
            transactions.add(transaction);
        }
        return transactions;
    }

    public static void printHashMap(HashMap<String, Double> map) {
        Map mp = map;
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
                System.out.println(pair.getKey() + " = " + pair.getValue());
        }
    }
}
