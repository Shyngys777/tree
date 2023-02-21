package shyngys.tree;

import javax.persistence.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class MoveTree {
    public static void main(String[] args) {

        EntityManagerFactory factory = Persistence.createEntityManagerFactory("main");
        EntityManager manager = factory.createEntityManager();
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        try {
            manager.getTransaction().begin();
            System.out.print("Enter the id of the category to move from the table: ");
            long categoryId = Long.parseLong(input.readLine());
            System.out.print("Enter the id of the new parent category: ");
            long newParentId = Long.parseLong(input.readLine());
            Tree moveTree = manager.find(Tree.class, newParentId);

            if (newParentId == 0) {
                TypedQuery<Tree> treeTypedQuery = manager.createQuery(
                        "select t from Tree t where t.rightKey > 0", Tree.class
                );
                List<Tree> trees = treeTypedQuery.getResultList();
                int max = trees.get(0).getRightKey();
                for (Tree tree:trees) {
                    if (tree.getRightKey() > max)
                        max = tree.getRightKey();
                }
                System.out.println(max);

                Query query = manager.createQuery(
                        "update Tree t set t.level = t.level - ?1 where t.rightKey < 0 "
                );
                query.setParameter(1, moveTree.getLevel());
                query.executeUpdate();

                Query query1 = manager.createQuery(
                        "update Tree t set t.rightKey= 0 - t.rightKey - ?1 + ?2 +1 where t.rightKey < 0 "
                );
                query1.setParameter(1,moveTree.getLeftKey());
                query1.setParameter(2,max);
                query1.executeUpdate();

                Query query2 = manager.createQuery(
                        "update Tree t set t.leftKey =  0 - t.leftKey -  ?1 + ?2 + 1 where t.leftKey < 0 "
                );
                query2.setParameter(1,moveTree.getLeftKey());
                query2.setParameter(2,max);
                query2.executeUpdate();
            } else {
                Tree parent = manager.find(Tree.class, categoryId);
                int length = parent.getRightKey() - parent.getLeftKey() + 1;
                // Сделать отрицательными ключи перемещаемой категории
                manager.createQuery("update Tree set leftKey = -(leftKey), rightKey = -(rightKey) " +
                                "where leftKey >= :leftKey " +
                                "and rightKey <= :rightKey")
                        .setParameter("leftKey", parent.getLeftKey())
                        .setParameter("rightKey", parent.getRightKey())
                        .executeUpdate();
                // Убрать образовавшийся промежуток в иерархии
                manager.createQuery("update Tree set rightKey = rightKey - :length where rightKey >= :rightKey")
                        .setParameter("length", length)
                        .setParameter("rightKey", parent.getRightKey())
                        .executeUpdate();
                manager.createQuery("update Tree set leftKey = leftKey - :length where leftKey > :leftKey")
                        .setParameter("length", length)
                        .setParameter("leftKey", parent.getRightKey())
                        .executeUpdate();
                // Выделить место в новой родительской категории
                Tree newParent = manager.find(Tree.class, newParentId);
                manager.createQuery("update Tree set leftKey = leftKey + :length where leftKey > :leftKey")
                        .setParameter("length", length)
                        .setParameter("leftKey", newParent.getRightKey())
                        .executeUpdate();
                manager.createQuery("update Tree set rightKey = rightKey + :length where rightKey >= :rightKey")
                        .setParameter("length", length)
                        .setParameter("rightKey", newParent.getRightKey())
                        .executeUpdate();
                manager.refresh(newParent);
                System.out.println(newParent.getRightKey());
                System.out.println(parent.getRightKey());
                System.out.println(newParent.getRightKey() - parent.getRightKey() - 1);
                // Поменять отрицательные ключи перемещаемой категории на подходящие значения для нового местоположения
                // Поменять показатели уровней и перебрать их по определенному порядку

                manager.createQuery("update Tree set leftKey = 0 - leftKey + (:newParentRight - :category - 1), " +
                                "rightKey = 0 - rightKey + (:newParentRight - :category - 1), level = level + " +
                                "(:newParentLevel - :categoryLevel + 1) where leftKey < 0")
                        .setParameter("newParentRight", newParent.getRightKey())
                        .setParameter("category", parent.getRightKey())
                        .setParameter("newParentLevel", newParent.getLevel())
                        .setParameter("categoryLevel", parent.getLevel())
                        .executeUpdate();
            }
            // if the new parent is 0 then
            input.close();
            manager.getTransaction().commit();
            // the moving itself should become the new one
        } catch (Exception e) {
            manager.getTransaction().rollback();
            e.printStackTrace();
        }
    }
}
