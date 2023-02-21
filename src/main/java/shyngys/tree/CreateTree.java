package shyngys.tree;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CreateTree {
    public static void main(String[] args) {
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("main");
        EntityManager manager = factory.createEntityManager();
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        try {
            manager.getTransaction().begin();
            System.out.print("Enter the id of the existing category or press 0 to create new : ");
            long parentId = Long.parseLong(input.readLine());
            System.out.print("Enter the name of the new category: ");
            String categoryName = input.readLine();

            if (parentId == 0) {
                Integer maxRightKey = manager.createQuery("select max(rightKey) from Tree", Integer.class).getSingleResult();
                
                Tree newCategory = new Tree();
                newCategory.setName(categoryName);
                newCategory.setLevel(0);
                manager.persist(newCategory);

                manager.createQuery("update Tree set rightKey = rightKey + 2 where rightKey >= :rightKey")
                        .setParameter("rightKey", maxRightKey + 1)
                        .executeUpdate();
                manager.createQuery("update Tree set level = level + 2 where leftKey > :rightKey")
                        .setParameter("rightKey", maxRightKey + 1)
                        .executeUpdate();
            } else {
                Tree parent = manager.find(Tree.class, parentId);

                Tree newCategory = new Tree();
                newCategory.setName(categoryName);
                newCategory.setLeftKey(parent.getRightKey());
                newCategory.setRightKey(parent.getRightKey() + 1);
                newCategory.setLevel(parent.getLevel() + 1);

                manager.persist(newCategory);
                manager.createQuery("update Tree set rightKey = rightKey + 2 where rightKey >= :leftKey")
                        .setParameter("leftKey", parent.getRightKey())
                        .executeUpdate();
                manager.createQuery("update Tree set leftKey = leftKey + 2 where leftKey > :leftKey")
                        .setParameter("leftKey", parent.getRightKey())
                        .executeUpdate();
            }
            manager.getTransaction().commit();
        }  catch (Exception e) {
            manager.getTransaction().rollback();
            e.printStackTrace();
        }
    }
}
