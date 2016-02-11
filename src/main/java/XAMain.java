import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.transaction.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

/**
 * Created by e440 on 2016/1/23.
 */
public class XAMain {

    private static Logger logger = Logger.getLogger(XAMain.class);

    private static ApplicationContext ctx;

    public static void main(String[] args) throws BeansException, InterruptedException {

        try {
            ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        } catch (BeansException e) {

        }
        Random rd = new Random();
        XAMain main = new XAMain();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 2; i++) {
            int transMoney = rd.nextInt(1000000);
            //main.transeMoney(transMoney, transMoney%2);
            main.transeMoney(transMoney, transMoney % 2, true);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("cost time = " + (endTime-startTime));

    }

    private void transeMoney(int money, int forward, boolean b) {

        logger.debug("money = " + money);
        int accountAAddMoney = 0;
        int accountBAddMoney = 0;
        if (forward == 1) {
            accountAAddMoney = money;
            accountBAddMoney = -money;
        } else {
            accountAAddMoney = -money;
            accountBAddMoney = money;
        }
        UserTransactionManager utm = (UserTransactionManager) ctx
                .getBean("atomikosTransactionManager");

        try {
            utm.begin();
            modifyAccountAMoney(accountAAddMoney);
            modifyAccountBMoney(accountBAddMoney);
            utm.commit();
        } catch (NotSupportedException e) {
            try {
                utm.rollback();
            } catch (SystemException e1) {
                e1.printStackTrace();
            }
        } catch (SystemException e) {
            try {
                utm.rollback();
            } catch (SystemException e1) {
                e1.printStackTrace();
            }
        } catch (SQLException e) {
            try {
                utm.rollback();
            } catch (SystemException e1) {
                e1.printStackTrace();
            }
        } catch (HeuristicRollbackException e) {
            try {
                utm.rollback();
            } catch (SystemException e1) {
                e1.printStackTrace();
            }
        } catch (RollbackException e) {
            try {
                utm.rollback();
            } catch (SystemException e1) {
                e1.printStackTrace();
            }
        } catch (HeuristicMixedException e) {
            try {
                utm.rollback();
            } catch (SystemException e1) {
                e1.printStackTrace();
            }
        }


    }

    private void modifyAccountBMoney(int accountBAddMoney) throws SQLException {

        AtomikosDataSourceBean adsB = (AtomikosDataSourceBean) ctx.getBean("dataSourceBeanB");
        Connection connB = null;

        try {
            connB = adsB.getConnection();
            ResultSet rsB = connB.prepareStatement("SELECT money from account where UserId = '123'").executeQuery();
            BigDecimal accountBMoney = BigDecimal.ZERO;
            if (rsB.next()) {
                accountBMoney = rsB.getBigDecimal(1);
            }
            accountBMoney = accountBMoney.add(new BigDecimal(accountBAddMoney));

            connB.prepareStatement(
                    "update account set money = '" + accountBMoney.toBigInteger() + "' where UserId = '123'")
                    .execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (null != connB) {
                try {
                    connB.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        //1/100的概率抛出异常
        throwSqlException(100, "modifyAccountBMoney");
    }

    private void modifyAccountAMoney(int accountAAddMoney) throws SQLException {

        AtomikosDataSourceBean adsA = (AtomikosDataSourceBean) ctx.getBean("dataSourceBeanA");
        Connection connA = null;
        try {
            connA = adsA.getConnection();
            ResultSet rsA = connA.prepareStatement("SELECT money from account where UserId = '123'").executeQuery();
            BigDecimal accountAMoney = BigDecimal.ZERO;
            if (rsA.next()) {
                accountAMoney = rsA.getBigDecimal(1);
            }
            accountAMoney = accountAMoney.add(new BigDecimal(accountAAddMoney));

            connA.prepareStatement(
                    "update account set money = '" + accountAMoney.toBigInteger() + "' where UserId = '123'")
                    .execute();

        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (null != connA) {
                try {
                    connA.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }


        //1/50的概率抛出异常
        throwSqlException(50, "modifyAccountAMoney");
    }

    /*
    public void transeMoney(int money, int forward) throws BeansException, InterruptedException {

        UserTransactionManager utm = (UserTransactionManager) ctx
                .getBean("atomikosTransactionManager");

        AtomikosDataSourceBean adsA = (AtomikosDataSourceBean) ctx.getBean("dataSourceBeanA");
        Connection connA = null;

        AtomikosDataSourceBean adsB = (AtomikosDataSourceBean) ctx.getBean("dataSourceBeanB");
        Connection connB = null;

        try {
            utm.begin();
            connA = adsA.getConnection();
            connB = adsB.getConnection();


            ResultSet rsA = connA.prepareStatement("SELECT money from account where UserId = '123'").executeQuery();
            BigDecimal accountAMoney = BigDecimal.ZERO;
            if (rsA.next()) {
                accountAMoney = rsA.getBigDecimal(1);
            }


            ResultSet rsB = connB.prepareStatement("SELECT money from account where UserId = '123'").executeQuery();
            BigDecimal accountBMoney = BigDecimal.ZERO;
            if (rsB.next())
            {
                accountBMoney = rsB.getBigDecimal(1);
            }

            System.out.println("before transfer accountA = " + accountAMoney + " account B = " + accountBMoney
            + "with transe money =  " + money );
            if (forward == 1)
            {
                accountAMoney = accountAMoney.add(new BigDecimal(money));
                accountBMoney = accountBMoney.subtract(new BigDecimal(money));
            }
            else
            {
                accountAMoney = accountAMoney.subtract(new BigDecimal(money));
                accountBMoney = accountBMoney.add(new BigDecimal(money));
            }

            //System.out.println("accountA = " + accountAMoney + " account B = " + accountBMoney);

            connA.prepareStatement(
                    "update account set money = '" + accountAMoney.toBigInteger() + "' where UserId = '123'")
                    .execute();
            //1/20的概率抛出异常
            throwSqlException(20);
            Thread.sleep(new Random().nextInt(100));

            connB.prepareStatement(
                    "update account set money = '" + accountBMoney.toBigInteger() + "' where UserId = '123'")
                    .execute();
            //1/10的概率抛出异常
            throwSqlException(10);

            utm.commit();
        } catch (SystemException e) {
            e.printStackTrace();
            try {
                utm.rollback();
            } catch (SystemException e1) {
                e1.printStackTrace();
            }
        } catch (HeuristicRollbackException e) {
            try {
                utm.rollback();
            } catch (SystemException e1) {
                e1.printStackTrace();
            }
        } catch (RollbackException e) {
            try {
                utm.rollback();
            } catch (SystemException e1) {
                e1.printStackTrace();
            }
        } catch (SQLException e) {
            try {
                utm.rollback();
            } catch (SystemException e1) {
                e1.printStackTrace();
            }
        } catch (HeuristicMixedException e) {
            try {
                utm.rollback();
            } catch (SystemException e1) {
                e1.printStackTrace();
            }
        } catch (NotSupportedException e) {
            try {
                utm.rollback();
            } catch (SystemException e1) {
                e1.printStackTrace();
            }
        }
        finally {
            if (null != connA)
            {
                try {
                    connA.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (null != connB)
            {
                try {
                    connB.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }
    }
*/
    private void throwSqlException(int probability, String name) throws SQLException {
        Random rd = new Random();
        if (rd.nextInt(probability) == 5) {
            System.out.println("throw exception roll back transactions name = " + name);
            throw new SQLException("random exception");
        }

    }

}
