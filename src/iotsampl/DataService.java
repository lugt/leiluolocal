package iotsampl;

import earth.server.Monitor;
import iotsampl.iot.core.IotLogger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

/**
 * Created by Frapo on 2017/8/8.
 * Version :17
 * Earth - Moudule iotsampl
 */
public class DataService {
    public static final String REDIS_SERVER = "localhost";

    private static SessionFactory sessionFactory = null;
    public static void setUp() throws HibernateException {

        if(sessionFactory != null) return;

        // A SessionFactory is set up once for an application!
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure("hibernate.cfg.xml") // configures settings from hibernate.cfg.xml
                .build();
        try {
            sessionFactory = new MetadataSources(registry)
                    .buildMetadata()
                    .buildSessionFactory();
        } catch (Exception e) {
            // The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
            // so destroy it manually.
            e.printStackTrace();
            IotLogger.i("Hibernate Exception: "+e.getMessage());
            StandardServiceRegistryBuilder.destroy(registry);
            sessionFactory = null;
        }
    }

    public static Session getSession() throws HibernateException{
        Session session;
        if (sessionFactory == null) {
            setUp();
            if(sessionFactory == null) {
                IotLogger.i("EESession: sessionFactory is dead");
                throw new HibernateException("Could not initiate sessionFactory");
            }
        }
        if(sessionFactory.isClosed()){
            setUp();
            session = sessionFactory.openSession();
        }
        try {
            session = sessionFactory.getCurrentSession();
        }catch (Exception e){
            session =  sessionFactory.openSession();
        }
        return session;
    }

    public static boolean isSessionAlive() {
        if(null == sessionFactory) return false;
        return !sessionFactory.isClosed();
    }

    public static void close() {
        if(null == sessionFactory) return;
        if (null != sessionFactory.getCurrentSession()) sessionFactory.getCurrentSession().close();
        if (sessionFactory != null) {
            sessionFactory.close();
        }

    }

    public static Transaction getTransact(Session session) throws Exception{
        if(session == null || !session.isOpen()){
            Monitor.logger("Session is not Connected in getTransact");
            throw new HibernateException("Session not connected");
        }
        Transaction tr = session.getTransaction();
        if(tr == null){
            tr = session.beginTransaction();
            tr.setTimeout(3);
            return tr;
        }else{
            if(!tr.isActive()){
                tr = session.beginTransaction();
                tr.setTimeout(3);
                return tr;
            }else{
                tr = session.beginTransaction();
                tr.setTimeout(3);
                return tr;
            }
        }
    }
}
