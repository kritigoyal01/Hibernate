import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.LockModeType;

public class SimulateHibernateOptimisticLocking {
    public static void main(String[] args) {
        Long perosnId = 1L;
        Thread t1 = new Thread(new Runnable() {
            Session session1 = HibernateUtil.getSessionFactory().openSession();
            Transaction tx = null;

            @Override
            public void run() {
                Person person = session1.get(Person.class, perosnId);
                if (person != null) {
                    tx = session1.beginTransaction();

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    person.setPassword("pass@34");
                    session1.update(person);
                    tx.commit();
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            Session session2 = HibernateUtil.getSessionFactory().openSession();
            Transaction tx = null;

            @Override
            public void run() {
                Person person = session2.get(Person.class, perosnId);
                if (person != null) {
                    tx = session2.beginTransaction();
                    person.setPassword("pass#234");
                    session2.update(person);
                    tx.commit();
                }
            }
        });

        t1.start();
        t2.start();

        //Pessimistic Locking
        Session session3 = HibernateUtil.getSessionFactory().openSession();
        Transaction tx1 = null;
        Person person = session3.get(Person.class, perosnId);
        if (person != null) {
            tx1 = session3.beginTransaction();
            //Acquire lock so that other transaction wont update
            session3.lock(person, LockModeType.PESSIMISTIC_WRITE);
            //performing task to update the password
            person.setPassword("pass#234");
            session3.update(person);
            //Commit the transaction
            tx1.commit();
        }
    }
}