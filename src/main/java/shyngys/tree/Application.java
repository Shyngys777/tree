package shyngys.tree;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Application {

    private static final EntityManagerFactory FACTORY = Persistence.createEntityManagerFactory("main");

    private static final BufferedReader IN = new BufferedReader(new InputStreamReader(System.in));
    public static void main(String[] args) {
        // making a new category - [1]
        // - Create a product - [2]
        // - Edit category - [3]
        // - Delete category - [4]
        // Choose action : _________
        while (true) {
            System.out.println("Choose an action you want to perform:");
            System.out.println("[1] --> Create a category");
            System.out.println("[2] --> Create a product");
            System.out.println("[3] --> Update a product");
            System.out.println("[4] --> Delete a product");
            System.out.println("[5] --> Cancel operation");
            System.out.println("Choose an action you want to perform: ");
        }
    }
    public static void createTree() {
//        IN.readLine();
        EntityManager manager = FACTORY.createEntityManager();
        try {
            manager.getTransaction().begin();
            manager.getTransaction().commit();
        } catch (Exception e) {
            manager.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            manager.close();
        }
    }
    public static void moveTree() {

    }
    private static void updateTree() {

    }
}
