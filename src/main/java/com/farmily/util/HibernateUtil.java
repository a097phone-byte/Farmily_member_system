package com.farmily.util;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

// SessionFactory and Singleton Pattern
public class HibernateUtil {

    private static StandardServiceRegistry registry;

    //	step 2. 宣告一個私有的static資料，透過呼叫createSessionFactory()
//	得到回傳的 SessionFactory 物件
    private static final SessionFactory sessionFactory = createSessionFactory();

    //	step 3. 宣告一個公開的 getter static 方法: getSessionFactory()
//	讓外界可以直接透過類別名稱呼叫取得這個唯一的 SessionFactory
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }


    //	step 1. 宣告一個私有的static方法，用來初始化並回傳SessionFactory
    private static SessionFactory createSessionFactory() {
        try {

            registry = new StandardServiceRegistryBuilder()
                    .configure()
                    .build();

            SessionFactory sessionFactory = new MetadataSources(registry)
                    .buildMetadata()
                    .buildSessionFactory();

            return sessionFactory;

        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }

    }

    public static void shutdown() {
        if (registry != null)
            StandardServiceRegistryBuilder.destroy(registry);
    }

}
